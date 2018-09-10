package com.coagent.jac.s7.fota;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.abupdate.iov.Constant.EcuId;
import com.abupdate.iov.event.info.DownloadInfo;
import com.abupdate.iov.event.info.ErrorInfo;
import com.abupdate.iov.event.info.HuPackage;
import com.abupdate.iov.event.info.InstallCondition;
import com.abupdate.iov.event.info.InstallInfo;
import com.abupdate.iov.event.info.InstallResult;
import com.abupdate.iov.event.info.Session;
import com.abupdate.iov.event.info.VersionInfo;
import com.abupdate.iov.task.FotaTask;
import com.abupdate.iov.task.GlobalInter;
import com.abupdate.iov.task.Sysi;
import com.coagent.jac.s7.fota.Dialog.ConditionDialog;
import com.coagent.jac.s7.fota.Dialog.MessageDialog;
import com.coagent.jac.s7.fota.Dialog.ProgressDialog;
import com.coagent.jac.s7.fota.Dialog.ProgressListener;
import com.coagent.jac.s7.fota.Dialog.ScheduleListener;
import com.coagent.jac.s7.fota.Dialog.UpdateDialogListener;
import com.coagent.jac.s7.fota.Dialog.UpdateNoteDialog;
import com.coagent.jac.s7.fota.Dialog.UpdateScheduleDialog;
import com.coagent.jac.s7.fota.Dialog.UpdateWarningDialog;
import com.coagent.proxy.update.UpdateManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 由于各个对话框之间相互耦合，暂时未作优化
 */
public class FotaService extends Service {
    private static final String ARG_TYPE = "arg_type";
    /**
     * 记录升级的id，当收到新版本通知时保存任务id到本地
     * 若下次收到新版本通知，则检测本地是否有保存该任务id，若有则不显示对话框
     * 若没有，则显示对话框，并覆盖本地保存的id值
     */
    private static final String SP_UPDATE_TASK_ID = "sp_update_task_id";

    // 消息对话框，可以设置按钮数量
    private MessageDialog messageDialog;
    // 下载或者安装进度条对话框
    private ProgressDialog progressDialog;
    // 条款说明对话框
    private UpdateNoteDialog noteDialog;
    // 设置静默升级时间对话框
    private UpdateScheduleDialog scheduleDialog;
    // 主动升级安装前提示对话框
    private UpdateWarningDialog warningDialog;
    // 升级前条件检测对话框
    private ConditionDialog conditionDialog;

    private Handler handler = new Handler();

    /**
     * 启动升级服务
     *
     * @param type 0 => 正常启动，启动后不执行任何操作
     *             1 => 启动后执行安装操作
     */
    public static Intent newInstance(Context context, int type) {
        Intent intent = new Intent(context, FotaService.class);
        intent.putExtra(ARG_TYPE, type);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FotaTask.instance().create(sysi, sessionCallback);
        FotaTask.instance().registerListener(globalInter);
        UpdateManager.getInstance().addUpdateListener(updateListener);

        VersionInfo info = new VersionInfo();
        info.taskId = "1";
        info.packageSize = 2688;
        info.upgradeType = 1;
        info.fromTime = "2018-9-7";
        info.toTime = "2018-9-9";
        info.releaseNote = "测试";
        globalInter.onNewVersion(info);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FotaTask.instance().destroy();
        UpdateManager.getInstance().removeUpdateListener(updateListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 获取Tbox传输过来文件要存档的目录，从root目录开始
    private String getTargetDir() {
        return File.separator + "sdcard" + File.separator;
    }

    private void installPackage(final String filePath) {
        // 直接开线程解压就行了，因为这里不允许有任何用户操作干预
        new Thread(new Runnable() {
            @Override
            public void run() {
                File dir = new File(getTargetDir());
                File zipFile = new File(filePath);
                // 解压文件
                List<File> files;
                try {
                    files = Utils.unzipFile(zipFile, dir);
                } catch (IOException e) {
                    e.printStackTrace();
                    files = new ArrayList<File>();
                }
                // 遍历识别update.zip文件名的升级包
                File updateFile = null;
                for (File file : files) {
                    if (file.getName().equals("update.zip")) {
                        updateFile = file;
                        break;
                    }
                }
                if (updateFile != null) {
                    UpdateManager.getInstance().updateOS(updateFile.getAbsolutePath());
                }
            }
        }).start();
    }

    private void createNoteDialog() {
        if (noteDialog == null) {
            noteDialog = new UpdateNoteDialog(this);
        } else {
            noteDialog.dismiss();
        }
    }

    private void createProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        } else {
            progressDialog.dismiss();
        }
    }

    private void createMessageDialog() {
        if (messageDialog == null) {
            messageDialog = new MessageDialog(this, "");
        } else {
            messageDialog.dismiss();
        }
    }

    private void createScheduleDialog() {
        if (scheduleDialog == null) {
            scheduleDialog = new UpdateScheduleDialog(this);
        } else {
            scheduleDialog.dismiss();
        }
    }

