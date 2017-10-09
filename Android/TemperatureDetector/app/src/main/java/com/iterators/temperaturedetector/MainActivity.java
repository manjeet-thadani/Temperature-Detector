package com.iterators.temperaturedetector;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.io.*;
import android.widget.AdapterView.*;
import java.text.*;
import android.text.format.*;

public class MainActivity extends Activity 
{
	// for measuring running time
	int sec = 0;
	//for check current status and unexpected error
	boolean running,first;
	
	//declaration of UI based Widgets
	Button cdButton;
	TextView status,runningTime,tempReadings;
	
	//Adapter and Socket for bluetooth
	BluetoothAdapter btAdapter=null;
	BluetoothSocket btSocket=null;
	
	// Is bluetooth connected??
	Boolean connected;
	
	//Dialog to choose from available Paired devices
	Dialog deviceListDialog;
	
	String macAddress;
	StringBuilder recDataString = new StringBuilder();
	private static final UUID BTMODULEUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	//Handler gets called everytime when android Os receive any Msg
	Handler bluetoothIn;
	
	 //final state for handler msg state = 0
	private final int handlerState=0;
	
	
	//for getting date and time
	Calendar c;
	
	//Connect socket
	ConnectedThread mConnectedThread;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		//Get and Set action bar and set its icon
		ActionBar bar=getActionBar();
		bar.setIcon(R.drawable.chip);
		
		checkBtStatus();
		
		init();
		
