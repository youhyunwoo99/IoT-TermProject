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
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
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


    private WifiManager wifiManager;

    private String serverAddress;
    private String URL;

    private String scanLog;
    private String dbLog;

    private int mode = 0;

    private boolean scanFlag = false;

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
                resultText.setBackgroundColor(Color.parseColor("#000000"));
                resultText.setText("서버 주소와 위치를 입력해주세요.");

            } else {
                URL = serverAddress + "/add";

                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                scanWiFiInfo();
                buttonDisable();
            }
        });

        //getSharedPreferences("파일이름",'모드')
        //모드 => 0 (읽기,쓰기가능)
        //모드 => MODE_PRIVATE (이 앱에서만 사용가능)
        preferences = getSharedPreferences("WiFiDataset", MODE_PRIVATE);

        saveDatasetBtn.setOnClickListener(v -> {
            serverAddress = serverAddressInput.getText().toString();
            positionText = "";
            if (serverAddress.equals("")) {
                resultText.setText("Please input server address");
            } else {
                URL = serverAddress + "/findPosition";
                Log.d("test12", URL);
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                scanWiFiInfo();
                buttonDisable();
            }
        });


        TextView positionLabel = (TextView) findViewById(R.id.positionLabel);
        positionLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positionInput.setText("");
            }
        });

        resultText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = mode == 0 ? 1 : 0;
                resultText.setText(mode == 1 ? "모드 - Key" : "모드 - Key:Value");
                resultText .setBackgroundColor(Color.parseColor("#000000"));
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
            dbLog ="";
            for (ScanResult scanResult : scanResultList) {
                scanLog += "BSSID: " + scanResult.BSSID + "  level: " + scanResult.level + "\n";

                //scanLog += scanResult.toString()+ "\n";
            }

            logTextView.setText(scanLog);
            resultText.setBackgroundColor(Color.parseColor("#0000FF"));
            scanFlag = true;
            buttonEnable();

            // 서버 통신을 별도의 쓰레드에서 처리
            new Thread(() -> {

                // 서버에 보낼 JSON 설정 부분
                try {
                    result_json.put("position", positionText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray json_array = new JSONArray();
                for (ScanResult scanResult : scanResultList) {
                    one_wifi_json = new JSONObject();
                    String bssid = scanResult.BSSID;
                    int rssi = scanResult.level;

                    try {
                        one_wifi_json.put("bssid", bssid);
                        one_wifi_json.put("rssi", rssi);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    json_array.put(one_wifi_json);
                }
                try {
                    result_json.put("wifi_data", json_array);

                    EditText passwordText = findViewById(R.id.userNameInput);
                    result_json.put("password", passwordText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 서버와 통신하는 부분
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    String mRequestBody = result_json.toString(); // json 을 통신으로 보내기위해 문자열로 변환하는 부분

                    Log.d("제발", URL + mRequestBody);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, response -> {
                        String position = response.split("\"")[3];
                        resultText.setText(position); // 결과 출력해주는 부분
                        buttonEnable();
                    }, error -> {
                        resultText.setText("Connection error! Check server address!\n");
                        buttonEnable();
                    }) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public byte[] getBody() { // 요청 보낼 데이터를 처리하는 부분
                            return mRequestBody.getBytes(StandardCharsets.UTF_8);
                        }

                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) { // onResponse 에 넘겨줄 응답을 처리하는 부분
                            String responseString = "";
                            if (response != null) {
                                responseString = new String(response.data, StandardCharsets.UTF_8); // 응답 데이터를 변환해주는 부분
                            }
                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                        }
                    };
                    requestQueue.add(stringRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();



        }
        };

        private void buttonEnable(){
            addDatasetBtn.setText("ADD");
            saveDatasetBtn.setText("FIND");

            addDatasetBtn.setEnabled(true);
            saveDatasetBtn.setEnabled(true);
        }
        private void buttonDisable(){
            addDatasetBtn.setText("--");
            saveDatasetBtn.setText("--");

            addDatasetBtn.setEnabled(false);
            saveDatasetBtn.setEnabled(false);
        }
}