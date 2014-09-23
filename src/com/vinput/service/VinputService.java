package com.vinput.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.commands.input.Input;

public class VinputService extends Service {
	final String TAG = "LeeServer";

	 private OutputStream outStream = null;
	 private Socket clientSocket = null;
	 private ServerSocket mServerSocket = null;
	 private ReceiveThread mReceiveThread = null;
	 private AcceptThread mAcceptThread = null;
	 private boolean stop = true;
	 DisplayMetrics dm = null;
	 int Hostw = 0;
	 int Hosth = 0;
	 Input vinput = new Input();


	@Override
	public void onCreate() {
		super.onCreate();

		Log.e(TAG, "Stsrt Server");
		dm = getResources().getDisplayMetrics();
		 Hostw = dm.widthPixels;
		 Hosth = dm.heightPixels;

		 mAcceptThread = new AcceptThread();
		 mAcceptThread.start();

	}

	private class AcceptThread extends Thread
	{
		@Override
		public void  run() {
		    try {
			     //实例化ServerSocket对象并设置端口号为8888
	             mServerSocket = new ServerSocket(8888);
	             while(true){
	                 clientSocket = mServerSocket.accept();
	                 Log.e(TAG, "Client IP: " + clientSocket.getInetAddress().getHostAddress());
	                 mReceiveThread = new ReceiveThread(clientSocket);
	                 stop = false;
	                 mReceiveThread.start();
	             }

		     } catch (IOException e) {
			      // TODO Auto-generated catch block
			      e.printStackTrace();
		     }

		}
	}

	private class ReceiveThread extends Thread
	{
	    private InputStream mInputStream = null;
	    private byte[] buf ;
	    private String str = null;

	    ReceiveThread(Socket s)
	    {
	        try {
	    		//获得输入流
	    		this.mInputStream = s.getInputStream();
	    	} catch (IOException e) {
	        // TODO Auto-generated catch block
	    		e.printStackTrace();
            }
        }

	    @Override
	     public void run()
         {
	    	while(!stop)
	    	{
	    		this.buf = new byte[512];

	    		//读取输入的数据(阻塞读)
	    		try {
	    			this.mInputStream.read(buf);
	    		} catch (IOException e1) {
	    			// TODO Auto-generated catch block
	    			e1.printStackTrace();
	    		}

	    		//字符编码转换
	    		try {
	    			this.str = new String(this.buf, "utf-8").trim();
	    			Log.e(TAG, "MSG: " + str);
	    			if(this.str.equals(""))
	    				break;
	    			String[] tmp = this.str.split(";");
	    			int clientw = Integer.parseInt(tmp[0]);;
	    			int clienth = Integer.parseInt(tmp[1]);
	    			int action = Integer.parseInt(tmp[2]);
	    			int x = Integer.parseInt(tmp[3]);;
	    			int y = Integer.parseInt(tmp[4]);
	    			float scale = (float)Hostw/(float)clientw;
	    			Log.e(TAG, "HOST: " + Hostw + ", " + Hosth + ", Client: " + clientw + ", " + clienth + ", X: " + x*scale + ", Y: " +y *scale+  ", action" + action);

	    			vinput.sendTap(x*scale, y*scale, action);

	    		} catch (UnsupportedEncodingException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}

            }
	    	try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	 }

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mReceiveThread != null)
		{
			stop = true;
			mReceiveThread.interrupt();
		 }
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}


}
