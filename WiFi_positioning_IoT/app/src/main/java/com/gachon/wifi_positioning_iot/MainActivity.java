package com.gachon.wifi_positioning_iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText serverAddressInput;
    private EditText userNameInput;
    private EditText positionInput;
    private String positionText;
    private TextView resultText;

    private Button addDatasetBtn;

    private WifiManager wifiManager;

    private String serverAddress;
    private String URL;

    JSONObject one_wifi_json = new JSONObject();
    JSONObject result_json = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        serverAddressInput = findViewById(R.id.serverAddressInput);
        addDatasetBtn = findViewById(R.id.button);
        positionInput = findViewById(R.id.positionInput);
        resultText = findViewById(R.id.resultText);

        // WiFi Scan을 위한 권한 요청
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("Permission", "권한 없음");
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.d("Permission", "Permission is not granted, but can be requested");
            } else {
                Log.d("Permission", "권한 요청");
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.CHANGE_WIFI_STATE},
                        1000);
            }
        }

        addDatasetBtn.setOnClickListener(v -> {
            serverAddress = serverAddressInput.getText().toString();
            positionText = positionInput.getText().toString();
            if (serverAddress.equals("") || positionText.equals("")) {
                resultText.setText("Please input server address and position");
            } else {
                URL = serverAddress + "/add";
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                scanWiFiInfo();
                addDatasetBtn.setEnabled(false);
            }
        });
    }

    private void scanWiFiInfo() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            List<ScanResult> scanResultList = wifiManager.getScanResults();
            unregisterReceiver(this);

            scanResultList.sort((s1, s2) -> s2.level - s1.level);


            TextView logTextView = findViewById(R.id.app_log);
            String scanLog = "";
            for (ScanResult scanResult : scanResultList) {
                scanLog += "BSSID: " + scanResult.BSSID + "  level: " + scanResult.level + "\n";
            }
            logTextView.setText(scanLog);





        }
    }
}