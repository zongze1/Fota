package com.coagent.jac.s7.fota;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.abupdate.iov.Constant.EcuId;
import com.abupdate.iov.event.QueryCallback;
import com.abupdate.iov.event.QueryEvent;
import com.abupdate.iov.event.info.DownloadInfo;
import com.abupdate.iov.event.info.EcuInfo;
import com.abupdate.iov.event.info.ErrorInfo;
import com.abupdate.iov.event.info.HuPackage;
import com.abupdate.iov.event.info.InstallCondition;
import com.abupdate.iov.event.info.InstallInfo;
import com.abupdate.iov.event.info.InstallResult;
import com.abupdate.iov.event.info.ResultInfo;
import com.abupdate.iov.event.info.VersionInfo;
import com.abupdate.iov.task.FotaTask;
import com.abupdate.iov.task.GlobalInter;
import com.abupdate.iov.task.Sysi;
import com.coagent.proxy.can.CanManager;
import com.coagent.proxy.can.EndOfLineInfo;
import com.coagent.proxy.constant.SettingsConstantsDef;
import com.coagent.proxy.setting.SettingManager;
import com.coagent.proxy.source.SourceManager;
import com.coagent.proxy.update.UpdateManager;

import java.lang.reflect.Method;
import java.util.HashSet;

import skin.support.SkinCompatManager;

import static com.coagent.jac.s7.fota.DialogFactory.LATEST_VERSION;
import static com.coagent.jac.s7.fota.DialogFactory.LOADING;
import static com.coagent.jac.s7.fota.DialogFactory.NEW_VERSION;
import static com.coagent.jac.s7.fota.DialogFactory.NO_POWER;
import static com.coagent.jac.s7.fota.DialogFactory.UPDATE_FAILED;
import static com.coagent.jac.s7.fota.DialogFactory.UPDATE_SUCCESS;
import static com.coagent.jac.s7.fota.DialogFactory.UPDATE_CONFIRM;
import static com.coagent.jac.s7.fota.Utils.TAG;

// TODO 由于需求变更，标志位越来越多了，建议重构。将当前状态，检测状态以一个标志位记录即可
public class FotaService extends Service {
    private static final String SERVICE_NAME = "com.coagent.jac.s7.fota";
    /* 默认的限制秒数 */
    private static final int DEFAULT_TIMEOUT = 3000;
    /* 检测超时限制 */
    private static final int CHECK_TIMEOUT = 60 * 1000;
    private static final int TYPE_NO_DIALOG = 1;
    private static final int TYPE_SHOW_LOADING_DIALOG = 2;

    /**
     * 该服务响应的操作
     * 默认 => 根据之前是否有检测过的记录，选择
     * type => {@link #TYPE_NO_DIALOG} 强制检查升级版本，但不显示"加载中"对话框
     * type => {@link #TYPE_SHOW_LOADING_DIALOG} 强制检查升级版本，并显示"加载中"对话框
     */
    private static final String ARG_TYPE = "arg_type";
    /**
     * 记录升级的id，当收到新版本通知时保存任务id到本地
     * 若下次收到新版本通知，则检测本地是否有保存该任务id，若有则不显示对话框
     * 若没有，则显示对话框，并覆盖本地保存的id值
     */
    private static final String SP_UPDATE_TASK_ID = "sp_update_task_id";

    /**
     * 由于jni回调在子线程，无法显示对话框，因此通过handler post到主线程处理
     */
    private Handler handler = new Handler();
    /**
     * 车机启动时tbox未必能启动，因此需要轮询执行车机与tbox的连接操作
     * 设置该标志量，当为true时表示车机与tbox已成功连接上，可以停止轮询
     */
    private boolean connected = false;
    /**
     * 轮询次数，当轮询超过一定次数后，则停止，避免消耗过多CPU(暂设置为5分钟，轮询3秒一次，即100次为最大值)
     */
    private int try_time = 0;
    // 记录检测升级的时间
    private long startCheckTime = 0;
    private static final int TRY_TIMES_MAX = 100;

    /**
     * 记录当前状态
     * STATE_IDLE => IDLE(待机)
     * STATE_CHECK => CHECK(主动检查)
     * STATE_CHECK => CHECK(静默检查)
     * STATE_UPDATE => UPDATE(升级)
     * 当在待机状态或静默检查时即便收到错误信息也不应该弹出对话框
     **/
    private static final int STATE_IDLE= 1;
    private static final int STATE_CHECK = 2;
    private static final int STATE_CHECK_SILENT = 3;
    private static final int STATE_UPDATE = 4;
    private int state = STATE_IDLE;
    /**
     * 管理所有显示的对话框
     */
    private DialogFactory factory;

