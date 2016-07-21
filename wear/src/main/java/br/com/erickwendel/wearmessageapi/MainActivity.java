package br.com.erickwendel.wearmessageapi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private static final String ANDROID_MESSAGE_PATH = "/message1";
    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final String START_ACTIVITY = "/start_activity";

    private GoogleApiClient mApiClient;
    private ListView mListView;
    private Button mButton;
    private ArrayAdapter<String> mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("WEAR", "Inicializando");
        setContentView(R.layout.round_activity_main);

        init();
        initGoogleClient();
    }

    @Override
    protected void onDestroy() {
        if (mApiClient != null){
            mApiClient.unregisterConnectionCallbacks(this);
            mApiClient.disconnect();
        }


        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectGoogleApi();
    }


    @Override
    public void onConnected(Bundle bundle) {
        sendMessage(START_ACTIVITY, "");
        Wearable.MessageApi.addListener(mApiClient, this);
    }


    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {
                    mArrayAdapter.add(new String(messageEvent.getData()));
                    mArrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    protected void onStop() {
        if (mApiClient != null) {
            Wearable.MessageApi.removeListener(mApiClient, this);
            if (mApiClient.isConnected())
                mApiClient.disconnect();
        }
        super.onStop();
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    private void init() {
        mListView = (ListView) findViewById(R.id.list);
        mArrayAdapter = new ArrayAdapter<>(this, R.layout.list_item);
        mListView.setAdapter(mArrayAdapter);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mButton = (Button) findViewById(R.id.btn_send_wear);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(ANDROID_MESSAGE_PATH, "From Wear");
            }
        });
    }

    private void initGoogleClient() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        connectGoogleApi();
    }

    private void connectGoogleApi() {
        if (mApiClient != null && !(mApiClient.isConnected() || mApiClient.isConnecting())) {
            mApiClient.connect();
        }
    }

    private void sendMessage(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes()).await();

                }
            }
        }).start();
    }


}
