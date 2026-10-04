package dev.flondy.checkyourlove;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Message {
public static void showToast(Context context, String message, String TAG)
{
	//ZEIGT ROT IM LOGCAT NICHT IM UI
	//d = DEBUG löst aber rote ERROR MELDUNGEN AUS
	if(TAG == "d")
	{
		//FÜR DEBUGGING LOG ANWEISUNGEN
		Log.e("Flondy", message);
	}
	//ZEIGT IM UI
	else {
//TAG = t
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
}
}
