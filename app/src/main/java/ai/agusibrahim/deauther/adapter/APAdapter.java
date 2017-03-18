package ai.agusibrahim.deauther.adapter;

import android.widget.*;
import android.content.*;
import java.util.*;
import android.view.*;
import ai.agusibrahim.deauther.model.*;
import ai.agusibrahim.deauther.R;

public class APAdapter extends ArrayAdapter<AP>
{
	private HashMap<Integer, String> encmap=new HashMap<Integer, String>();
	public APAdapter(Context ctx, List<AP> user){
		super(ctx,0,user);
		encmap.put(8, "WPA*");
		encmap.put(4, "WPA2");
		encmap.put(2, "WPA");
		encmap.put(7, "OPEN");
		encmap.put(5, "WEP");
	}
	public static class ViewHolder{
		TextView ssid, rssi, channel, mac, hidden, enc;
		ProgressBar signal;
		ImageView check, encimg;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AP ap=getItem(position);
		ViewHolder holder;
		if(convertView==null){
			holder=new ViewHolder();
			convertView=LayoutInflater.from(getContext()).inflate(R.layout.item_ap, parent, false);
			holder.ssid=(TextView) convertView.findViewById(R.id.ap_ssid);
			holder.mac=(TextView) convertView.findViewById(R.id.ap_mac);
			holder.enc=(TextView) convertView.findViewById(R.id.ap_enc);
			holder.mac=(TextView) convertView.findViewById(R.id.ap_mac);
			holder.check=(ImageView) convertView.findViewById(R.id.ap_check);
			holder.encimg=(ImageView) convertView.findViewById(R.id.ap_encimg);
			holder.signal=(ProgressBar) convertView.findViewById(R.id.ap_signal);
			convertView.setTag(holder);
		}else{
			holder=(APAdapter.ViewHolder) convertView.getTag();
		}
		holder.signal.setMax(100);
		holder.ssid.setText(ap.ssid);
		holder.mac.setText(ap.mac);
		holder.enc.setText(encmap.get(ap.enc));
		if(ap.enc==7)
			holder.encimg.setImageResource(R.drawable.ic_lock_unlocked);
		else holder.encimg.setImageResource(R.drawable.ic_lock);
		holder.signal.setProgress(getPowerPercentage(ap.rssi));
		if(ap.check){
			holder.check.setVisibility(View.VISIBLE);
			holder.check.setImageResource(R.drawable.ic_checkbox_marked_circle);
		}else holder.check.setVisibility(View.INVISIBLE);
		return convertView;
	}
	public int getPowerPercentage(int power) {
		int i = 0;
		if (power <= -100) {
            i = 0;
		} else {
            i = 100 + power;
		}
		return i;
	}
}