    private void createUpdateWarningDialog() {
        if (warningDialog == null) {
            warningDialog = new UpdateWarningDialog(this, handler);
        } else {
            warningDialog.dismiss();
        }
    }

    private void createConditionDialog() {
        if (conditionDialog == null) {
            conditionDialog = new ConditionDialog(this, handler);
        } else {
            conditionDialog.dismiss();
        }
    }

    private GlobalInter globalInter = new GlobalInter() {
        @Override
        public void onNewVersion(VersionInfo versionInfo) {
            String dstVer = SPUtils.getInstance().getString(SP_UPDATE_TASK_ID);
            if (dstVer.equals(versionInfo.taskId)) {
                return;
            }

            // 收到新版本提示
            createMessageDialog();
            messageDialog.setCanceledOnTouchOutside(false);
            messageDialog.setTitle(getString(R.string.check_version_title))
                    .setContent(getString(R.string.check_version_content))
                    .setButtonCount(2)
                    .setPositiveText(getString(R.string.update_now))
                    .setNegativeText(getString(R.string.update_schedule))
                    .setListener(newVersionListener)
                    .show();
            // 预先填充条款对话框的说明
            createNoteDialog();
            noteDialog.setContent(versionInfo.releaseNote);
            SPUtils.getInstance().put(SP_UPDATE_TASK_ID, versionInfo.taskId);
        }

        private UpdateDialogListener newVersionListener = new UpdateDialogListener() {
            @Override
            public void onPositiveClick() {
                // 点击"现在升级"按钮
                // 启动下载对话框
                messageDialog.dismiss();
                noteDialog.setListener(activeListener)
                        .show();
            }

            @Override
            public void onNegativeClick() {
                // 点击"预定时间"按钮
                messageDialog.dismiss();
                noteDialog.setListener(silentListener)
                        .show();
            }
        };

        // 条款对话框主动升级listener
        private UpdateDialogListener activeListener = new UpdateDialogListener() {
            @Override
            public void onPositiveClick() {
                // 同意条款则开始下载升级包
                noteDialog.dismiss();
                createProgressDialog();
                progressDialog.setTitle(getString(R.string.downloading))
                        .setListener(progressListener)
                        .show();
                FotaTask.instance().download();
            }

            @Override
            public void onNegativeClick() {
                noteDialog.dismiss();
                // TODO 通知Launcher显示待升级图标
            }
        };

        // 条款对话框静默升级listener
        private UpdateDialogListener silentListener = new UpdateDialogListener() {
            @Override
            public void onPositiveClick() {
                // 同意条款则打开计划时间设置对话框
                createScheduleDialog();
                scheduleDialog.setListener(scheduleListener)
                        .show();
            }

            @Override
            public void onNegativeClick() {
                noteDialog.dismiss();
                // TODO 通知Launcher显示待升级图标
            }
        };

        private ScheduleListener scheduleListener = new ScheduleListener() {
            @Override
            public void schedule(long timeMillis) {
                Calendar calendar = Calendar.getInstance();
                int seconds = (int) ((calendar.getTimeInMillis() - timeMillis) / 1000);
                FotaTask.instance().setDeferredUpgradePlan(seconds);
            }
        };

        private ProgressListener progressListener = new ProgressListener() {
            @Override
            public void onCancel() {
                createMessageDialog();
                // 更新信息对话框的内容并显示
                messageDialog.setTitle("")
                        .setContent(getString(R.string.cancel_download_content))
                        .setPositiveText(getString(R.string.cancel_download))
                        .setNegativeText(getString(R.string.continue_download))
                        .setListener(cancelDownloadListener)
                        .show();
            }

            @Override
            public void onComplete() {
                // 下载完成，弹出安装前提示
                progressDialog.dismiss();
                createUpdateWarningDialog();
                warningDialog.setListener(downloadCompleteListener)
                        .show();
            }
        };

        private UpdateDialogListener cancelDownloadListener = new UpdateDialogListener() {
            @Override
            public void onPositiveClick() {
                // 退出下载，隐藏信息对话框及进度对话框
                FotaTask.instance().downloadCancel();
                messageDialog.dismiss();
                progressDialog.setProgress(0)
                        .dismiss();
            }

            @Override
            public void onNegativeClick() {
                // 继续下载
                messageDialog.dismiss();
            }
        };

        private UpdateDialogListener downloadCompleteListener = new UpdateDialogListener() {
            @Override
            public void onPositiveClick() {
                warningDialog.dismiss();
                // 点击"现在升级"按钮
                // 通知fota检测车机是否符合安装条件
                createConditionDialog();
                conditionDialog.setListener(checkConditionListener)
                        .show();
                FotaTask.instance().install();
            }

            @Override
            public void onNegativeClick() {
                warningDialog.dismiss();
                // TODO 通知Launcher显示待升级图标
            }
        };

        @Override
        public void onDownloadProgress(DownloadInfo downloadInfo) {
            if (!progressDialog.isShowing()) {
                return;
            }
            float progress;
            progress = downloadInfo.currentNum / downloadInfo.countNum;
            progress *= downloadInfo.nowBytes / downloadInfo.totalBytes;
            progressDialog.setProgress(progress);
        }

        @Override
        public void onInstallCondition(InstallCondition installCondition) {
            // 先检测电量，若不通过，显示错误信息框
            if (!installCondition.isBatteryOk) {
                conditionDialog.dismiss();
                createMessageDialog();
                messageDialog.setTitle("")
                        .setContent(getString(R.string.no_power))
                        .setButtonCount(1)
                        .setNegativeText(getString(R.string.confirm))
                        .setListener(messageDialogListener)
                        .show();
                return;
            }

            // 更新安装条件
            if (conditionDialog.isShowing()) {
                conditionDialog.changeCondition(installCondition);
            } else {
                conditionDialog.setCondition(installCondition);
            }
        }

        private UpdateDialogListener checkConditionListener = new UpdateDialogListener() {
            @Override
            public void onPositiveClick() {
                // 显示安装进度条
                createProgressDialog();
                progressDialog.setTitle(getString(R.string.installing))
                        .show();
            }

            @Override
            public void onNegativeClick() {
                // 存在未通过条件，取消升级
                conditionDialog.dismiss();
            }
        };

        @Override
        public void onInstallProgress(InstallInfo installInfo) {

        }

        @Override
        public void onInstallEnd(InstallResult installResult) {

        }

        @Override
        public void onError(ErrorInfo errorInfo) {
            createMessageDialog();
            messageDialog.setTitle("")
                    .setContent(errorInfo.desc)
                    .setButtonCount(1)
                    .setNegativeText(getString(R.string.confirm))
                    .setListener(messageDialogListener)
                    .show();
        }

        private UpdateDialogListener messageDialogListener = new UpdateDialogListener() {
            @Override
            public void onPositiveClick() {

            }

            @Override
            public void onNegativeClick() {
                messageDialog.dismiss();
            }
        };
    };

