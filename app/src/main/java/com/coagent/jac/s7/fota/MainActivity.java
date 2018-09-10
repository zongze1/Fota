package com.coagent.jac.s7.fota;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.coagent.jac.s7.fota.Dialog.MessageDialog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private MessageDialog messageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, FotaService.class);
        startService(intent);
    }
}
