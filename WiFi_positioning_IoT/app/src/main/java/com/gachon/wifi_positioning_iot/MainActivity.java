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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText serverAddressInput;
    private EditText userNameInput;
    private EditText positionInput;
    private String positionText;
    private TextView resultText;
    private TextView logTextView;


    private Button addDatasetBtn;
    private Button saveDatasetBtn;
    private Button checkDatasetBtn;


    private WifiManager wifiManager;

    private String serverAddress;
    private String scanLog;

    JSONObject one_wifi_json = new JSONObject();
    JSONObject result_json = new JSONObject();

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        serverAddressInput = findViewById(R.id.serverAddressInput);
        addDatasetBtn = findViewById(R.id.button);
        saveDatasetBtn = findViewById(R.id.button2);
        checkDatasetBtn = findViewById(R.id.button3);

        positionInput = findViewById(R.id.positionInput);
        resultText = findViewById(R.id.resultText);
        logTextView = findViewById(R.id.app_log);

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
                resultText.setText("서버 주소와 위치를 입력해주세요.");
            } else {
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                scanWiFiInfo();
                buttonDisable();
            }
        });

        //getSharedPreferences("파일이름",'모드')
        //모드 => 0 (읽기,쓰기가능)
        //모드 => MODE_PRIVATE (이 앱에서만 사용가능)
        preferences = getSharedPreferences("WiFiDataset", MODE_PRIVATE);

        //버튼 이벤트
        saveDatasetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonDisable();
                //Editor를 preferences에 쓰겠다고 연결
                SharedPreferences.Editor editor = preferences.edit();
                //putString(KEY,VALUE)
                editor.putString(positionText, scanLog);
                //항상 commit & apply 를 해주어야 저장이 된다.
                editor.commit();
                resultText.setText("데이터셋 저장 완료");
                buttonEnable();
            }
        });

        checkDatasetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonDisable();
                //메소드 호출
                getPreferences();
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


            scanLog = "";
            for (ScanResult scanResult : scanResultList) {
                scanLog += "BSSID: " + scanResult.BSSID + "  level: " + scanResult.level + "\n";
            }
            logTextView.setText(scanLog);
            resultText.setText("데이터셋 스캔 완료");
            buttonEnable();
        }
        };
        //Preferences에서 꺼내오는 메소드
        private void getPreferences(){

            Map<String, ?> allEntries = preferences.getAll();

            int i = 0;
            String msg ="";
            // 키-값 쌍을 순회하며 출력하기
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 키와 값을 출력하거나 원하는 작업 수행
                Log.d("Result #"+i, key + ": " + value.toString());
                msg += "Result #"+i+"\n" + key + ": " + value.toString() + "\n";
                i++;
            }
            logTextView.setText(msg);
            resultText.setText("데이터셋 불러오기 완료");
            buttonEnable();

        }

        private void buttonEnable(){
            addDatasetBtn.setText("ADD");
            saveDatasetBtn.setText("SAVE");
            checkDatasetBtn.setText("CHECK");

            addDatasetBtn.setEnabled(true);
            saveDatasetBtn.setEnabled(true);
            checkDatasetBtn.setEnabled(true);
        }
        private void buttonDisable(){
            addDatasetBtn.setText("--");
            saveDatasetBtn.setText("--");
            checkDatasetBtn.setText("--");

            addDatasetBtn.setEnabled(false);
            saveDatasetBtn.setEnabled(false);
            checkDatasetBtn.setEnabled(false);
        }
}