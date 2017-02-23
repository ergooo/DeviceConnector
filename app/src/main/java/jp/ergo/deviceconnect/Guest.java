package jp.ergo.deviceconnect;


import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Guest {
    private static final String TAG = "Guest";
    boolean waiting;
    int udpPort = 9999;//ホスト、ゲストで統一  
    int tcpPort = 3333;//ホスト、ゲストで統一
    ServerSocket serverSocket;
    Socket socket;

    final WifiConnection wifiConnection;
    
    public Guest(Context context){
        wifiConnection = new WifiConnection(context);
    }
    
    //同一Wi-fiに接続している全端末に対してブロードキャスト送信を行う 
    void sendBroadcast(){
        Log.d(TAG, "sendBroadcast: ");
        final String myIpAddress = wifiConnection.getIpAddress();
        Log.d(TAG, "sendBroadcast: myIpAddress " + myIpAddress);
        waiting = true;
        new Thread() {
            @Override
            public void run() {
                int count = 0;
                //送信回数を10回に制限する  
                while (count < 10) {
                    try {
                        DatagramSocket udpSocket = new DatagramSocket(udpPort);
                        udpSocket.setBroadcast(true);
                        DatagramPacket packet = new DatagramPacket(myIpAddress.getBytes(), myIpAddress.length(), wifiConnection.getBroadcastAddress(), udpPort);
                        Log.d(TAG, "run: packet " + packet);
                        udpSocket.send(packet);
                        udpSocket.close();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //5秒待って再送信を行う  
                    try {
                        Thread.sleep(5000);
                        count++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    
    //ホストからTCPでIPアドレスが返ってきたときに受け取るメソッド  
    void receivedHostIp(){
        Log.d(TAG, "receivedHostIp: ");
        new Thread() {
            @Override
            public void run() {
                while (waiting) {
                    try {
                        if(serverSocket == null) {
                            serverSocket = new ServerSocket(tcpPort);
                        }
                        socket = serverSocket.accept();
                        //↓③で使用  
                        inputDeviceNameAndIp(socket);
                        if (serverSocket != null) {
                            serverSocket.close();
                            serverSocket = null;
                        }
                        if (socket != null) {
                            socket.close();
                            socket = null;
                        }
                    } catch (IOException e) {
                        waiting = false;
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    final SampleDevice hostDevice = new SampleDevice();
    //端末名とIPアドレスのセットを受け取る   
    private void inputDeviceNameAndIp(Socket socket){
        Log.d(TAG, "inputDeviceNameAndIp: ");
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            int infoCounter = 0;
            String remoteDeviceInfo;
            //ホスト端末情報(端末名とIPアドレス)を保持するためのクラスオブジェクト 
            //※このクラスは別途作成しているもの  
            while((remoteDeviceInfo = bufferedReader.readLine()) != null && !remoteDeviceInfo.equals("outputFinish")){
                switch(infoCounter){
                    case 0:
                        Log.d(TAG, "inputDeviceNameAndIp: case 0 remoteDeviceInfo " + remoteDeviceInfo);
                        //1行目、端末名の格納 
                        hostDevice.setDeviceName(remoteDeviceInfo);
                        infoCounter++;
                        break;
                    case 1:
                        Log.d(TAG, "inputDeviceNameAndIp: case 1 remoteDeviceInfo " + remoteDeviceInfo);
                        //2行目、IPアドレスの取得    
                        hostDevice.setDeviceIpAddress(remoteDeviceInfo);
                        infoCounter++;
                        return;
                    default:
                        return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //IPアドレスが判明したホストに対して接続を行う   
    void connect(){
        Log.d(TAG, "connect: ");
        waiting = false;
        new Thread() {
            @Override
            public void run() {
                try{
                    if(socket == null) {
                        Log.d(TAG, "run: remoteIpAddress " + hostDevice.getDeviceIpAddress());
                        socket = new Socket(hostDevice.getDeviceIpAddress(), tcpPort);
                        //この後はホストに対してInputStreamやOutputStreamを用いて入出力を行ったりするが、ここでは割愛        
                    }
                }catch(UnknownHostException e){
                    e.printStackTrace();
                }catch (ConnectException e){
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
