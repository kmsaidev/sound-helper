package com.eutophia.sound_helper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

import euphony.lib.receiver.AcousticSensor;
import euphony.lib.receiver.EuRxManager;
import euphony.lib.receiver.EuFreqObject;

public class ListenerActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 17389;

    private boolean mode = false;
    private Context mContext;
    private View mainLayout;
    private TextView listenView;
    private Button listenBtn;

    EuRxManager mRxManager = new EuRxManager(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listener);
        mContext = this;
        mainLayout = findViewById(R.id.main_Layout);
        listenView = findViewById(R.id.listen_view);
        listenBtn = findViewById(R.id.button);

        mRxManager.setAcousticSensor(new AcousticSensor() {
            public String byteArrayToHexString(byte[] bytes) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                    builder.append(String.format(i + ":%08B ", bytes[i]));
                }
                return builder.toString();
            }

            public byte[] hexStringToByteArray(String hex) {
                int l = hex.length();
                byte[] data = new byte[l / 2];
                for (int i = 0; i < l; i += 2) {
                    data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                            + Character.digit(hex.charAt(i + 1), 16));
                }
                return data;
            }

            @Override
            public void notify(String letters) {
//                if (letters.indexOf("20") == 0) {
//                    letters = letters.substring(2);
//                }
//                // "AC00" - "D7AF"
//                int ko_min = 0xAC00, ko_max = 0xD7AF;
//
//                for (int i = 0; i < letters.length() - 4; i += 4) {
//                    int tmp = Integer.parseInt(letters.substring(i, i + 4), 16);
//                    if (ko_min <= tmp && tmp <= ko_max ) {
//                    }
//                    else {
//                        i -= 6;
//                    }
//                }
                Log.d("ListenerActivity", letters);
                listenView.setText(letters);
                listenBtn.setText("Listen");
                mode = false;
            }
        });

        listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // recorder request should be checked.
                if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestRecorderPermission();
                }
                else {
                    if(mode) {
                        mRxManager.finish();
                        listenBtn.setText("Listen");
                        mode = false;
                    } else {
                        mRxManager.listen();  //Listening Start
                        listenBtn.setText("Stop");
                        mode = true;
                    }
                }
            }
        });

        requestRecorderPermission();
    }

    private void requestRecorderPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Snackbar.make(mainLayout, R.string.recorder_access_required,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        ActivityCompat.requestPermissions(ListenerActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                    }
                }).show();
            } else {
                Snackbar.make(mainLayout, R.string.recorder_unavailable, Snackbar.LENGTH_SHORT).show();
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(mainLayout, R.string.recorder_permission_granted,
                            Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    Snackbar.make(mainLayout, R.string.recorder_permission_rejected,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
        }
    }
}