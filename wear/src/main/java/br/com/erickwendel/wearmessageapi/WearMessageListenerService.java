package br.com.erickwendel.wearmessageapi;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by erickwendel on 7/20/16.
 */
public class WearMessageListenerService extends WearableListenerService {
    private static  final String START_ACTIVITY = "/start_activity";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(!messageEvent.getPath().equalsIgnoreCase(START_ACTIVITY))
        {
            super.onMessageReceived(messageEvent);
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }
}
