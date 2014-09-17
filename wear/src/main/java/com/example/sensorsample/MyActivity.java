package com.example.sensorsample;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MyActivity extends Activity implements SensorEventListener{
    private final String TAG = MyActivity.class.getName();
    private final float GAIN = 0.9f;
    private final String[] SEND_MESSAGES = {"/Action/NONE", "/Action/PUNCH", "/Action/UPPER", "/Action/HOOK"};

    private TextView mTextView;
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private String mNode;
    private float x,y,z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "onConnected");

//                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                                //Nodeは１個に限定
                                if (nodes.getNodes().size() > 0) {
                                    mNode = nodes.getNodes().get(0).getId();
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended");

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed : " + connectionResult.toString());
                    }
                })
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = (x * GAIN + event.values[0] * (1 - GAIN));
            y = (y * GAIN + event.values[1] * (1 - GAIN));
            z = (z * GAIN + event.values[2] * (1 - GAIN));

            if (mTextView != null) mTextView.setText(String.format("X : %f\nY : %f\nZ : %f\n",x, y, z));

            int motion;
            motion = detectMotion(x, y, z);

            if (motion > 0 && mNode != null ) {
//                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, SEND_MESSAGES[motion], null).await();
//                if (!result.getStatus().isSuccess()) {
//                    Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
//                }
                Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, SEND_MESSAGES[motion], null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
                        }
                    }
                });
            }
        }
    }

    /**
     * 超適当な判定
     *
     */
    float ox,oy,oz;
    int delay;
    private int detectMotion(float x, float y, float z) {
        int diffX = (int)((x - ox)*10);
        int diffY = (int)((y - oy)*10);
        int diffZ = (int)((z - oz)*10);
        int motion = 0;

        Log.d(TAG, "s:" + diffX + "/" + diffY + "/" + diffZ + " - " + (int)x + "/" + (int)y + "/" + (int)z);
        if (Math.abs(diffZ) > 20) {
            Log.d(TAG, "upper!");
            motion = 2;
            delay = 4;
        } else if (Math.abs(diffY) > 20) {
            Log.d(TAG, "hook!");
            motion = 3;
            delay = 4;
        } else if (diffX > 10) {
            if (delay == 0) {
                Log.d(TAG, "punch!");
                motion = 1;
            }
        }

        if (delay > 0) delay--;
        ox = x;
        oy = y;
        oz = z;
        return motion;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