    private Sysi sysi = new Sysi() {
        // 零件图号
        @Override
        public String getPartNum(EcuId.EcuEnum ecuEnum) {
            return "7911150U3401";
        }

        // 设备节点地址，诊断id
        @Override
        public String getNodeAddr(EcuId.EcuEnum ecuEnum) {
            return "0x754";
        }

        // 软件版本
        @Override
        public String getSoftwareVersion(EcuId.EcuEnum ecuEnum) {
            return "V8_JAC_S7_V1.08";
        }

        // 目标目录的空间
        @Override
        public int getTargetFreeSpace(EcuId.EcuEnum ecuEnum) {
            String targetDir = getTargetDir();
            File dir = new File(targetDir);
            return (int) dir.getFreeSpace();
        }

        // 硬件版本
        @Override
        public String getHardwareVersion(EcuId.EcuEnum ecuEnum) {
            return "HN4R06A";
        }

        // 硬件序列号
        @Override
        public String getHardwareSerialNumber(EcuId.EcuEnum ecuEnum) {
            return Build.SERIAL;
        }

        // 供应商编码
        @Override
        public String getTieroneName(EcuId.EcuEnum ecuEnum) {
            return "ADAYO";
        }

        // 生产日期
        @Override
        public String getProductionDate(EcuId.EcuEnum ecuEnum) {
            return "20180821";
        }

        @Override
        public void install(HuPackage huPackage) {
            installPackage(huPackage.ecuPackage);
        }

        // 升级包校验，0 => 成功， 非0 => 失败
        @Override
        public int verifyPackage(HuPackage huPackage) {
            return 0;
        }

        // 升级包保存到sdcard根目录
        @Override
        public String getTargetPath(EcuId.EcuEnum ecuEnum) {
            return getTargetDir();
        }
    };

    private FotaTask.SessionCallback sessionCallback = new FotaTask.SessionCallback() {
        @Override
        public void receiver(Session session) {

        }
    };

    private UpdateManager.IUpdateListener updateListener = new UpdateManager.IUpdateListener() {
        @Override
        public void updateStart(int i) {

        }

        @Override
        public void updateEnd(int i) {
            progressDialog.dismiss();
            createMessageDialog();
            messageDialog.setTitle("")
                    .setContent(getString(R.string.update_success))
                    .setButtonCount(0)
                    .show();
        }

        @Override
        public void updateProgerss(int i, int i1) {
            progressDialog.setProgress(i);
        }

        @Override
        public void updateVerify(int i, int i1) {

        }

        @Override
        public void exception(int i) {

        }

        @Override
        public void updateError(int i, int i1, int i2, int i3, int i4) {
            createMessageDialog();
            messageDialog.setTitle("")
                    .setContent(getString(R.string.update_failed))
                    .setButtonCount(0)
                    .show();
        }
    };
}
