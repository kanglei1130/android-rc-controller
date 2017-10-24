package com.Service;

import android.util.Log;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Created by Wei on 8/9/17.
 * We don't need to change the IP address which App is connecting to every time.
 * The App will automotive find the IP address according to the Mac address
 */

public class GetUSBIP {
    private String TAG = "GetUSBIP";

    public String getUSBThetheredIP(){

        BufferedReader bufferedReader=null;
        String ips="";
        try{
            bufferedReader=new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while((line=bufferedReader.readLine())!=null){
                String[]splitted=line.split(" +");
                if(splitted!=null&&splitted.length>=4){
                    String ip=splitted[0];
                    String mac=splitted[3];
                    if(mac.matches("..:..:..:..:..:..")){
                        if(mac.matches("00:00:00:00:00:00")){
                            //Log.d(TAG,"DEBUG Wrong IP:" + mac + ":" + ip);
                        }else{
                            Log.d(TAG, "remote PC MAC is: " + mac + ",remote PC IP address is: " + ip);
                            ips=ip;
                            break;
                        }
                    }
                }
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            try{
                bufferedReader.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return ips;
    }
}