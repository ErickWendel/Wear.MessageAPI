package br.com.erickwendel.wearmessageapi;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final String ANDROID_MESSAGE_PATH = "/message1";

    private GoogleApiClient mApiClient;
    private ArrayAdapter<String> mAdapter;

    private ListView mListView;
    private EditText mEditText;
    private Button mSendButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();
        initGoogleApiClient();
    }


    @Override
    protected void onDestroy() {
        if (mApiClient != null) {
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
                if (messageEvent.getPath().equalsIgnoreCase(ANDROID_MESSAGE_PATH)) {
                    mAdapter.add(new String(messageEvent.getData()));
                    mAdapter.notifyDataSetChanged();
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
        mListView = (ListView) findViewById(R.id.list_view);
        mEditText = (EditText) findViewById(R.id.input);
        mSendButton = (Button) findViewById(R.id.btn_send);

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mListView.setAdapter(mAdapter);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    mAdapter.add(text);
                    mAdapter.notifyDataSetChanged();
                    sendMessage(WEAR_MESSAGE_PATH, text);
                    Log.i("Mobile", "Enviou a mensagem" + text);
                }

            }
        });

    }

    private void initGoogleApiClient() {
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEditText.setText("");
                    }
                });
            }
        }).start();
    }

}
