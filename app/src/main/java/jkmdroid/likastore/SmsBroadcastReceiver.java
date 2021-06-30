package jkmdroid.likastore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

/**
 * Created by jkmdroid on 6/27/21.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {
    SmsMessage smsMessage;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Intent intent1;
        try {
            String format = bundle.getString("format");
            Object[] pduObject = (Object[]) bundle.get("pdus");
            for (Object o : pduObject) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    smsMessage = SmsMessage.createFromPdu((byte[]) o, format);
                } else {
                    smsMessage = SmsMessage.createFromPdu((byte[]) o);
                }
                String phoneNumber = smsMessage.getDisplayOriginatingAddress(), message = smsMessage.getDisplayMessageBody();
                System.out.println(phoneNumber + "\n" + message + "=>broadcast");
                if (phoneNumber.equalsIgnoreCase("MPESA") && (message.contains("LiquorStore") || message.contains("Confirmed"))) {
                    intent1 = new Intent("mpesa_code");
                    intent1.putExtra("message", message);
                }else{
                    intent1 = new Intent("error_code");
                    intent1.putExtra("error", "message not found");
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }
    }
}