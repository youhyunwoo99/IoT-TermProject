package com.gachon.wifi_positioning_iot;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText serverAddressInput;
    private EditText userNameInput;
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
    }
}