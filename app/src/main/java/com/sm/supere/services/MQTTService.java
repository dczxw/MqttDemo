package com.sm.supere.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.sm.supere.R;
import com.sm.supere.constant.Constant;
import com.sm.supere.imps.IGetMessageCallBack;
import com.sm.supere.process.CommonProcess;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 服务层
 */
public class MQTTService extends Service {

    public static final String TAG = MQTTService.class.getSimpleName();
    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;
    private String host = "";
    private String userName = "";
    private String passWord = "";
    private static String clientId = "69f65a2e91e6051a23abe7293bf3386a";          // 12306 md5加密用作客户端标识
    private IGetMessageCallBack IGetMessageCallBack;
    private ScheduledExecutorService reconnectPool;//重连线程池


    public MQTTService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSys();
        init();
    }

    private void initSys() {
        host = String.format("tcp://%s:%s", Constant.server_host, Constant.server_port);
        userName = Constant.mqtt_user;
        passWord = Constant.mqtt_pwd;
    }

    /**
     * 发布信息
     *
     * @param topic 主题
     * @param msg   消息
     */
    public static void publishCommon(String topic, String msg) {
        try {
            if (client != null) {
                client.publish(topic, msg.getBytes(), 0, false);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布信息
     *
     * @param topic 主题
     * @param msg   消息
     */
    public static void publish(String topic, String msg) {
        try {
            if (client != null) {
                String commonTopic = String.format("mz/s/%s/%s", clientId, topic);
                client.publish(commonTopic, msg.getBytes(), 0, false);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅主题
     * @param topic 主题
     */
    public static void subscribe(String topic) {
        if (isConnected()) {
            try {
                client.unsubscribe(topic);         // 先取消订阅
                client.subscribe(topic, 1);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 取消订阅主题
     * @param topic 主题
     */
    public static void unSubscribe(String topic) {
        if (isConnected()) {
            try {
                client.unsubscribe(topic);         // 先取消订阅
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean isConnected() {
        return client.isConnected();
    }

    private void init() {
        // 服务器地址（协议+地址+端口号）
        String uri = host;
        client = new MqttAndroidClient(this, uri, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(true);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(20);
        // 用户名
        conOpt.setUserName(userName);
        // 密码
        conOpt.setPassword(passWord.toCharArray());     //将字符串转换为字符串数组

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + clientId + "\"}";
        Log.e(getClass().getName(), "message是:" + message);
        if (!message.equals("")) {
            // 最后的遗嘱
            // MQTT本身就是为信号不稳定的网络设计的，所以难免一些客户端会无故的和Broker断开连接。
            //当客户端连接到Broker时，可以指定LWT，Broker会定期检测客户端是否有异常。
            //当客户端异常掉线时，Broker就往连接时指定的topic里推送当时指定的LWT消息。
            try {
                String topic = formatPublishTopic("");
                conOpt.setWill(topic, message.getBytes(), 0, false);
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }

        if (doConnect) {
            doClientConnection();
        }
    }


    @Override
    public void onDestroy() {
        stopSelf();
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        if (!client.isConnected() && isConnectIsNormal()) {
            try {
                client.connect(conOpt, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开启线程池，重连...
     */
    private synchronized void startReconnectTask() {
        if (reconnectPool != null) return;
        reconnectPool = Executors.newScheduledThreadPool(1);
        reconnectPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doClientConnection();
            }
        }, 0, 5 * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 关闭重连机制
     */
    private synchronized void closeReconnectTask() {
        if (reconnectPool != null) {
            reconnectPool.shutdownNow();
            reconnectPool = null;
        }
    }

    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try {
                closeReconnectTask();
                // 订阅话题
                String commonTopic = String.format("mz/c/%s/+", clientId);
                client.unsubscribe(commonTopic);         // 先取消订阅
                client.subscribe(commonTopic, 1);   // 然后再订阅
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            Log.d(TAG, "onFailure: 连接失败," + arg1.getMessage());
            startReconnectTask();
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            Log.i(TAG, "收到消息：" + str2);
            String content = new String(message.getPayload());
            if (IGetMessageCallBack != null && !TextUtils.isEmpty(topic)) {

                String[] split = topic.split("/");
                if (split.length == 4) {
                    String receiveId = split[2];
                    String cmd = split[3];
                    if (receiveId.equals(clientId)) {
                        IGetMessageCallBack.receiveMessage(cmd, content);
                    } else {
                        Log.d(TAG, "收到垃圾信息:" + topic);
                    }
                }else {
                    IGetMessageCallBack.receiveMessage(CommonProcess.cmd, content);
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }


        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接，重连
            startReconnectTask();
        }
    };

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.e(getClass().getName(), "onBind");
        return new CustomBinder();
    }

    /**
     * @param IGetMessageCallBack 设置回调事件
     */
    public void setIGetMessageCallBack(IGetMessageCallBack IGetMessageCallBack) {
        this.IGetMessageCallBack = IGetMessageCallBack;
    }

    public class CustomBinder extends Binder {
        public MQTTService getService() {
            return MQTTService.this;
        }
    }


    private String formatPublishTopic(String topic) {
        if (TextUtils.isEmpty(topic)) topic = "topic";
        return String.format("mz/s/%s/%s", clientId, topic);
    }
}
