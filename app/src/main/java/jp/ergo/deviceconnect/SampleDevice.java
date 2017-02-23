package jp.ergo.deviceconnect;


public class SampleDevice {
    private String deviceName;
    private String deviceIpAddress;

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceIpAddress(String deviceIpAddress) {
        this.deviceIpAddress = deviceIpAddress;
    }

    public String getDeviceIpAddress() {
        return deviceIpAddress;
    }
}
