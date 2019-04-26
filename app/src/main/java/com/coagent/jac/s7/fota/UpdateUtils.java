package com.coagent.jac.s7.fota;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.UserHandle;
import android.util.Log;

import com.coagent.proxy.setting.SettingManager;
import com.coagent.proxy.update.UpdateManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import static com.coagent.jac.s7.fota.Utils.TAG;

public class UpdateUtils {
    private static final String BROADCAST_UPDATE_LATER = "com.coagent.jac.s7.fota.update_later";

    // 升级的临时文件夹，记录以便升级成功后清理
    private static final String UPDATE_DIR_NAME = "fota_service_update";
    // 当存在升级行为时，该位置会保存标记值，分别是小写的"os"或"mcu"，对应升级OS或MCU
    private static final String ARG_OS_UPDATE_STATUS = "arg_os_update_status";
    private static final String STATUS_OS = "os";
    private static final String STATUS_MCU = "mcu";
    // 当需要升级OS及MCU时，优先升级MCU
    // OS升级包路径
    private static final String ARG_OS_PATH = "arg_os_path";
    // MCU升级包路径
    private static final String ARG_MCU_PATH = "arg_mcu_path";
    // 记录目标系统版本号
    private static final String ARG_OS_VERSION = "arg_os_version";
    // 记录目标MCU版本号
    private static final String ARG_MCU_VERSION = "arg_mcu_version";

    /* 记录检测状态 */
    public static final String UPDATE_STATE = "update_state";
    /* 已是最新版本(或检测出错) */
    public static final int LATEST_VERSION = 1;
    /* 检测到新版本 */
    public static final int NEW_VERSION = 2;
    /* 从未检测过(开机时会重置为该标志位) */
    public static final int NEVER_CHECK = 3;

    public static boolean isUpdateBefore() {
        return !SPUtils.getInstance().getString(ARG_OS_UPDATE_STATUS).isEmpty();
    }

    /**
     * 检查升级是否已经结束
     * 由于可能需要同时升级OS和MCU，而MCU必须是优先升级，因此作一个标记量以保存升级状态
     * @return
     *      true => 升级完毕
     *      false => 还有待升级的步骤
     */
    public static boolean isUpdateFinish() {
        String status = SPUtils.getInstance().getString(ARG_OS_UPDATE_STATUS);
        switch (status) {
            case STATUS_OS:
                Log.e(TAG, "os update finish");
                // 升级mcu
                return true;
            case STATUS_MCU:
                Log.e(TAG, "mcu update finish");
                return !updateOs();
            default:
                // 遇到其他异常状况，则重新走mcu升级流程
                return !updateMcu();
        }
    }

    public static boolean checkUpdateResult() {
        boolean isUpdateSuccess = true;
        if (!isOsUpdateSuccess()) {
            isUpdateSuccess = false;
        }
        if (!isMcuUpdateSuccess()) {
            isUpdateSuccess = false;
        }
        return isUpdateSuccess;
    }

    public static void cleanUpdateTempFile() {
        // 清理文件并清空标志位
        SPUtils.getInstance().put(ARG_OS_PATH, "");
        SPUtils.getInstance().put(ARG_OS_VERSION, "");
        SPUtils.getInstance().put(ARG_MCU_PATH, "");
        SPUtils.getInstance().put(ARG_MCU_VERSION, "");
        SPUtils.getInstance().put(ARG_OS_UPDATE_STATUS, "");
        Log.e(TAG, "clear all update signal");
        File updateTempDir = new File(getTargetDir() + UPDATE_DIR_NAME);
        if (Utils.deleteDir(updateTempDir)) {
            Log.e(TAG, "clean temp");
        } else {
            Log.e(TAG, "an error occured when clear temp dir");
        }
    }

    // 比对系统当前版本号和升级前版本号是否一致
    private static boolean isOsUpdateSuccess() {
        String targetVersion = SPUtils.getInstance().getString(ARG_OS_VERSION);
        String currentOsVersion = getOsVersion();
        Log.e(TAG, "os => target version: " + targetVersion + " current version: " + currentOsVersion);
        return targetVersion.equals(currentOsVersion);
    }

