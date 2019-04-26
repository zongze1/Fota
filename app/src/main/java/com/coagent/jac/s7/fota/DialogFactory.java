package com.coagent.jac.s7.fota;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.abupdate.iov.event.info.InstallCondition;
import com.coagent.jac.s7.fota.base.BaseDialog;
import com.coagent.jac.s7.fota.dialog.ConditionDialog;
import com.coagent.jac.s7.fota.dialog.DownloadProgressDialog;
import com.coagent.jac.s7.fota.dialog.ErrorDialog;
import com.coagent.jac.s7.fota.dialog.InstallProgressDialog;
import com.coagent.jac.s7.fota.dialog.LoadingDialog;
import com.coagent.jac.s7.fota.dialog.LatestVersionDialog;
import com.coagent.jac.s7.fota.dialog.NewVersionDialog;
import com.coagent.jac.s7.fota.dialog.NoPowerDialog;
import com.coagent.jac.s7.fota.base.ProgressDialog;
import com.coagent.jac.s7.fota.dialog.UpdateFailedDialog;
import com.coagent.jac.s7.fota.dialog.UpdateNoteDialog;
import com.coagent.jac.s7.fota.dialog.UpdateScheduleDialog;
import com.coagent.jac.s7.fota.dialog.UpdateSuccessDialog;
import com.coagent.jac.s7.fota.dialog.UpdateConfirmDialog;
import com.coagent.jac.s7.fota.features.EndDialog;

import static com.coagent.jac.s7.fota.Utils.TAG;

/**
 * 负责生成各种对话框
 * 并管理其生命周期
 * 保证多个对话框不会重叠出现
 */
public class DialogFactory {
    /* 检测等待 */
    public static final int LOADING = 1;
    /* 检查到新版本 */
    public static final int NEW_VERSION = 2;
    /* 已是最新版本 */
    public static final int LATEST_VERSION = 3;
    /* 升级免责声明 */
    public static final int RELEASE_NOTE = 4;
    /* 预约升级 */
    public static final int SCHEDULE = 5;
    /* 升级条件检查 */
    public static final int CONDITION = 6;
    /* 升级前确认 */
    public static final int UPDATE_CONFIRM = 7;
    /* 电量不足 */
    public static final int NO_POWER = 8;
    /* 升级成功 */
    public static final int UPDATE_SUCCESS = 9;
    /* 升级失败 */
    public static final int UPDATE_FAILED = 10;

    private Context context;
    private Handler handler;
    private BaseDialog dialog;
    private ProgressDialog progressDialog;
    private UpdateInfo updateInfo = new UpdateInfo();

    public DialogFactory(Context context, Handler handler) {
        this.context = context.getApplicationContext();
        this.handler = handler;
    }

    public UpdateInfo getUpdateInfo() {
        return updateInfo;
    }

    public void showCommonDialog(int type) {
        Log.e(TAG, "show dialog of type:" + type);
        handler.postDelayed(() -> {
            switch (type) {
                case LOADING:
                    dismiss();
                    dialog = new LoadingDialog(context, DialogFactory.this);
                    dialog.show();
                    break;
                case NEW_VERSION:
                    dismiss();
                    dialog = new NewVersionDialog(context, DialogFactory.this);
                    dialog.show();
                    break;
                case LATEST_VERSION:
                    dismiss();
                    dialog = new LatestVersionDialog(context, DialogFactory.this);
                    dialog.show();
                    break;
                case RELEASE_NOTE:
                    dismiss();
                    dialog = new UpdateNoteDialog(context, DialogFactory.this);
                    progressDialog = new DownloadProgressDialog(context, DialogFactory.this);
                    dialog.show();
                    break;
                case SCHEDULE:
                    dismiss();
                    dialog = new UpdateScheduleDialog(context, DialogFactory.this);
                    dialog.show();
                    break;
                case UPDATE_CONFIRM:
                    dismiss();
                    dialog = new UpdateConfirmDialog(context, DialogFactory.this, handler);
                    dialog.show();
                    break;
                case CONDITION:
                    dismiss();
                    dialog = new ConditionDialog(context, DialogFactory.this, handler);
                    progressDialog = new InstallProgressDialog(context, DialogFactory.this);
                    dialog.show();
                    break;
                case NO_POWER:
                    dismiss();
                    dialog = new NoPowerDialog(context, DialogFactory.this);
                    dialog.show();
                    break;
                case UPDATE_SUCCESS:
                    if (dialog instanceof EndDialog && dialog.isShowing()) {
                        return;
                    }
                    dismiss();
                    UpdateSuccessDialog successDialog = new UpdateSuccessDialog(context, DialogFactory.this);
                    successDialog.show();
                    break;
                case UPDATE_FAILED:
                    if (dialog instanceof EndDialog && dialog.isShowing()) {
                        return;
                    }
                    dismiss();
                    UpdateFailedDialog failedDialog = new UpdateFailedDialog(context, DialogFactory.this);
                    failedDialog.show();
                    break;
            }
        }, 100);
    }

    public void showErrorDialog(final int errorCode) {
        dismiss();
        handler.post(() -> {
            dialog = new ErrorDialog(context, DialogFactory.this, errorCode);
            dialog.show();
        });
        handler.postDelayed(this::dismiss, 5000);
    }

    public void setDownloadProgress(final float progress) {
        handler.post(() -> {
            if (progressDialog == null) {
                progressDialog = new DownloadProgressDialog(context, DialogFactory.this);
            }
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
            progressDialog.setProgress(progress);
        });
    }

    public void setInstallProgress(final float progress, final String ecuName) {
        handler.post(() -> {
            if (progressDialog == null) {
                progressDialog = new InstallProgressDialog(context, DialogFactory.this);
            }
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
            float percent = progress / 100f;
            progressDialog.setProgress(percent);
            ((InstallProgressDialog) progressDialog).setEcuTv(ecuName);
            ((InstallProgressDialog) progressDialog).setProgressTv(progress);
        });
    }

    /**
     * 检测当前是否正在安装，当前该方式是通过检测进度框是否显示
     */
    public boolean isInstalling() {
        return progressDialog != null && progressDialog.isShowing();
    }

    public void changeCondition(final InstallCondition condition) {
        handler.post(() -> {
            if (dialog == null) {
                dialog = new ConditionDialog(context, DialogFactory.this, handler);
            }
            if (!dialog.isShowing()) {
                dialog.show();
            }
            if (dialog instanceof ConditionDialog) {
                ((ConditionDialog) dialog).changeCondition(condition);
            }
        });
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    public void release() {
        dismiss();
        updateInfo.release();
        handler = null;
        context = null;
    }
}
