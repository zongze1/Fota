package com.coagent.jac.s7.fota;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.abupdate.iov.task.FotaTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView leftTv;
    private TextView rightTv;
    private Button update;
    private boolean hasCreate = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leftTv = findViewById(R.id.ota_update_left_content);
        rightTv = findViewById(R.id.ota_update_right_content);
        update = findViewById(R.id.ota_update_button);
        rightTv.setOnClickListener(this);
        update.setOnClickListener(this);
        hasCreate = true;

        // 检查标志位
        int state = SPUtils.getInstance().getInt(UpdateUtils.UPDATE_STATE);
        checkState(state);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SPUtils.getInstance().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SPUtils.getInstance().unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hasCreate = false;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
        if (key.equals(UpdateUtils.UPDATE_STATE)) {
            int state = SPUtils.getInstance().getInt(key);
            checkState(state);
        }
    };

    private void checkState(int state) {
        switch (state) {
            case UpdateUtils.NEVER_CHECK:
                leftTv.setVisibility(View.GONE);
                rightTv.setVisibility(View.VISIBLE);
                update.setEnabled(false);
                // 静默检测
                Intent checkVersion = new Intent(this, FotaService.class);
                checkVersion.putExtra("arg_type", 1);
                startService(checkVersion);
                break;
            case UpdateUtils.LATEST_VERSION:
                leftTv.setVisibility(View.VISIBLE);
                rightTv.setVisibility(View.VISIBLE);
                leftTv.setText(R.string.latest_version_content);
                update.setEnabled(false);
                break;
            case UpdateUtils.NEW_VERSION:
                leftTv.setVisibility(View.VISIBLE);
                rightTv.setVisibility(View.GONE);
                leftTv.setText(R.string.new_version_content);
                update.setEnabled(true);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ota_update_button:
                FotaTask.instance().download();
                break;
            case R.id.ota_update_right_content:
                Intent checkVersion = new Intent(this, FotaService.class);
                checkVersion.putExtra("arg_type", 2);
                startService(checkVersion);
                break;
        }
    }
}
