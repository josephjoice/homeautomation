/*
 * Copyright (C) 2011 Eirik Taylor
 *
 * This work is licensed under a Creative Commons Attribution-Noncommercial-Share Alike 3.0 Unported License.
 * See the following website for more information: 
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 * 
 */

package com.uzzors2k.blu_car;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import android.app.ActionBar.LayoutParams;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class blu_car extends FragmentActivity { 
	String tempin=null,tempout=null,LPG=null,lightin=null,lightout=null,motion=null;
	Double Tempin=0.0,Tempout=0.0,Lightin=0.0,Lightout=0.0;
	int dispflag=0;
	int l1=0,l2=0;
	boolean Lpg=false,Motion=false;
	public class FragmentClass extends android.support.v4.app.Fragment
	{
		int layout;
		View view ;
		int PageNum;
		FragmentClass(int view,int pgnum)
		{
			PageNum=pgnum;
			layout=view;
			
		}
		@Override
	      public View onCreateView(LayoutInflater inflater, ViewGroup container,
	              Bundle savedInstanceState) {
			
			view = inflater.inflate(layout, container, false);
			if(PageNum==0)
			{
				
				connect_button=(Button)view.findViewById(R.id.connect_button);
				if(outStream!=null)
				{
				connect_button.setBackgroundResource(R.color.button_connected);
				connect_button.setText(R.string.connected);
				}
				connect_button.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						connect();
						
					}
				});
			}
			else if(PageNum==1)
			{
				regulator=(SeekBar)view.findViewById(R.id.regulator);
				regulator.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						 if(progress<25)
							 seekBar.setProgress(0);
						 else if(progress<75)
							 seekBar.setProgress(50);
						 else
							 seekBar.setProgress(100);
					send(findViewById(R.id.fan));	 
						  
						
					}
				});
				
				((ImageView)view.findViewById(R.id.im1)).setImageResource(l1==1?R.drawable.lighton:R.drawable.lightoff);
				((ImageView)view.findViewById(R.id.im2)).setImageResource(l2==1?R.drawable.lighton:R.drawable.lightoff);

			}
			else if(PageNum==2)
			{
				
				((TextView)view.findViewById(R.id.tempin)).setText(tempin+Tempin);
				((TextView)view.findViewById(R.id.tempout)).setText(tempout+Tempout);
				((TextView)view.findViewById(R.id.lightin)).setText(lightin+Lightin);
				((TextView)view.findViewById(R.id.lightout)).setText(lightout+Lightout);
				((TextView)view.findViewById(R.id.lpg)).setText(LPG+(Lpg?"UNSAFE":"SAFE"));
				((TextView)view.findViewById(R.id.motion)).setText(motion+(Motion?"YES":"NO"));
				tempPage=view;
				dispflag=1;
				
			}
			else if(PageNum==3)
			{
				
			}
			
	          return view;
	}}
	public class TabPagerAdapter extends FragmentStatePagerAdapter {
		android.support.v4.app.Fragment f;
	    public TabPagerAdapter(android.support.v4.app.FragmentManager fm) {
	    super(fm);
	    
	  }
	  @Override
	  public android.support.v4.app.Fragment getItem(int i) {
		
	    switch (i) {
	        case 0:
	            f=new FragmentClass(R.layout.startpage, 0);
	            return f;
	        case 1:
	        	 f=new FragmentClass(R.layout.main, 1);
	            return f;
	        case 2:
	        	 f=new FragmentClass(R.layout.temp_display, 2);
	            return f;
	        case 3:
	        	 f=new FragmentClass(R.layout.sensomate, 3);
	            return f;
	        case 4:
	        	 f=new FragmentClass(R.layout.credits, 4);
	            return f;    
	       
	        }
	    return null;
	  }
	  @Override
	  public int getCount() {
	    // TODO Auto-generated method stub
	    return 5; //No of Tabs
	  }
	    }
	
	// Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    int flag[]={0,0,0,0,0};
    // Program variables
    public int speed=1;
    private byte AttinyOut;
    private boolean ledStat;
    public View tempPage;
    private boolean connectStat = false;
    private Button led_button;
    private Button forward_button;
    private Button reverse_button;
    Button connect_button;
    private TextView xAccel;
    protected static final int MOVE_TIME = 80;
    private long lastWrite = 0;
    public int COMMANDMODE=1;
    private AlertDialog aboutAlert;
    private View aboutView;
    private View controlView;
    OnClickListener myClickListener;
    ProgressDialog myProgressDialog;
    private Toast failToast;
    private Handler mHandler;
    SeekBar regulator;
    TabPagerAdapter TabAdapter;
    ViewPager Tab;
   
   
	// Bluetooth Stuff
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null; 
    private OutputStream outStream = null;
    private InputStream inStream=null;
    
    private ConnectThread mConnectThread = null;
    private String deviceAddress = null;
    // Well known SPP UUID (will *probably* map to RFCOMM channel 1 (default) if not in use); 
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
     /** Called when the activity is first created. */ 
     @Override 
     public void onCreate(Bundle savedInstanceState) { 
    	 super.onCreate(savedInstanceState);
    	 
    	 // Create main button view
    	 LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	 aboutView = inflater.inflate(R.layout.aboutview, null);
    	 controlView = inflater.inflate(R.layout.pager, null);
    	 controlView.setKeepScreenOn(true);
    	 setContentView(controlView);
    	 tempin   ="Inside Temperature   :";
    	 tempout  ="Outside Temperature:";
    	 lightin  ="Inside Light                :";
    	 lightout ="Outside Light             :";
    	 motion   ="Motion in house        :";
    	 LPG      ="LPG state                   :";
    	 
         // Finds buttons in .xml layout file
      
        
       
         
         // Handle click events from help and info dialogs
         myClickListener = new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				switch (which) {
    				case DialogInterface.BUTTON_POSITIVE:
    					dialog.dismiss();
    				break;
    				case DialogInterface.BUTTON_NEUTRAL:
    					// Display website
    					Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(getResources().getString(R.string.website_url)));
						startActivity(browserIntent);
    				break;
    				default: dialog.dismiss();
    				}
    			}
         };
         
         myProgressDialog = new ProgressDialog(this);
         failToast = Toast.makeText(this, R.string.failedToConnect, Toast.LENGTH_SHORT);
         
         mHandler = new Handler() {
             @Override
             public void handleMessage(Message msg) {
            	 if (myProgressDialog.isShowing()) {
                 	myProgressDialog.dismiss();
                 }
            	 // Check if bluetooth connection was made to selected device
                 if (msg.what == 1) {
                 	// Set button to display current status
                     connectStat = true;
                     connect_button.setText(R.string.connected);
                     connect_button.setBackgroundResource(R.color.button_connected);
         	 		// Reset the BluCar
         	 		AttinyOut = 0;
         	 		ledStat = false;
         	 		write(AttinyOut);
                 }
                 else if(msg.what>65&&dispflag==1)
                 {
                	 
                	 ((TextView)tempPage.findViewById(R.id.tempin)).setText(tempin+Tempin);
     				((TextView)tempPage.findViewById(R.id.tempout)).setText(tempout+Tempout);
     				((TextView)tempPage.findViewById(R.id.lightin)).setText(lightin+Lightin);
     				((TextView)tempPage.findViewById(R.id.lightout)).setText(lightout+Lightout);
     				((TextView)tempPage.findViewById(R.id.lpg)).setText(LPG+(Lpg?"UNSAFE":"SAFE"));
     				((TextView)tempPage.findViewById(R.id.motion)).setText(motion+(Motion?"YES":"NO"));
                 }
                 
             }
         };
         
         // Create about dialog
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setView(aboutView).setCancelable(true).setTitle(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.appVersion)).setIcon(R.drawable.robocet).setPositiveButton(getResources().getString(R.string.okButton), myClickListener).setNeutralButton(getResources().getString(R.string.websiteButton), myClickListener);
         aboutAlert = builder.create();
         
         // Check whether bluetooth adapter exists
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
         if (mBluetoothAdapter == null) { 
              Toast.makeText(this, R.string.no_bt_device, Toast.LENGTH_LONG).show(); 
              finish(); 
              return; 
         } 
         
         // If BT is not on, request that it be enabled.
         if (!mBluetoothAdapter.isEnabled()) {
        	 Toast.makeText(getApplicationContext(), "Requesting Bluetooth", Toast.LENGTH_SHORT).show();
             Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
         }
         
          /**********************************************************************
           * Buttons for controlling BluCar
           */
          
         // Connect to Bluetooth Module
    
       
       TabAdapter = new TabPagerAdapter(getSupportFragmentManager());
         Tab = (ViewPager)findViewById(R.id.pager);
         Tab.setAdapter(TabAdapter);
   
        
    
        
         
        
     }
     int sensor_state=0;
     public void send(View V)
     
     
     { 
    	 switch(COMMANDMODE)
    	 {
    	 	case 0:
    	 		break;
    	 	case 1:
    	 		
    	 		switch(V.getId())
    	 		{

    	 		case R.id.lamp1:
    	 			if(((ToggleButton)findViewById(R.id.lamp1)).isChecked())
    	 			{
    	 				l1=1;
    	 				((ImageView)findViewById(R.id.im1)).setImageResource(R.drawable.lighton);
    	 				
    	 				write((byte)'A');
    	 			}
    	 			else
    	 			{
    	 				l1=0;
    	 				((ImageView)findViewById(R.id.im1)).setImageResource(R.drawable.lightoff);
    	 				write((byte)'a');
    	 			}
  
    	 			break;
    	 		case R.id.lamp2:
    	 			if(((ToggleButton)findViewById(R.id.lamp2)).isChecked())
    	 			{l2=1;
    	 				((ImageView)findViewById(R.id.im2)).setImageResource(R.drawable.lighton);
    	 				write((byte)'B');
    	 			}
    	 			else
    	 			{
    	 				l2=0;
    	 				write((byte)'b');
    	 				((ImageView)findViewById(R.id.im2)).setImageResource(R.drawable.lightoff);
    	 			}
  
    	 			break;
    	 		case R.id.fan:
    	 			if(((ToggleButton)findViewById(R.id.fan)).isChecked())
    	 				write((byte)'D');
    	 			else
    	 				write((byte)'d');
    	 			if(regulator.getProgress()==0)
    	 				write((byte)'0');
    	 			else if(regulator.getProgress()==50)
    	 				write((byte)'1');
    	 			else
    	 				write((byte)'2');
    	 			break;
    	 		case R.id.fan2:
    	 			if(((ToggleButton)findViewById(R.id.fan2)).isChecked())
    	 				write((byte)'X');
    	 			else
    	 				write((byte)'x');
    	 			break;
    	 		case R.id.sensor_state:
    	 			if(((ToggleButton)findViewById(R.id.sensor_state)).isChecked())
    	 				write((byte)'p');
    	 			else
    	 				write((byte)'P');
    	 			break;	
    	 		case R.id.sleep_mode:
    	 			if(((ToggleButton)findViewById(R.id.sleep_mode)).isChecked())
    	 				write((byte)'s');
    	 			else
    	 				write((byte)'S');
    	 			break;		
    	 		
    	 		}
    	 		break;
    	 }	
     }
     /** Thread used to connect to a specified Bluetooth Device */
     public class ConnectThread extends Thread {
    	private String address;
    	private boolean connectionStatus;
    	
		ConnectThread(String MACaddress) {
			address = MACaddress;
			connectionStatus = true;
    	}
		
		public void run() {
    		// When this returns, it will 'know' about the server, 
            // via it's MAC address. 
			try {
				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
				
				// We need two things before we can successfully connect 
	            // (authentication issues aside): a MAC address, which we 
	            // already have, and an RFCOMM channel. 
	            // Because RFCOMM channels (aka ports) are limited in 
	            // number, Android doesn't allow you to use them directly; 
	            // instead you request a RFCOMM mapping based on a service 
	            // ID. In our case, we will use the well-known SPP Service 
	            // ID. This ID is in UUID (GUID to you Microsofties) 
	            // format. Given the UUID, Android will handle the 
	            // mapping for you. Generally, this will return RFCOMM 1, 
	            // but not always; it depends what other BlueTooth services 
	            // are in use on your Android device. 
	            try { 
	                 btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID); 
	            } catch (IOException e) { 
	            	connectionStatus = false;
	            } 
			}catch (IllegalArgumentException e) {
				connectionStatus = false;
			}
            
            // Discovery may be going on, e.g., if you're running a 
            // 'scan for devices' search from your handset's Bluetooth 
            // settings, so we call cancelDiscovery(). It doesn't hurt 
            // to call it, but it might hurt not to... discovery is a 
            // heavyweight process; you don't want it in progress when 
            // a connection attempt is made. 
            mBluetoothAdapter.cancelDiscovery(); 
            
            // Blocking connect, for a simple client nothing else can 
            // happen until a successful connection is made, so we 
            // don't care if it blocks. 
            try {
                 btSocket.connect(); 
            } catch (IOException e1) {
                 try {
                      btSocket.close(); 
                 } catch (IOException e2) {
                 }
            }
            
            // Create a data stream so we can talk to server. 
            try { 
            	outStream = btSocket.getOutputStream(); 
            	inStream=btSocket.getInputStream();
            } catch (IOException e2) {
            	connectionStatus = false;
            }
            
            // Send final result
            if (connectionStatus) {
            	mHandler.sendEmptyMessage(1);
            	ReadThread read=new ReadThread(inStream);
            	read.start();
            	
            }else {
            	mHandler.sendEmptyMessage(0);
            }
		}
     }
     public class ReadThread extends Thread
     {String t="";
    	 TextView text;
    	 InputStream in;
    	 public ReadThread(InputStream inp) {
    		 in=inp;
    		int t1=0; 
    		
    		 
		}
    	 
    	 public void run()
    	 {
    		 while(true)
    			 
    		 {
    			 try {
    			char a=(char)in.read();
    			if(a=='g')
    				Lpg=false;
    			else if(a=='G')
    				Lpg=true;
    			else if(a=='m')
    				Motion=false;
    			else if(a=='M')
    				Motion=true;
    			else if((a>='0'&&a<='9'||a=='.'))
    				t=t+a;
    			else if(a=='k')
    			{
    				if(!t.equals(""))
    				Tempin=Double.parseDouble(t);
    				t="";
    			}
    			else if(a=='j')
    			{
    				if(!t.equals(""))
    				Tempout=Double.parseDouble(t);
    				t="";
    			}
    			else if(a=='i')
    			{
    				if(!t.equals(""))
    				Lightin=Double.parseDouble(t);
    				t="";
    			}
    			else if(a=='h')
    			{
    				if(!t.equals(""))
    				Lightout=Double.parseDouble(t);
    				t="";
    			}
    		if(a=='m'||a=='g'||a=='j'||a=='k'||a=='h'||a=='i'||a=='M'||a=='G')
    			mHandler.sendEmptyMessage((int)a);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//Toast.makeText(getApplicationContext(), "error ", Toast.LENGTH_LONG).show();
					}
    			 
    		 }
    	 }
     }
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	 switch (requestCode) {
         case REQUEST_CONNECT_DEVICE:
        	 // When DeviceListActivity returns with a device to connect
             if (resultCode == Activity.RESULT_OK) {
            	// Show please wait dialog
 				myProgressDialog = ProgressDialog.show(this, getResources().getString(R.string.pleaseWait), getResources().getString(R.string.makingConnectionString), true);
 				
            	// Get the device MAC address
        		deviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        		// Connect to device with specified MAC address
        		//connect_button.setBackgroundResource(resid)
                mConnectThread = new ConnectThread(deviceAddress);
                mConnectThread.start();
                
             }else {
            	 // Failure retrieving MAC address
            	 Toast.makeText(this, R.string.macFailed, Toast.LENGTH_SHORT).show();
             }
             break;
         case REQUEST_ENABLE_BT:
             // When the request to enable Bluetooth returns
             if (resultCode == Activity.RESULT_OK) {
                 // Bluetooth is now enabled
             } else {
                 // User did not enable Bluetooth or an error occured
                 Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                 finish();
             }
         }
     }
     
    
     
     
     
     public void write(byte data) {
    	 if (outStream != null) {
             try {
            	 outStream.write(data);
            	 //outStream.write((byte)'\r');
            //	outStream.//write((byte)'\n');
            	
             } catch (IOException e) {
             }
         }
     }
     
     public void emptyOutStream() {
    	 if (outStream != null) {
             try {
            	 outStream.flush();
             } catch (IOException e) {
             }
         }
     }
     
     public void connect() {
    	 // Launch the DeviceListActivity to see devices and do scan
         Intent serverIntent = new Intent(this, DeviceListActivity.class);
         if(!connectStat)
         startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
         else
        disconnect();
     }
     
    
     public void disconnect() {
    	 if (outStream != null) {
    		 try {
    	 			outStream.close();
    	 			connectStat = false;
    				connect_button.setText(R.string.disconnected);
    				connect_button.setBackgroundResource(R.color.button_src);
    	 		} catch (IOException e) {
    	 		}
    	 } 
     }
     
   
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.option_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.about:
             
        	 aboutAlert.show();
             return true;
         }
         return false;
     }

     @Override 
     public void onResume() { 
          super.onResume();
     } 

     @Override 
     public void onDestroy() { 
    	 emptyOutStream();
         disconnect();
         
         super.onDestroy(); 
     } 
  static int numberpicked=-1,light;
  static String a="";
  static int vi=0;
  PopupWindow win=null;
