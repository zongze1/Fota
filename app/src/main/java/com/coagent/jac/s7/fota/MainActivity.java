package com.coagent.jac.s7.fota;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, FotaService.class);
        startService(intent);
    }

    public void checkVersion(View view) {
        Intent intent = FotaService.newInstance(this, 1);
        startService(intent);
    }
}
