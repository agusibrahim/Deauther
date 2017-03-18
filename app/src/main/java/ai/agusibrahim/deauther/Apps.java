package ai.agusibrahim.deauther;
import android.app.*;
import cat.ereza.customactivityoncrash.*;

public class Apps extends Application
{
	@Override
	public void onCreate() {
		CustomActivityOnCrash.install(this);
		super.onCreate();
	}
}