public void program(View v)
{
	vi=v.getId();
	
	LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 View popupview = inflater.inflate(R.layout.popup, null);
	 NumberPicker nump=((NumberPicker)popupview.findViewById(R.id.number));
	 numberpicked=nump.getValue();
	 nump.setOnValueChangedListener(new OnValueChangeListener() {
		
		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
			numberpicked=newVal;
			
		}
	});
	 Button bprog=((Button)popupview.findViewById(R.id.prog));
	 Button unprog=((Button)popupview.findViewById(R.id.unprog));
	 Button cancel=((Button)popupview.findViewById(R.id.cancel));
	 cancel.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		win.dismiss();	
			
		}
	});
	 unprog.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			send();
			win.dismiss();	
		}
	});
	 bprog.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(numberpicked<10)
				a='0'+Integer.toString(numberpicked);
			else
				
		 a=Integer.toString(numberpicked);
		send();	
		win.dismiss();		
		}
	});
	 nump.setMinValue(0);
	 nump.setMaxValue(99);
	win=new PopupWindow(popupview, LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
	win.showAtLocation(v.getRootView(), Gravity.CENTER,0,0);
	}
public void send()
{
	if(vi==R.id.prog1&&a.equals(""))
	{
		write((byte)'I');
		////write((byte)'\n');
		
	}
	else if(vi==R.id.prog1)
	{
		write((byte)'H');
		//write((byte)'\n');
		write((byte)a.charAt(0));
		//write((byte)'\n');
		write((byte)a.charAt(1));
		//write((byte)'\n');
		write((byte)'h');
		//write((byte)'\n');
		a="";
		
	}
	else if(vi==R.id.prog2&&a.equals(""))
	{
		write((byte)'K');
		//write((byte)'\n');
		
	}
	else if(vi==R.id.prog2)
	{
		write((byte)'J');
		//write((byte)'\n');
		write((byte)a.charAt(0));
		//write((byte)'\n');
		write((byte)a.charAt(1));
		//write((byte)'\n');
		write((byte)'j');
		//write((byte)'\n');
		a="";
		
	}
	else if(vi==R.id.prog3&&a.equals(""))
	{
		write((byte)'M');
		//write((byte)'\n');
		
	}
	else if(vi==R.id.prog3)
	{
		write((byte)'L');
		//write((byte)'\n');
		write((byte)a.charAt(0));
		//write((byte)'\n');
		write((byte)a.charAt(1));
		//write((byte)'\n');
		write((byte)'l');
		//write((byte)'\n');
		a="";
		
	}
		
	}
}
