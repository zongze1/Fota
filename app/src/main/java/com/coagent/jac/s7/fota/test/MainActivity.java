package com.coagent.jac.s7.fota.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.abupdate.iov.Constant.EcuId;
import com.abupdate.iov.Constant.State;
import com.abupdate.iov.event.Query2Callback;
import com.abupdate.iov.event.QueryCallback;
import com.abupdate.iov.event.QueryEvent;
import com.abupdate.iov.event.info.DownloadInfo;
import com.abupdate.iov.event.info.ErrorInfo;
import com.abupdate.iov.event.info.HuPackage;
import com.abupdate.iov.event.info.InstallCondition;
import com.abupdate.iov.event.info.InstallInfo;
import com.abupdate.iov.event.info.InstallResult;
import com.abupdate.iov.event.info.LocalVersionInfo;
import com.abupdate.iov.event.info.Session;
import com.abupdate.iov.event.info.VersionInfo;
import com.abupdate.iov.task.FotaTask;
import com.abupdate.iov.task.GlobalInter;
import com.abupdate.iov.task.Sysi;
import com.coagent.jac.s7.fota.FotaService;
import com.coagent.jac.s7.fota.R;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "====Fota====";
    /**
     * 保存当前模式，0 => 普通模式，1 => UI模式，重复点击同一个模式切换按钮则是重连操作
     */
    private TextView modeTv;

    private LogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> logs = new ArrayList<String>();
        adapter = new LogAdapter(logs);
        RecyclerView logRv = (RecyclerView) findViewById(R.id.rv);
        if (logRv == null) {
            return;
        }
        logRv.setLayoutManager(new LinearLayoutManager(this));
        logRv.setAdapter(adapter);

        BroadcastManager.registerBroadcast(this, receiver);
        // 更新so库
        copySo2UserLib();

        Intent intent = new Intent(this, FotaService.class);
        stopService(intent);
        modeTv = (TextView) findViewById(R.id.mode);
        modeTv.setTag(1);
        // 默认启动UI模式
        turnOnUiMode(null);
    }

    public void clearList(View view) {
        adapter.setNewData(new ArrayList<String>());
    }

    public void turnOnNormalMode(View view) {
        Integer mode = (Integer) modeTv.getTag();
        if (mode == 0) {
            // 重连
            FotaTask.instance().destroy();
        } else {
            Intent intent = new Intent(this, FotaService.class);
            startService(intent);
        }
        modeTv.setTag(0);
        modeTv.setText(R.string.no_ui_mode);
        FotaTask.instance().create(sysi, callback);
        FotaTask.instance().registerListener(globalInter);

        findViewById(R.id.ui_mode_check_version).setVisibility(View.GONE);
        findViewById(R.id.no_ui_mode_check_version).setVisibility(View.VISIBLE);
        findViewById(R.id.download).setVisibility(View.VISIBLE);
        findViewById(R.id.download_cancel).setVisibility(View.VISIBLE);
        findViewById(R.id.install).setVisibility(View.VISIBLE);
        findViewById(R.id.set_deferred_upgrade_plan).setVisibility(View.VISIBLE);
        findViewById(R.id.get_state).setVisibility(View.VISIBLE);
        findViewById(R.id.get_local_version_info).setVisibility(View.VISIBLE);
        findViewById(R.id.get_SDK_version).setVisibility(View.VISIBLE);
        findViewById(R.id.get_version_info).setVisibility(View.VISIBLE);
        findViewById(R.id.get_ECU_name).setVisibility(View.VISIBLE);
    }

    public void turnOnUiMode(View view) {
        Integer mode = (Integer) modeTv.getTag();
        if (mode == 1) {
            // 重连
            Intent intent = FotaService.newInstance(this, 1);
            stopService(intent);
            startService(intent);
        } else {
            FotaTask.instance().destroy();
        }
        modeTv.setTag(1);
        modeTv.setText(R.string.ui_mode);

        findViewById(R.id.ui_mode_check_version).setVisibility(View.VISIBLE);
        findViewById(R.id.no_ui_mode_check_version).setVisibility(View.GONE);
        findViewById(R.id.download).setVisibility(View.GONE);
        findViewById(R.id.download_cancel).setVisibility(View.GONE);
        findViewById(R.id.install).setVisibility(View.GONE);
        findViewById(R.id.set_deferred_upgrade_plan).setVisibility(View.GONE);
        findViewById(R.id.get_state).setVisibility(View.GONE);
        findViewById(R.id.get_local_version_info).setVisibility(View.GONE);
        findViewById(R.id.get_SDK_version).setVisibility(View.GONE);
        findViewById(R.id.get_version_info).setVisibility(View.GONE);
        findViewById(R.id.get_ECU_name).setVisibility(View.GONE);
    }

    // 将sdcard路径下的so库拷贝到用户目录并读取
    @SuppressLint("UnsafeDynamicallyLoadedCode")
    private void copySo2UserLib() {
        File dir = getDir("libs", Activity.MODE_PRIVATE);
        String iovPath = Environment.getExternalStorageDirectory() + File.separator + "libc_iov.so";
        File destIovFile = new File(dir.getAbsolutePath() + File.separator + "libc_iov.so");
        String frUePath = Environment.getExternalStorageDirectory() + File.separator + "libfr_ue.so";
        File destFrUeFile = new File(dir.getAbsolutePath() + File.separator + "libfr_ue.so");

        if (Utils.copyLibraryFile(iovPath, destIovFile.getAbsolutePath()) &&
                Utils.copyLibraryFile(frUePath, destFrUeFile.getAbsolutePath())) {
            BroadcastManager.sendLogBroadcast(this, "so库替换成功，" +
                    "新c_iov.so库体积为：" + destIovFile.length() + "字节" +
                    " 新fr_ue.so库体积为: " + destFrUeFile.length() + "字节");
            System.load(destIovFile.getAbsolutePath());
            System.load(destFrUeFile.getAbsolutePath());
        } else {
            BroadcastManager.sendLogBroadcast(this, "so库替换失败，请将要替换的so库文件放置在/sdcard/目录下，且命名为libfr_ue.so和libc_iov.so");
        }
    }

    private void log(Object object) {
        if (object == null) {
            logActual("object is null");
            return;
        }
        StringBuilder builder = new StringBuilder();
        String value;
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                value = String.valueOf(field.get(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                value = "an error occured";
            }
            if (builder.length() != 0) {
                builder.append("\n");
            }
            builder.append(field.getName())
                    .append(": ")
                    .append(value);
        }
        logActual(builder.toString());
    }

    private void logActual(String log) {
        Log.d(TAG, log);
        BroadcastManager.sendLogBroadcast(this, log);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String log = BroadcastManager.getLog(intent);
            Log.d(TAG, log);
            adapter.addData(log);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        FotaTask.instance().destroy();
    }

    public void ui_mode_checkVersion(View view) {
        Intent intent = FotaService.newInstance(this, 1);
        startService(intent);
    }

    public void no_ui_mode_checkVersion(View view) {
        logActual("start check version");
        FotaTask.instance().checkVersion();
    }

    public void download(View view) {
        logActual("start download");
        FotaTask.instance().download();
    }

    public void downloadCancel(View view) {
        logActual("cancel download");
        FotaTask.instance().downloadCancel();
    }

    public void install(View view) {
        logActual("start install");
        FotaTask.instance().install();
    }

    public void setDeferredUpgradePlan(View view) {
        logActual("set deferred upgrade plan for 10 seconds");
        FotaTask.instance().setDeferredUpgradePlan(10);
    }

    public void getState(View view) {
        QueryEvent.getInstance().getState(new QueryCallback<State.Status>() {
            @Override
            public void onReceive(State.Status status) {
                log(status);
            }
        });
    }

    public void getLocalVersionInfo(View view) {
        QueryEvent.getInstance().getLocalVersionInfo(new QueryCallback<LocalVersionInfo>() {
            @Override
            public void onReceive(LocalVersionInfo versionInfo) {
                log(versionInfo);
            }
        });
    }

    public void getSDKVersion(View view) {
        QueryEvent.getInstance().getSDKVersion(new QueryCallback<String>() {
            @Override
            public void onReceive(String sdkVersion) {
                logActual(sdkVersion);
            }
        });
    }

    public void getVersionInfo(View view) {
        QueryEvent.getInstance().getVersionInfo(new QueryCallback<VersionInfo>() {
            @Override
            public void onReceive(VersionInfo versionInfo) {
                log(versionInfo);
            }
        });
    }

    public void getECUName(View view) {
        for (EcuId.EcuEnum ecuEnum : EcuId.EcuEnum.values()) {
            QueryEvent.getInstance().getECUName(ecuEnum, new Query2Callback<EcuId.EcuEnum, String>() {
                @Override
                public void onReceive(EcuId.EcuEnum ecuEnum, String s) {
                    logActual(ecuEnum.name() + s);
                }
            });
        }
    }

    private Sysi sysi = new Sysi() {
        @Override
        public String getPartNum(EcuId.EcuEnum ecuEnum) {
            logActual(Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + ecuEnum.name());
            return "7911150U3401";
        }

        @Override
        public String getNodeAddr(EcuId.EcuEnum ecuEnum) {
            logActual(Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + ecuEnum.name());
            return "0x754";
        }

        @Override
        public String getSoftwareVersion(EcuId.EcuEnum ecuEnum) {
            logActual(Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + ecuEnum.name());
            return "V8_JAC_S7_V1.07";
        }

        @Override
        public int getTargetFreeSpace(EcuId.EcuEnum ecuEnum) {
            logActual(Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + ecuEnum.name());
            return 100000;
        }

        @Override
        public String getHardwareVersion(EcuId.EcuEnum ecuEnum) {
            logActual(Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + ecuEnum.name());
            return "HN4R06A";
        }

        @Override
        public String getHardwareSerialNumber(EcuId.EcuEnum ecuEnum) {
            logActual(Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + ecuEnum.name());
            return "65462315";
        }

        @Override
        public String getTieroneName(EcuId.EcuEnum ecuEnum) {
            logActual(Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + ecuEnum.name());
            return "ADAYO";
        }

        @Override
        public String getProductionDate(EcuId.EcuEnum ecuEnum) {
            logActual(Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + ecuEnum.name());
            DateFormat format = new SimpleDateFormat("yyyyMM", Locale.CHINA);
            return format.format(new Date());
        }

        @Override
        public void install(HuPackage huPackage) {
            log(huPackage);
        }

        @Override
        public int verifyPackage(HuPackage huPackage) {
            log(huPackage);
            return 1;
        }

        @Override
        public String getTargetPath(EcuId.EcuEnum ecuEnum) {
            logActual(Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + ecuEnum.name());
            return File.separator + "sdcard" + File.separator;
        }
    };

    private FotaTask.SessionCallback callback = new FotaTask.SessionCallback() {
        @Override
        public void receiver(Session session) {
//            log(session);
        }
    };

    private GlobalInter globalInter = new GlobalInter() {
        @Override
        public void onNewVersion(VersionInfo versionInfo) {
            log(versionInfo);
        }

        @Override
        public void onDownloadProgress(DownloadInfo downloadInfo) {
            log(downloadInfo);
        }

        @Override
        public void onInstallCondition(InstallCondition installCondition) {
            log(installCondition);
        }

        @Override
        public void onInstallProgress(InstallInfo installInfo) {
            log(installInfo);
        }

        @Override
        public void onInstallEnd(InstallResult installResult) {
            log(installResult);
        }

        @Override
        public void onError(ErrorInfo errorInfo) {
            log(errorInfo);
        }
    };
}