    /**
     * 当升级成功之后，需要上报升级结果，但可能会有失败的情况发生
     * 该runnable来不断执行任务保证上报成功
     */
    private Runnable uploadRunnable;
    /**
     * 记录服务启动后的当前主题，避免同一个主题重复替换
     */
    private String currentTheme = "";
    /**
     * 错误码白名单，该白名单内的错误码都应该忽略：
     * 103 => 蓄电池不足(在条件检测处已经响应了该异常)
     * 306 => 没有新版本(由于在检测过程中所有错误都会视为"最新版本"，因此该错误码可以直接过滤掉)
     */
    private static final HashSet<Integer> ERROR_WHITELIST = new HashSet<>();
    static {
        ERROR_WHITELIST.add(103);
        ERROR_WHITELIST.add(306);
    }

    /**
     * 启动升级服务
     *
     * @param type 0 => 正常启动服务，不主动检测版本
     *             1 => 启动后执行check version，当收到新版本时强制弹出对话框
     */
    public static Intent newInstance(Context context, int type) {
        Intent intent = new Intent(context, FotaService.class);
        intent.putExtra(ARG_TYPE, type);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 清除任务标记，保证每次车机开机时有新版本都会弹出通知
        SPUtils.getInstance().remove(SP_UPDATE_TASK_ID);
        // 刚开机将当前检测状态重置为未检测过
        SPUtils.getInstance().put(UpdateUtils.UPDATE_STATE, UpdateUtils.NEVER_CHECK);
        factory = new DialogFactory(this, handler);
        // 检查是否有过升级行为
        checkUpdateBefore();
        UpdateUtils.setupConfigurationFile(this);
        handler.removeCallbacks(connectRunnable);
        handler.post(connectRunnable);

        // 将自身添加到ServiceManager里
        try {
            @SuppressLint("PrivateApi")
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method addServiceMethod = serviceManagerClass.getMethod("addService", String.class, IBinder.class);
            addServiceMethod.invoke(serviceManagerClass, SERVICE_NAME, proxy);
        } catch (Exception e) {
            Log.e(TAG, "an error occured when add service to service manager", e);
        }

        String theme = SettingManager.getInstance().getTheme();
        onThemeChanged(theme);
        SettingManager.getInstance().addOnThemeChangedListener(themeChangedListener);
    }

