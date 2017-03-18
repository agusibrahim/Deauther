package ai.agusibrahim.deauther.model;

public class AP
{
	public String ssid, mac;
	public boolean check, hidden;
	public int rssi, enc, channel, idx;
	public AP(int idx, String ssid, String mac, int rssi, int enc, int channel, boolean hidden, boolean cek) {
		this.ssid = ssid;
		this.mac = mac;
		this.rssi = rssi;
		this.enc = enc;
		this.channel = channel;
		this.hidden = hidden;
		this.check=cek;
	}
	
}
