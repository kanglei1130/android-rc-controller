package com.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPService extends Service implements Runnable {

    private static final String TAG = "UDPService";
    private final Binder binder_ = new UDPService.UDPBinder();
    private AtomicBoolean UDPThreadRunning = new AtomicBoolean(false);
    public GetUSBIP getUSBIP = new GetUSBIP();

    public int remotePort = 5000;
    //automticly find USB tethering ip
    public String remoteIPName = getUSBIP.getUSBThetheredIP();
    public String control = "";

    public class UDPBinder extends Binder {
        public UDPService getService() {
            return UDPService.this;
        }
        public void sendData(String cmd){
            control = cmd;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder_;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        getIpAddress();
        return START_STICKY;
    }

    private void startService() {
        Log.d(TAG, "start service");
        UDPThreadRunning.set(true);
        (new Thread(this)).start();
    }

    public void onDestroy() {
        Log.d(TAG,"udpserver connection is closed");
        stopSelf();
        UDPThreadRunning.set(false);
    }

    public void run() {
        // TODO Auto-generated method stub
        Log.d(TAG, "start sending thread");
        while (UDPThreadRunning.get()) {
            try {
                InetAddress serverAddr = InetAddress.getByName(remoteIPName);
                DatagramSocket udpSocket = new DatagramSocket();
                byte[] buf = control.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, remotePort);
                udpSocket.send(packet);
                udpSocket.close();
            } catch (SocketException e) {
                Log.e("Udp:", "Socket Error:", e);
            } catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            }
        }
    }

/*  Get local ip and show IP when initial for smoothing user. This IP need to be used as remote IP in C++
*   This IP doesn't like to change. So you just need to check once.
*/
    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    String sAddr = inetAddress.getHostAddress();
                    boolean isIPv4 = sAddr.indexOf(':') < 0;
                    if (!inetAddress.isLoopbackAddress() && isIPv4) {
                        ip = inetAddress.getHostAddress();
                        Log.d(TAG, "Local Ip Address is: " + ip);
                    }
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip = "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

}
