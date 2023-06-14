package com.gachon.wifi_positioning_iot;

public class RssiItem {
    String name;
    String bssid;
    String rssi;

    public RssiItem(String name, String bssid, String rssi) {
        this.name = name;
        this.bssid= bssid;
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    public String getBssid() {
        return bssid;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBssid(String bssi) {
        this.bssid = bssid;
    }
}