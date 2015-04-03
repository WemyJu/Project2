package six.project2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import six.project2.DBAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class GoParking extends Activity {
	private final static String TAG="HelloWorld";
	private ImageButton recordBtn,searchBtn,helpBtn;
	private Thread registerThread;
	private DBAdapter myDB;
	private int id=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		//-------connect button---------
		recordBtn=(ImageButton)findViewById(R.id.home_record);
		searchBtn=(ImageButton)findViewById(R.id.home_search);
		helpBtn=(ImageButton)findViewById(R.id.home_help);
		//-------set button event---------
	
		recordBtn.setOnClickListener(new OnClickListener()
			{	
				@Override
				public void onClick(View v) 
				{
					Intent recordIntent=new Intent(GoParking.this,Record.class);
					Bundle bundle = new Bundle();
					bundle.putInt("ID",id);
					recordIntent.putExtras(bundle); 
					startActivity(recordIntent);
				}
			
			}
		);
		
		searchBtn.setOnClickListener(new OnClickListener()
			{	
				@Override
				public void onClick(View v) 
				{
					Intent searchIntent=new Intent(GoParking.this,Search.class);
					Bundle bundle = new Bundle();
					bundle.putInt("ID",id);
					searchIntent.putExtras(bundle); //將參數放入
					startActivity(searchIntent);
				}
			
			}
		);
		myDB=new DBAdapter(this);
		helpBtn.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View v) 
			{
				myDB.open();
				myDB.deleteDB();
				myDB.close();

			}
		
		}
		);
		
		Register register=new Register();
		registerThread=new Thread(register);
		registerThread.start();
	}


	public class Register implements Runnable
	{
		Socket socket;
		OutputStream out;
		InputStream in;
		byte[] strb = new byte[200];
		
		@Override
		public void run() 
		{
			try 
			{
				socket=new Socket("140.116.246.200",9000);
				out=socket.getOutputStream();
				in=socket.getInputStream();
			} 
			catch (UnknownHostException e) {} 
			catch (IOException e) {}
			try {
				idConfirm();
			} catch (IOException e) {e.printStackTrace();}
		}
		private void idConfirm() throws IOException
    	{    		
    		Log.v(TAG,"id confirm");
    		myDB.open();
    		Cursor cur=myDB.getMyId();
    		String msg;
    		//-----old user-----
    		if(cur.moveToFirst())
    		{
    			id=cur.getInt(1);
    			msg="who#"+id+"#";
    			sendData(msg);		
    			Log.v(TAG,msg);
    			
    		}//-----new user-----
    		else
    		{
    			sendData("new#");
    			msg=readData();
    			id=Integer.parseInt(msg);
    			myDB.insertID(id);  			
    		}
    		cur.close();
    		myDB.close();
    		socket.close();
    	}
		private void sendData(String s)
    	{
    		Log.v(TAG,"send "+s);
    		Arrays.fill(strb, (byte)0);
    		System.arraycopy(s.getBytes(), 0, strb, 0, s.length());
			try 
			{
				if(out!=null)
				{
					out.write(strb);
					out.flush();
				}
			} catch (IOException e1) {}
    	}
		private String readData()
    	{    		
    		String s = "";
    		String[] splits = null;
    		Arrays.fill(strb, (byte)0);
    		try 
			{
    			if(in!=null)
    				in.read(strb);
    			
    			s=new String(strb);
    			splits=s.split("#");
			
			} catch (IOException e1) {}
    		return splits[0];
    	}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_home, container,
					false);
			return rootView;
		}
	}
	@Override
	protected void onPause() 
	{
		super.onPause();
		Log.v(TAG,"HomeOnPause");
	}
	@Override
	protected void onStop() 
	{
		super.onStop();
		Log.v(TAG,"HomeOnStop");
	}
	protected void onDestroy(Bundle savedInstanceState) 
	{
		Log.v(TAG,"HomeOnDestroy");

		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}