    // 比对MCU当前版本号和升级前版本号是否一致
    private static boolean isMcuUpdateSuccess() {
        String targetVersion = SPUtils.getInstance().getString(ARG_MCU_VERSION);
        String currentMcuVersion = SettingManager.getInstance().getSettingsInfo().mMcuVersion;
        Log.e(TAG, "mcu => target version: " + targetVersion + " current version: " + currentMcuVersion);
        return targetVersion.equals(currentMcuVersion);
    }

    public static void setupConfigurationFile(Context context) {
        // 检查sd卡下是否有配置文件
        String frueConfigFileName = File.separator + "sdcard" + File.separator + "fr_ue_config.json";
        String logConfigFileName = File.separator + "sdcard" + File.separator + "log_config.json";

        // 若不存在，则将assets的配置文件复制到sdcard里
        File frueConfigFile = new File(frueConfigFileName);
        File logConfigFile = new File(logConfigFileName);
        if (frueConfigFile.exists() && logConfigFile.exists()) {
            return;
        }
        Log.e(TAG, "no config files in /sdcard");
        try {
            InputStream is1 = context.getAssets().open("fr_ue_config.json");
            InputStream is2 = context.getAssets().open("log_config.json");
            frueConfigFile.createNewFile();
            logConfigFile.createNewFile();
            FileOutputStream fos1 = new FileOutputStream(frueConfigFile);
            FileOutputStream fos2 = new FileOutputStream(logConfigFile);
            byte[] temp = new byte[1024];
            int i;
            while ((i = is1.read(temp)) > 0) {
                fos1.write(temp, 0, i);
            }
            while ((i = is2.read(temp)) > 0) {
                fos2.write(temp, 0, i);
            }
            fos1.close();
            fos2.close();
            is1.close();
            is2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveTargetOsVersion(String targetVersion) {
        SPUtils.getInstance().put(ARG_OS_VERSION, targetVersion);
    }

    public static void saveTargetMcuVersion(String targetVersion) {
        SPUtils.getInstance().put(ARG_MCU_VERSION, targetVersion);
    }

    public static void installPackage(final String filePath) {
        Log.e(TAG, "install thread start");
        // 直接开线程解压，这里不允许有任何用户操作干预
        new Thread(() -> {
            // 将升级压缩包解压到指定位置
            File dstDir = new File(getTargetDir() + UPDATE_DIR_NAME);
            File srcZip = new File(filePath);
            unzipUpdateZip(srcZip, dstDir);
            // 任何情况下都优先升级mcu
            Log.e(TAG, "update mcu first");
            if (updateMcu()) {
                return;
            }
            Log.e(TAG, "no mcu update package, then update os");
            updateOs();
        }).start();
    }

    public static String getOsVersion() {
        return Build.DISPLAY.split("_")[2];
    }

    public static void unzipUpdateZip(File zipFile, File dir) {
        Log.e(TAG, "unzip update file");
        // 解压文件
        List<File> files;
        try {
            files = Utils.unzipFile(zipFile, dir);
        } catch (IOException e) {
            Log.e(TAG, "unzip error: " + e.getMessage(), e);
            files = Utils.listFilesInDir(dir, false);
        }
        // 由于OS和MCU未必是同时升级，因此先复位其目标版本
        // 对有升级包的部分才设置目标版本
        String mcuTargetVersion = SPUtils.getInstance().getString(ARG_MCU_VERSION);
        String osTargetVersion = SPUtils.getInstance().getString(ARG_OS_VERSION);
        String mcuVersion = SettingManager.getInstance().getSettingsInfo().mMcuVersion;
        SPUtils.getInstance().put(ARG_MCU_VERSION, mcuVersion);
        SPUtils.getInstance().put(ARG_OS_VERSION, getOsVersion());
        for (File file : files) {
            if (file.getName().startsWith("DLT_")) {
                // 以"DLT_"开头的为OS升级包
                Log.e(TAG, "unzip os package, save it as: " + file.getAbsolutePath());
                SPUtils.getInstance().put(ARG_OS_PATH, file.getAbsolutePath());
                SPUtils.getInstance().put(ARG_OS_VERSION, osTargetVersion);
            } else if (file.getName().contains("MCU")) {
                // MCU是一个文件夹，img文件在文件夹内
                List<File> mcuDir = Utils.listFilesInDir(file, false);
                if (mcuDir.isEmpty()) {
                    break;
                }
                // 遍历文件夹，识别出带img后缀的文件作为mcu升级包
                for (File mcuImg : mcuDir) {
                    // 检查后缀是否为img
                    String ext = Utils.getFileExtension(mcuImg.getAbsolutePath());
                    if (ext.equals("img")) {
                        // 保存其升级路径
                        Log.e(TAG, "unzip mcu package, save it as: " + mcuImg.getAbsolutePath());
                        SPUtils.getInstance().put(ARG_MCU_PATH, mcuImg.getAbsolutePath());
                        SPUtils.getInstance().put(ARG_MCU_VERSION, mcuTargetVersion);
                        break;
                    }
                }
            } else if (file.getName().equals("iflytek")) {
                // 当前语音包要复制的目标位置在sdcard根目录的iflytek文件夹内
                String destPath = getVoiceTargetDir();
                File destDir = new File(destPath);
                if (Utils.copyDir(file.getAbsoluteFile(), destDir)) {
                    Log.e(TAG, "copy iflytek success");
                    // 执行adb的同步命令
                    try {
                        Runtime.getRuntime().exec("sync");
                    } catch (IOException e) {
                        Log.e(TAG, "sync error after iflytek update finish: " + e.getMessage(), e);
                    }
                } else {
                    Log.e(TAG, "an error occured when copy iflytek");
                }
            }
        }
    }

    // 发送有新版本的全局通知广播
    public static void sendSystemSettingBroadcast(Context context, boolean updateComplete) {
        Intent broadcast = new Intent(BROADCAST_UPDATE_LATER);
        broadcast.putExtra(BROADCAST_UPDATE_LATER, updateComplete);

        UserHandle handle = null;
        try {
            Class<?> userHandleClass = Class.forName("android.os.UserHandle");
            Field allFiled = userHandleClass.getField("ALL");
            handle = (UserHandle) allFiled.get(userHandleClass);
        } catch (Exception e) {
            Log.e("FotaService", "could not found hide filed of UserHandle", e);
        }
        if (handle != null) {
            context.sendBroadcastAsUser(broadcast, handle);
        } else {
            context.sendBroadcast(broadcast);
        }
    }

    public static long getTargetDirFreeSpace() {
        String targetDir = getTargetDir();
        File dir = new File(targetDir);
        // 转换为kb为单位
        return (int) (dir.getFreeSpace() / 1024);
    }

    // 存档TBox传输过来的数据的目录
    public static String getTargetDir() {
        return File.separator + "sdcard" + File.separator;
    }

    // 语音包的目标路径
    public static String getVoiceTargetDir() {
        return getTargetDir() + "iflytek";
    }

    private static boolean updateOs() {
        SPUtils.getInstance().put(ARG_OS_UPDATE_STATUS, STATUS_OS);
        String filePath = SPUtils.getInstance().getString(ARG_OS_PATH);
        if (filePath.isEmpty()) {
            return false;
        }
        UpdateManager.getInstance().updateOS(filePath);
        Log.e(TAG, "update os: " + filePath);
        return true;
    }

    private static boolean updateMcu() {
        SPUtils.getInstance().put(ARG_OS_UPDATE_STATUS, STATUS_MCU);
        String filePath = SPUtils.getInstance().getString(ARG_MCU_PATH);
        if (filePath.isEmpty()) {
            return false;
        }
        UpdateManager.getInstance().updateMCU(filePath);
        Log.e(TAG, "update mcu: " + filePath);
        return true;
    }
}