		timer();
    }

	private void init(){
		cdButton=(Button)findViewById(R.id.cdbutton);
		status=(TextView)findViewById(R.id.status);
		runningTime=(TextView)findViewById(R.id.runningtime);
		tempReadings=(TextView)findViewById(R.id.tempReadings);
		
		connected=false;
		first=false;
		cdButton.setText("Connect");
		
		
		IntentFilter filter1=new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
		IntentFilter filter2=new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		IntentFilter filter3=new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		
		this.registerReceiver(mReceiver,filter1);
		this.registerReceiver(mReceiver,filter2);
		this.registerReceiver(mReceiver,filter3);
		
		cdButton.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1){
					if(!connected)
						showDialog();
					else{
						try{
							btSocket.close();
						}catch(IOException e){}
					}
					
				}
		});
		
		bluetoothIn = new Handler(){
			public void handleMessage(android.os.Message msg){
				String message = (String)msg.obj;
				recDataString.append(message);
				String[] array = recDataString.toString().split("\n");
				tempReadings.setText(array[array.length-1]);
			
				first=!first;
			
				c=Calendar.getInstance();
				
				SimpleDateFormat df=new SimpleDateFormat("dd-MMM-yyyy");
				String date=df.format(c.getTime());
				
				SimpleDateFormat tf=new SimpleDateFormat("HH:mm:ss");
				String time=tf.format(c.getTime());
				
				try{
					if(array[array.length-1].length()!=1){
						ReadingsDatabase rd=new ReadingsDatabase(MainActivity.this);
						rd.write();
						rd.insertIntoTable(date,time,array[array.length-1]);
						rd.close();
					}
				}catch(Exception e){
					Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
				}
				
			}
		};
	}

	private final BroadcastReceiver mReceiver=new BroadcastReceiver(){
		@Override
		public void onReceive(Context p1, Intent p2){
			String action=p2.getAction();
			BluetoothDevice device=p2.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				//device found
			}
			else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
				//device is now connected
				cdButton.setText("Disconnect");
				status.setText("Connected");
				running=true;
				connected=!connected;
			}
			else if(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)){
				//device is about to disconnect
				cdButton.setText("Connect");
				status.setText("Not Connected");
				running=false;
				sec=0;
				connected=!connected;
			}
			else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
				//device has disconnected
				cdButton.setText("Connect");
				status.setText("Not Connected");
				running=false;
				sec=0;
				connected=!connected;
			}
		}
	};
	
	private void timer(){
		
		final Handler handler=new Handler();
		handler.post(new Runnable(){
				@Override
				public void run()
				{
					int hrs=sec/3600;
					int min=(sec%3600)/60;
					int secs=sec%60;

					String str=String.format("%d:%02d:%02d",hrs,min,secs);
					runningTime.setText(str);

					if(running){
						sec++;
					}
					handler.postDelayed(this,1000);
				}
		});
		
		
	}
	
	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
		return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
	}

	public void doWork(){
		BluetoothDevice device = btAdapter.getRemoteDevice(macAddress);
		
		try{
			btSocket = createBluetoothSocket(device);
		}catch(IOException e){
			Toast.makeText(getApplicationContext(),"Socket Connection Failed",Toast.LENGTH_SHORT).show();
		}
		try{
			btSocket.connect();
		}catch(IOException e){
			try{
				btSocket.close();
			}catch(IOException e2){}
		}
		
		mConnectedThread=new ConnectedThread(btSocket);
		mConnectedThread.start();
		mConnectedThread.write("x");
	}
	
	void checkBtStatus(){
		btAdapter=BluetoothAdapter.getDefaultAdapter();

		if(btAdapter==null){
			Toast.makeText(getApplicationContext(),"Device Doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
		}
		else{
			if(btAdapter.isEnabled()){
				Toast.makeText(getApplicationContext(),"Bluetooth Enabled",Toast.LENGTH_SHORT).show();
			}
			else{
				AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
				builder.setMessage("App wants to turn on BT");
				builder.setCancelable(false);
				builder.setPositiveButton("Allow", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface p1, int p2){
							btAdapter.enable();
							Toast.makeText(getApplicationContext(),"Turning BT on",Toast.LENGTH_SHORT).show();
							cdButton.setText("Connect");
						}
				});
				builder.setNegativeButton("Deny", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface p1, int p2){
							finish();
						}
				});
				AlertDialog alert=builder.create();
				alert.show();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.menu,menu);
		return super.onCreateOptionsMenu(menu);
	}

	void showDialog(){
		
		deviceListDialog=new Dialog(MainActivity.this);
		
		deviceListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		deviceListDialog.setContentView(R.layout.connect_me_dialog);
		
		btAdapter=BluetoothAdapter.getDefaultAdapter();
		
		ArrayAdapter<String> mPairedDevicesArrayAdapterDialog = new ArrayAdapter<String>(MainActivity.this,R.layout.connect_me_new_row);
		
		ListView pairedDevicesListViewDialog = (ListView)deviceListDialog.findViewById(R.id.connectMeListView);
		pairedDevicesListViewDialog.setOnItemClickListener(onItemClick);
		
		Set<BluetoothDevice> pairedDevicesSetDialog = btAdapter.getBondedDevices();
		
		if(pairedDevicesSetDialog.size()>0){
			for (BluetoothDevice device : pairedDevicesSetDialog){
				mPairedDevicesArrayAdapterDialog.add(device.getName()+"\n"+device.getAddress());
			}
		}
		else{
			mPairedDevicesArrayAdapterDialog.add("No Paired Devices");
		}
		
		pairedDevicesListViewDialog.setAdapter(mPairedDevicesArrayAdapterDialog);
		deviceListDialog.show();
	}
	
	private OnItemClickListener onItemClick = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4){
			String deviceStr = ((TextView)p2).getText().toString();
			macAddress = deviceStr.substring(deviceStr.length()-17);
			Toast.makeText(getApplicationContext(),macAddress,Toast.LENGTH_SHORT).show();
			deviceListDialog.hide();
			doWork();
		}
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(running){
			Toast.makeText(getApplicationContext(),"Disconnect Device First",Toast.LENGTH_SHORT).show();
		}
		else{
			switch(item.getItemId()){
				case R.id.connectMe:
					showDialog();
					return true;
				
				case R.id.allRecords:
					ReadingsDatabase rd=new ReadingsDatabase(MainActivity.this);
					rd.write();
					Dialog d=new Dialog(MainActivity.this);
					d.setContentView(R.layout.records);
					d.setTitle("Readings:");
					TextView tv=(TextView)d.findViewById(R.id.tv);
					
					tv.setText(rd.getData());
					rd.close();
					d.show();
					return true;
				
				case R.id.last100Records:
					ReadingsDatabase rd1=new ReadingsDatabase(MainActivity.this);
					rd1.write();
					Dialog d1=new Dialog(MainActivity.this);
					d1.setContentView(R.layout.records);
					d1.setTitle("Readings:");
					TextView tv1=(TextView)d1.findViewById(R.id.tv);

					tv1.setText(rd1.last100());
					rd1.close();
					d1.show();
					rd1.close();
					return true;
					
				case R.id.exit:
					finish();
					return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private class ConnectedThread extends Thread{
		private InputStream mInStream;
		private OutputStream mOutStream;
		
		public ConnectedThread(BluetoothSocket btSocket){
			InputStream tempInStream=null;
			OutputStream tempOutStream=null;
			try{
				tempInStream=btSocket.getInputStream();
				tempOutStream=btSocket.getOutputStream();
			}catch(IOException e){}
			
			mInStream=tempInStream;
			mOutStream=tempOutStream;
		}
		
		public void run(){
			byte[] buffer = new byte[256];
			int bytes;
			while(true){
				try{
					bytes=mInStream.read(buffer);
					String message=new String(buffer,0,bytes);
					bluetoothIn.obtainMessage(handlerState,bytes,-1,message).sendToTarget();
				}catch(IOException e){
					break;
				}
			}
		}
		
		public void write(String input){
			byte[] msgBuffer=input.getBytes();
			
			try{
				mOutStream.write(msgBuffer);
			}catch(IOException e){
				Toast.makeText(getApplicationContext(),"Failure Occured",Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
}
