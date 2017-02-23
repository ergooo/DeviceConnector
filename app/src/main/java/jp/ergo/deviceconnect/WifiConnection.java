package jp.ergo.deviceconnect;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.InetAddress;

public class WifiConnection {
    WifiManager wifi;
    String ipAddress;

    /*コンストラクタ*/
    public WifiConnection(Context context){
        wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    }

    //IPアドレスの取得  
    String getIpAddress(){
        int ipAddress_int = wifi.getConnectionInfo().getIpAddress();
        if(ipAddress_int == 0){
            ipAddress = null;
        }else {
            ipAddress = (ipAddress_int & 0xFF) + "." + (ipAddress_int >> 8 & 0xFF) + "." + (ipAddress_int >> 16 & 0xFF) + "." + (ipAddress_int >> 24 & 0xFF);
        }
        return ipAddress;
    }

    //ブロードキャストアドレスの取得    
    InetAddress getBroadcastAddress(){
        DhcpInfo dhcpInfo = wifi.getDhcpInfo();
        int broadcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
        byte[] quads = new byte[4];
        for (int i = 0; i < 4; i++){
            quads[i] = (byte)((broadcast >> i * 8) & 0xFF);
        }
        try {
            return InetAddress.getByAddress(quads);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
