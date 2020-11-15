package com.ld;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.amazonaws.logging.LogFactory;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttSubscriptionStatusCallback;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;

/**
 * 创建时间：2020/11/6  17:33
 * 作者：5#
 * 描述：TODO
 * 注意：
 */
public class IotTestActivity extends Activity {
    public static final String TAG = "LDAWS";
    private AWSIotMqttManager mAwsIotMqttManager;
    private String mIdentityId;
    private boolean isConnected = false;

    private String topic = "myTopic/1";
    private EditText mShowEt;
    private StringBuffer mSb;
    private EditText publishEt;

    private  AWSIotMqttNewMessageCallback awsIotMqttNewMessageCallback = new AWSIotMqttNewMessageCallback() {
        @Override
        public void onMessageArrived(String topic, byte[] data) {
            Log.e(TAG, "==========onCreate==onMessageArrived topic==" + topic);
            String s = new String(data);
            setEtText(topic);
            setEtText(s);
            Log.e(TAG, "==========onCreate==onMessageArrived data==" + s);
        }
    };


    private AWSIotMqttSubscriptionStatusCallback subscriptionStatusCallback = new AWSIotMqttSubscriptionStatusCallback() {
        @Override
        public void onSuccess() {
            Log.e(TAG, "==========onCreate==subscribeToTopic=onSuccess");
            setEtText("MQTT subscribeToTopic  onSuccess");
        }

        @Override
        public void onFailure(Throwable exception) {
            Log.e(TAG, "==========onCreate==subscribeToTopic=onFailure");
            setEtText("MQTT subscribeToTopic  onFailure ：\n" + exception.getLocalizedMessage());
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_test);


        LogFactory.setLevel(LogFactory.Level.ALL);

        mIdentityId = AWSMobileClient.getInstance().getIdentityId();
        Log.e(TAG, mIdentityId);

        Log.e(TAG, "-===" + LogFactory.getLevel());

        mShowEt = (EditText) findViewById(R.id.show_result_et);
        publishEt = (EditText) findViewById(R.id.publish_et);
        mSb = new StringBuffer();



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String endPoint = "a32rgv79k6sf8k-ats.iot.eu-west-1.amazonaws.com";

                    mAwsIotMqttManager = new AWSIotMqttManager(mIdentityId, endPoint);
                    mAwsIotMqttManager.setKeepAlive(10);
                    mAwsIotMqttManager.setCleanSession(true);

                    Log.e(TAG, "==========onCreate==" + mIdentityId);

                    //关联策略 需要添加权限策略
                    AttachPolicyRequest attachPolicyReq = new AttachPolicyRequest();
                    attachPolicyReq.setPolicyName("iot-policy"); // name of your IOT AWS policy
                    attachPolicyReq.setTarget(AWSMobileClient.getInstance().getIdentityId());
                    AWSIotClient mIotAndroidClient = new AWSIotClient(AWSMobileClient.getInstance());
                    mIotAndroidClient.setRegion(Region.getRegion("eu-west-1")); // name of your IoT Region such as "us-east-1"
                    mIotAndroidClient.attachPolicy(attachPolicyReq);

                    mAwsIotMqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                        @Override
                        public void onStatusChanged(AWSIotMqttClientStatus awsIotMqttClientStatus, Throwable throwable) {
                            Log.e(TAG, "onStatusChanged===" + awsIotMqttClientStatus.toString());
                            if (awsIotMqttClientStatus == AWSIotMqttClientStatus.Connected) {
                                Log.e(TAG, "==========onCreate===Connected");
                                isConnected = true;
                                setEtText("MQTT Connected");
                                mAwsIotMqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0, subscriptionStatusCallback, awsIotMqttNewMessageCallback);
                            } else if (awsIotMqttClientStatus == AWSIotMqttClientStatus.ConnectionLost) {
                                Log.e(TAG, "==========onCreate==ConnectionLost");
                                isConnected = false;
                                setEtText("MQTT ConnectionLost");
                            } else if (awsIotMqttClientStatus == AWSIotMqttClientStatus.Reconnecting) {
                                Log.e(TAG, "==========onCreate==Reconnecting");
                                isConnected = false;
                                setEtText("MQTT Reconnecting");
                            } else if (awsIotMqttClientStatus == AWSIotMqttClientStatus.Connecting) {
                                Log.e(TAG, "==========onCreate==Connecting");
                                isConnected = false;
                                setEtText("MQTT Connecting");
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


        findViewById(R.id.publish_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String publishStr = publishEt.getText().toString().trim();
                if ("".equals(publishStr)) {
                    publishStr = "default string : test lz";
                }
                if (mAwsIotMqttManager != null && isConnected) {
                    mAwsIotMqttManager.publishString(publishStr, topic, AWSIotMqttQos.QOS0);
                } else {
                    Toast.makeText(IotTestActivity.this, "等待连接", Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.subscribe_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAwsIotMqttManager != null && isConnected) {
                    mAwsIotMqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0, subscriptionStatusCallback, awsIotMqttNewMessageCallback);
                }
            }
        });
        findViewById(R.id.unsubscribe_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAwsIotMqttManager != null) {
                    mAwsIotMqttManager.unsubscribeTopic(topic);
                }
            }
        });
        findViewById(R.id.clear_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSb.delete(0,mSb.length());
                mShowEt.setText(mSb);
            }
        });

    }

    private void setEtText(String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSb.append(text + "\n");
                mShowEt.setText(mSb);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAwsIotMqttManager != null) {
            mAwsIotMqttManager.disconnect();
        }
    }
}
