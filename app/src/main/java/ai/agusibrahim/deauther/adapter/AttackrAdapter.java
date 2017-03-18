package ai.agusibrahim.deauther.adapter;

import android.widget.*;
import android.content.*;
import java.util.*;
import android.view.*;
import ai.agusibrahim.deauther.model.*;
import ai.agusibrahim.deauther.R;
import android.graphics.*;
import ai.agusibrahim.deauther.MainActivity;
import cz.msebera.android.httpclient.*;
import com.loopj.android.http.*;

public class AttackrAdapter extends ArrayAdapter<Attacker>
{
	stateChanged state;
	public AttackrAdapter(Context ctx, List<Attacker> user, stateChanged state){
		super(ctx,0,user);
		this.state=state;
	}
	public static class ViewHolder{
		android.support.v7.widget.SwitchCompat atbtn;
		TextView atname, atstatus;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Attacker att=getItem(position);
		final ViewHolder holder;
		if(convertView==null){
			holder=new ViewHolder();
			convertView=LayoutInflater.from(getContext()).inflate(R.layout.item_attacker, parent, false);
			holder.atname=(TextView) convertView.findViewById(R.id.attacker_name);
			holder.atstatus=(TextView) convertView.findViewById(R.id.attacker_status);
			holder.atbtn=(android.support.v7.widget.SwitchCompat) convertView.findViewById(R.id.attacker_btn);
			convertView.setTag(holder);
		}else{
			holder=(AttackrAdapter.ViewHolder) convertView.getTag();
		}
		if(att.status.startsWith("no")){
			holder.atstatus.setTextColor(Color.RED);
			holder.atbtn.setEnabled(false);
			holder.atbtn.setChecked(false);
		}else if(att.status.startsWith("ready")){
			holder.atstatus.setTextColor(Color.GREEN);
			holder.atbtn.setChecked(false);
			holder.atbtn.setEnabled(true);
			holder.atbtn.setTag(1);
		}else{
			holder.atstatus.setTextColor(Color.DKGRAY);
			holder.atbtn.setChecked(true);
			holder.atbtn.setEnabled(true);
			holder.atbtn.setTag(0);
		}
		holder.atstatus.setText(att.status);
		holder.atname.setText(att.name);
		holder.atbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(final CompoundButton p1, boolean p2) {
					int pos=getPosition(att);
					state.onChange(p2);
					MainActivity.client.get("http://192.168.4.1/attackStart.json?num="+pos, null, new TextHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode, Header[] headers, String res) {
								state.onSuccess();
							}

							@Override
							public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
								state.onFail();
								Toast.makeText(p1.getContext(), "Can't Select the AP. Error when connect to Deauther",1).show();
							}
						});
				}
			});
		return convertView;
	}
	public interface stateChanged{
		void onChange(boolean state)
		void onSuccess()
		void onFail()
	}
}
