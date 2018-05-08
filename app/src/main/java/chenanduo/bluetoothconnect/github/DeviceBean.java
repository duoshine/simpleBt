package chenanduo.bluetoothconnect.github;

/**
 * Created by chen on 2017
 */

public class DeviceBean {
    private String name;

    private String address;

    private int rssi;

    private String scanRecord;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(String scanRecord) {
        this.scanRecord = scanRecord;
    }
}
