package jp.ergo.deviceconnect;


import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Host {
    private static final String TAG = "Host";
    DatagramSocket receiveUdpSocket;
    boolean waiting;
    int udpPort = 9999;//ホスト、ゲストで統一  


    ServerSocket serverSocket;
    Socket connectedSocket;
    int tcpPort = 3333;//ホスト、ゲストで統一  

    final WifiConnection wifiConnection;
    public Host(Context context){
        wifiConnection = new WifiConnection(context);
    }
    
    //ブロードキャスト受信用ソケットの生成   
    //ブロードキャスト受信待ち状態を作る  
    void createReceiveUdpSocket() {
        Log.d(TAG, "createReceiveUdpSocket: ");
        waiting = true;
        new Thread() {
            @Override
            public void run() {
                String address = null;
                try {
                    //waiting = trueの間、ブロードキャストを受け取る
                    while (waiting) {
                        //受信用ソケット
                        DatagramSocket receiveUdpSocket = new DatagramSocket(udpPort);
                        byte[] buf = new byte[256];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        //ゲスト端末からのブロードキャストを受け取る  
                        //受け取るまでは待ち状態になる   
                        Log.d(TAG, "run: wait");
                        receiveUdpSocket.receive(packet);
                        Log.d(TAG, "run: receive " + packet);
                        //受信バイト数取得 
                        int length = packet.getLength();
                        Log.d(TAG, "run: length" + length);
                        //受け取ったパケットを文字列にする 
                        address = new String(buf, 0, length);
                        Log.d(TAG, "run: address: " + address);
                        //↓③で使用  
                        returnIpAdress(address);
                        receiveUdpSocket.close();
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    //ゲストからの接続を待つ処理  
    void connect() {
        Log.d(TAG, "connect: ");
        new Thread() {
            @Override
            public void run() {
                try {
                    //ServerSocketを生成する
                    serverSocket = new ServerSocket(tcpPort);
                    //ゲストからの接続が完了するまで待って処理を進める 
                    connectedSocket = serverSocket.accept();
                    //この後はconnectedSocketに対してInputStreamやOutputStreamを用いて入出力を行ったりするが、ここでは割愛      
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Socket returnSocket;

    //ブロードキャスト発信者(ゲスト)にIPアドレスと端末名を返す   
    void returnIpAdress(final String address){
        Log.d(TAG, "returnIpAdress: ");
        new Thread() {
            @Override
            public void run() {
                try{
                    if(returnSocket != null){
                        returnSocket.close();
                        returnSocket = null;
                    }
                    if(returnSocket == null) {
                        Log.d(TAG, "run: address: " + address + ", tcpPort: " + tcpPort);
                        returnSocket = new Socket(address, tcpPort);
                    }
                    //端末情報をゲストに送り返す  
                    outputDeviceNameAndIp(returnSocket, Build.MODEL, wifiConnection.getIpAddress());
                }catch(UnknownHostException e){
                    e.printStackTrace();
                }catch (ConnectException e){
                    e.printStackTrace();
                    try{
                        if(returnSocket != null) {
                            returnSocket.close();
                            returnSocket = null;
                        }
                    }catch(IOException e1) {
                        e.printStackTrace();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }


    //端末名とIPアドレスのセットを送る  
    void outputDeviceNameAndIp(final Socket outputSocket, final String deviceName, final String deviceAddress){
        Log.d(TAG, "outputDeviceNameAndIp: ");
        new Thread(){
            @Override
            public void run(){
                final BufferedWriter bufferedWriter;
                try {
                    bufferedWriter = new BufferedWriter(
                            new OutputStreamWriter(outputSocket.getOutputStream())
                    );
                    Log.d(TAG, "run: write");
                    //デバイス名を書き込む    
                    bufferedWriter.write(deviceName);
                    bufferedWriter.newLine();
                    //IPアドレスを書き込む    
                    bufferedWriter.write(deviceAddress);
                    bufferedWriter.newLine();
                    //出力終了の文字列を書き込む  
                    bufferedWriter.write("outputFinish");
                    //出力する 
                    bufferedWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


}
