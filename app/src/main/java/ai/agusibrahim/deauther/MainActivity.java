package ai.agusibrahim.deauther;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.*;
import android.net.wifi.*;
import android.text.format.*;
import android.content.*;
import android.net.*;
import ai.agusibrahim.deauther.MainActivity.*;
import android.widget.*;
import java.util.*;
import ai.agusibrahim.deauther.model.*;
import ai.agusibrahim.deauther.adapter.*;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import org.json.*;
import android.os.*;
import java.net.*;
import android.support.design.widget.*;
import com.afollestad.materialdialogs.*;

public class MainActivity extends AppCompatActivity {
	Toolbar toolbar;
	ListView aplist;
	List<AP> apdata=new ArrayList<AP>();
	private MainActivity.WifiReceiver wirec;
	List<Attacker> attdata=new ArrayList<Attacker>();
	private APAdapter apadap;
	public static AsyncHttpClient client;
	private FloatingActionButton fabattack;
	private WifiManager wifiMgr;

	private MaterialDialog dlgAtt;

	private ListView attlist;

	private AttackrAdapter atadap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		client = new AsyncHttpClient();
		client.setTimeout(5);
		wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
		setContentView(R.layout.main_activity);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		fabattack=(FloatingActionButton) findViewById(R.id.fab_attack);
		aplist=(ListView) findViewById(R.id.aplist);
		setSupportActionBar(toolbar);
		wirec=new WifiReceiver();
		apadap=new APAdapter(this, apdata);
		aplist.setAdapter(apadap);
		aplist.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(final AdapterView<?> p1, View p2, final int p3, long p4) {
					client.get("http://192.168.4.1/APSelect.json?num="+p3, null, new TextHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode, Header[] headers, String res) {
								AP ap=(AP) p1.getItemAtPosition(p3);
								ap.check=!ap.check;
								apdata.set(p3, ap);
								apadap.notifyDataSetChanged();
							}

							@Override
							public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
								Toast.makeText(MainActivity.this, "Can't Select the AP. Error when connect to Deauther",1).show();
							}
						});
					
				}
			});
		fabattack.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(final View p1) {
					getAttackr();
				}
			});
		
		IntentFilter ifil=new IntentFilter();
		ifil.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(wirec, ifil);
		new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					getApList(true, false);
				}
			}, 1000);
	}
	
	private void getAttackr(){
		MainActivity.client.get("http://192.168.4.1/attackInfo.json", null, new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
					attdata.clear();
					try{
						JSONArray ar=res.getJSONArray("attacks");
						for(int i=0;i<ar.length();i++){
							JSONObject att=ar.getJSONObject(i);
							attdata.add(new Attacker(att.getString("name"), att.getString("status"), att.getInt("running")==1?true:false));
						}
						if(dlgAtt==null){
							showAttDlg();
						}else{
							if(dlgAtt.isShowing())
							atadap.notifyDataSetChanged();
							else showAttDlg();
						}
					} catch (Exception e) {}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
					Toast.makeText(MainActivity.this, "Can't Select the AP. Error when connect to Deauther",1).show();
				}
			});
	}
	private void showAttDlg(){
		View v=LayoutInflater.from(MainActivity.this).inflate(R.layout.main_attacker, null);
		attlist=(ListView) v.findViewById(R.id.mainattackerListView1);
		atadap=new AttackrAdapter(MainActivity.this, attdata, new AttackrAdapter.stateChanged(){
				@Override
				public void onChange(boolean state) {
					getAttackr();
				}

				@Override
				public void onSuccess() {
					// TODO: Implement this method
				}

				@Override
				public void onFail() {
					// TODO: Implement this method
				}
			});
		attlist.setAdapter(atadap);
		dlgAtt=new MaterialDialog.Builder(MainActivity.this)
			.customView(v, false)
			.title("Attack")
			.positiveText("Close")
			.show();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	public class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {     
			ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo netInfo = conMan.getActiveNetworkInfo();
			if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) 
				setTitle("Have Wifi, "+getWifiIp());
			else
				setTitle("Don't have Wifi Connection");    
		}   
	};
	private String getWifiIp(){
		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();
		//Toast.makeText(this, ""+wifiInfo.getBSSID(),0).show();
		String[] ipp=android.text.format. Formatter.formatIpAddress(ip).split("\\.");
		return ipp[0]+"."+ipp[1]+"."+ipp[2]+".1";
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(wirec);
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.menu_refresh:
				client.cancelAllRequests(true);
				scanAp(true);
				Toast.makeText(MainActivity.this, "Trying to Refresh",0).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void scanAp(final boolean clear){
		client.get("http://192.168.4.1/APScan.json", null, new TextHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, String res) {
					getApList(clear, true);
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
					Toast.makeText(MainActivity.this, "Fetching Error: "+t.getMessage(),1).show();
				}
			});
	}
	private void getApList(final boolean clear, final boolean refresh){
		/*for(int i=0;i<50;i++){
			int randrssi=randint(20, 80);
			apdata.add(new AP("Warung Kopi "+i, "a"+i+":bb:"+(10-i)+":11:"+randint(11,99)+":"+(55-i), randrssi-(randrssi*2), randint(0,1)==0?2:7, randint(1,9), false, false));
		}
		//apdata.add(new AP("Warung Kopi", "aa:bb:cc:11:22:33", -87, 4, 6, false, false));
		//apdata.add(new AP("RS Hassanudin", "ba:7b:5c:01:e2:43", -57, 2, 6, false, true));
		//apdata.add(new AP("WiFi-ID Telkom", "fa:5b:c7:11:22:43", -37, 5, 6, false, false));
		apadap.notifyDataSetChanged();
		if(true) return;*/
		client.get("http://"+getWifiIp()+"/APScanResults.json", null, new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
					try {
						JSONArray aps = res.getJSONArray("aps");
						List<AP> newapdata=new ArrayList<AP>();
						for(int i=0;i<aps.length();i++){
							JSONObject apo=aps.getJSONObject(i);
							newapdata.add(new AP(apo.getInt("i"), apo.getString("ss"), apo.getString("m"), apo.getInt("r"), apo.getInt("e"), apo.getInt("c"), false, apo.getInt("se")==1?true:false));
						}
						if(clear) apdata.clear();
						apdata.addAll(newapdata);
						apadap.notifyDataSetChanged();
						if(refresh){
							Toast.makeText(MainActivity.this, "Scan Success", 0).show();
						}
					} catch (JSONException e) {
						Toast.makeText(MainActivity.this, ""+e.getMessage(),1).show();
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
					
					Toast.makeText(MainActivity.this, "Fetching Error: "+t.getMessage(),1).show();
				}
			});
	}
	public static int randint(int minimum, int maximum){
		Random rn = new Random();
		int range = maximum - minimum + 1;
		int randomNum =  rn.nextInt(range) + minimum;
		return randomNum;
	}
}