    private void checkUpdateBefore() {
        // 如开机前没有执行过升级的操作则跳过后续步骤
        if (!UpdateUtils.isUpdateBefore()) {
            return;
        }
        // 检查是否完成升级，若所有升级已完成则上报结果
        if (UpdateUtils.isUpdateFinish()) {
            uploadRunnable = () -> {
                boolean isUpdateSuccess = UpdateUtils.checkUpdateResult();
                Log.e(TAG, "upload result of mp5 to tbox: " + isUpdateSuccess);
                QueryCallback<Integer> uploadResultCallback = result -> {
                    if (result == 0) {
                        handler.removeCallbacks(uploadRunnable);
                        uploadRunnable = null;
                        UpdateUtils.cleanUpdateTempFile();
                    } else {
                        // 不断重试保证上报成功
                        Log.e(TAG, "upload result failed, try again soon");
                        handler.postDelayed(uploadRunnable, DEFAULT_TIMEOUT);
                    }
                };
                FotaTask.instance().updateResult(EcuId.EcuEnum.HU, isUpdateSuccess ? 1 : 0, uploadResultCallback);
            };
            handler.post(uploadRunnable);
        } else {
            Log.e(TAG, "update continue, show install progress dialog");
            // 系统安装状态监听
            UpdateManager.getInstance().addUpdateListener(updateListener);
            // 当还有安装步骤时，需要预先显示安装对话框(阻塞用户的其他操作)
            factory.setInstallProgress(0, "");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String theme = SettingManager.getInstance().getTheme();
        onThemeChanged(theme);
        // 如果正在处于安装阶段，则不检查新版本，避免报错
        if (UpdateUtils.isUpdateBefore()) {
            Log.e(TAG, "do not check version during the time of installation");
            return Service.START_STICKY;
        }
        if (intent == null) {
            Log.e(TAG, "intent is null");
            return Service.START_STICKY;
        }

        boolean isIdle = state == STATE_IDLE;
        int type = intent.getIntExtra(ARG_TYPE, TYPE_NO_DIALOG);
        switch (type) {
            case TYPE_SHOW_LOADING_DIALOG:
                // 若当前为静默升级检测状态或空闲状态，而用户又主动点击了检测按钮
                // 则显示等待对话框，并改变检测状态标志值
                // 其余情况(已在主动检测状态或升级状态)则不响应该操作，避免重复发送升级检测请求
                if (state == STATE_CHECK_SILENT || isIdle) {
                    state = STATE_CHECK;
                    // 清空任务id记录，当有新版本来到时强制弹出对话框
                    SPUtils.getInstance().remove(SP_UPDATE_TASK_ID);
                    Log.e(TAG, "check version");
                }
                // 这里只是显示对话框，实际是否发送检测请求，依赖当前检测状态
                factory.showCommonDialog(LOADING);
                break;
            case TYPE_NO_DIALOG:
                if (isIdle) {
                    state = STATE_CHECK_SILENT;
                    Log.e(TAG, "check version silent");
                }
                break;
        }
        if (isIdle) {
            FotaTask.instance().checkVersion();
            startCheckTime = SystemClock.uptimeMillis();
            // 1分钟后超时
            handler.postDelayed(checkTimeoutRunnable, CHECK_TIMEOUT);
        }
        return Service.START_STICKY;
    }

    private Runnable checkTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (state == STATE_IDLE || state == STATE_CHECK_SILENT) {
                state = STATE_IDLE;
                return;
            }
            state = STATE_IDLE;
            factory.showCommonDialog(LATEST_VERSION);
            SPUtils.getInstance().put(UpdateUtils.UPDATE_STATE, UpdateUtils.LATEST_VERSION);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        factory.release();
        Utils.release();
        FotaTask.instance().destroy();
        SettingManager.getInstance().removeOnThemeChangedListener(themeChangedListener);
        UpdateManager.getInstance().removeUpdateListener(updateListener);
        Log.e(TAG, "FotaService has destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private GlobalInter globalInter = new GlobalInter() {
        @Override
        public void onNewVersion(VersionInfo versionInfo) {
            handler.removeCallbacks(checkTimeoutRunnable);
            Log.e(TAG, "get new version: " + versionInfo.toString());
            // 记录之后升级提示所需要的信息
            factory.getUpdateInfo().setUpgradeTime(versionInfo.upgradeTime);
            factory.getUpdateInfo().setEcuInfoList(versionInfo.arrEcuInfo);
            // 检测当前版本升级通知的任务id是否与本地相同，若相同则不弹出对话框
            String dstVer = SPUtils.getInstance().getString(SP_UPDATE_TASK_ID);
            if (dstVer.equals(versionInfo.taskId)) {
                Log.e(TAG, "prompted already, do not show dialog again");
                state = STATE_IDLE;
                return;
            }
            // 复位SP文件中OS和MCU的目标版本为当前版本
            // 因为在更新完毕后会比对当前和目标版本以上传升级结果
            // 但当前升级包未必是为了升级车机的OS及MCU
            String mcuVersion = SettingManager.getInstance().getSettingsInfo().mMcuVersion;
            UpdateUtils.saveTargetOsVersion(getOsVersion());
            UpdateUtils.saveTargetMcuVersion(mcuVersion);
            for (EcuInfo info : versionInfo.arrEcuInfo) {
                if (info.ecuId == EcuId.HU_ID) {
                    // 保存目标版本
                    UpdateUtils.saveTargetOsVersion(info.dstVer);
                    UpdateUtils.saveTargetMcuVersion(info.dstVer);
                    break;
                }
            }

            state = STATE_UPDATE;
            // 收到新版本提示，弹出对话框通知用户，同时记录其任务id(保证不多次弹出)
            factory.showCommonDialog(NEW_VERSION);
            SPUtils.getInstance().put(SP_UPDATE_TASK_ID, versionInfo.taskId);
            SPUtils.getInstance().put(UpdateUtils.UPDATE_STATE, UpdateUtils.NEW_VERSION);
            UpdateUtils.sendSystemSettingBroadcast(FotaService.this, false);
        }

        @Override
        public void onDownloadProgress(final DownloadInfo downloadInfo) {
            float progress;
            progress = downloadInfo.currentNum / (float) downloadInfo.countNum;
            progress *= downloadInfo.nowBytes / (float) downloadInfo.totalBytes;
            if (progress >= 1) {
                Log.e(TAG, "download complete");
                factory.showCommonDialog(UPDATE_CONFIRM);
                return;
            }
            factory.setDownloadProgress(progress);
        }

        @Override
        public void onInstallCondition(InstallCondition installCondition) {
            // 正在安装过程中不显示条件检测弹框
            if (factory.isInstalling()) {
                return;
            }
            Log.e(TAG, "install condition: " + installCondition.toString());
            // 先检测电量，若不通过，显示错误信息框
            if (!installCondition.isBatteryOk) {
                factory.showCommonDialog(NO_POWER);
                return;
            }
            factory.changeCondition(installCondition);
        }

        @Override
        public void onInstallProgress(final InstallInfo installInfo) {
            Log.e(TAG, "install progress: " + installInfo.toString());
            EcuId.EcuEnum ecuEnum = EcuId.getEcuEnumById(installInfo.ecuId);
            String ecuName = QueryEvent.getInstance().getECUName(ecuEnum);
            factory.setInstallProgress(installInfo.totalProgress, ecuName);
        }

        @Override
        public void onInstallEnd(InstallResult installResult) {
            if (installResult.arrRe.length <= 0) {
                // 由于取消升级时也会回调该接口，但是arrRe为空，此时不作任何操作，但重置升级状态
                state = STATE_IDLE;
                return;
            }
            boolean success = true;
            for (ResultInfo info : installResult.arrRe) {
                if (info.result == 0 || info.result == -1) {
                    success = false;
                    break;
                }
            }
            if (success) {
                factory.showCommonDialog(UPDATE_SUCCESS);
                UpdateUtils.sendSystemSettingBroadcast(FotaService.this, true);
            } else {
                factory.showCommonDialog(UPDATE_FAILED);
                UpdateUtils.sendSystemSettingBroadcast(FotaService.this, false);
            }
            state = STATE_IDLE;
            Log.e(TAG, "update result: " + installResult.toString());
        }

        @Override
        public void onError(final ErrorInfo errorInfo) {
            long currentTime = SystemClock.uptimeMillis();
            Log.e(TAG, "on error from tbox: " + errorInfo.toString() + " state: " + state);
            if ((state == STATE_IDLE
                    || state == STATE_CHECK_SILENT)
                    // 忽略开始检测3秒内的所有报错信息
                    && (currentTime - startCheckTime) < DEFAULT_TIMEOUT) {
                SPUtils.getInstance().put(UpdateUtils.UPDATE_STATE, UpdateUtils.LATEST_VERSION);
                Log.e(TAG, "ignore error");
                return;
            }
            // 当前处于检测状态时，不显示错误框，提示为最新版本
            if (state == STATE_CHECK) {
                // 同时将其状态重置为IDLE，避免多次弹出对话框
                state = STATE_IDLE;
                factory.showCommonDialog(LATEST_VERSION);
                SPUtils.getInstance().put(UpdateUtils.UPDATE_STATE, UpdateUtils.LATEST_VERSION);
                Log.e(TAG, "show latest version dialog");
                return;
            }
            state = STATE_IDLE;
            // 过滤掉不应该去关注的错误码
            if (ERROR_WHITELIST.contains(errorInfo.errCode)) {
                return;
            }
            Log.e(TAG, "show error dialog");
            factory.showErrorDialog(errorInfo.errCode);
            handler.removeCallbacks(checkTimeoutRunnable);
        }
    };

    private String getOsVersion() {
        return Build.DISPLAY.split("_")[2];
    }

    private Sysi sysi = new Sysi() {
        // 零件图号
        @Override
        public String getPartNum(EcuId.EcuEnum ecuEnum) {
            SettingsConstantsDef.CustomerId customerId = SettingManager.getInstance().getCustomerId(FotaService.this);
            if (customerId == SettingsConstantsDef.CustomerId.JAC_R3) {
                // R3图号
                return "7911150V5070";
            } else if (customerId == SettingsConstantsDef.CustomerId.JAC_S7) {
                // S7图号(分高低配)
                EndOfLineInfo info = CanManager.getInstance().getEndOfLineInfo();
                if (info.hasElectricTailDoor) {
                    return "7911300U00A4";
                } else {
                    return "7911300U00A3";
                }
            }
            return "";
        }

        @Override
        public String getNodeAddr(EcuId.EcuEnum ecuEnum) {
            return "0x754";
        }

        // 软件版本
        @Override
        public String getSoftwareVersion(EcuId.EcuEnum ecuEnum) {
            return getOsVersion();
        }

        // 目标目录的空间
        @Override
        public int getTargetFreeSpace(EcuId.EcuEnum ecuEnum) {
            return (int) UpdateUtils.getTargetDirFreeSpace();
        }

        // 硬件版本
        @Override
        public String getHardwareVersion(EcuId.EcuEnum ecuEnum) {
            return SettingManager.getInstance().getSettingsInfo().mMcuVersion;
        }

        // 硬件序列号
        @Override
        public String getHardwareSerialNumber(EcuId.EcuEnum ecuEnum) {
            return SourceManager.getInstance().getUuid();
        }

        @Override
        public String getTieroneName(EcuId.EcuEnum ecuEnum) {
            return "L42044";
        }

        // 生产日期
        @Override
        public String getProductionDate(EcuId.EcuEnum ecuEnum) {
            return "";
        }

        @Override
        public void install(HuPackage huPackage) {
            // 系统安装状态监听
            UpdateManager.getInstance().addUpdateListener(updateListener);
            UpdateUtils.installPackage(huPackage.ecuPackage);
        }

        // 升级包校验，0 => 成功， 非0 => 失败
        @Override
        public int verifyPackage(HuPackage huPackage) {
            return 0;
        }

        // 升级包保存到sdcard根目录
        @Override
        public String getTargetPath(EcuId.EcuEnum ecuEnum) {
            return UpdateUtils.getTargetDir();
        }
    };

    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "connect runnable start");
            if (!connected && try_time < TRY_TIMES_MAX) {
                Log.e(TAG, "try connecting to tbox");
                try_time++;
                FotaTask.instance().create(sysi, sessionCallback);
                handler.removeCallbacks(connectRunnable);
                handler.postDelayed(connectRunnable, DEFAULT_TIMEOUT);
            }
        }
    };

    private FotaTask.SessionCallback sessionCallback = session -> {
        Log.e(TAG, session == null ? "session is null" : session.toString());
        if (session != null && session.sessionID == 0x01) {
            Log.e(TAG, "establish connect success");
            connected = true;
            FotaTask.instance().registerListener(globalInter);
        }
    };

    private UpdateManager.IUpdateListener updateListener = new UpdateManager.IUpdateListener() {
        @Override
        public void updateStart(int i) {
            Log.e(TAG, "update start: " + i);
        }

        @Override
        public void updateEnd(int i) {
            Log.e(TAG, "update finish: " + i);
            state = STATE_IDLE;
            factory.dismiss();
            // 极端情况下，该方法被回调但车机仍然未重启，因此需要主动重启车机以完成升级
            UpdateManager.getInstance().reboot();
        }

        /**
         * @param type
         *      0 => os
         *      1 => mcu
         */
        @Override
        public void updateProgerss(int type, int progress) {
            int updateRes = type == 0 ? R.string.update_OS : R.string.update_MCU;
            factory.setInstallProgress(progress, getString(updateRes));
        }

        @Override
        public void updateVerify(int i, int i1) {

        }

        @Override
        public void exception(int i) {
            Log.e(TAG, "update exception: " + i);
            state = STATE_IDLE;
            factory.showCommonDialog(UPDATE_FAILED);
            // 升级失败时，需要通知系统"有新版本"，同时重启车机以复位所有状态
            UpdateUtils.sendSystemSettingBroadcast(FotaService.this, false);
            UpdateManager.getInstance().reboot();
        }

        @Override
        public void updateError(int i, int i1, int i2, int i3, int i4) {
            Log.e(TAG, "update error: " + i + " " + i1 + " " + i2+ " " + i3+ " " + i4);
            state = STATE_IDLE;
            factory.showCommonDialog(UPDATE_FAILED);
            UpdateUtils.sendSystemSettingBroadcast(FotaService.this, false);
            UpdateManager.getInstance().reboot();
        }
    };

    private SettingManager.OnThemeChangedListener themeChangedListener = this::onThemeChanged;

    private void onThemeChanged(String theme) {
        if (theme == null) {
            theme = "colorful";
        }
        if (theme.equals(currentTheme)) {
            return;
        }
        currentTheme = theme;
        switch (theme) {
            case "white":
            case "black":
                SkinCompatManager.getInstance()
                        .loadSkin(theme + ".skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS);
                break;
            default:
                SkinCompatManager.getInstance()
                        .restoreDefaultTheme();
        }
    }

    private IFotaService.Stub proxy = new IFotaService.Stub() {
    };
}