package com.sm.supere.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sm.supere.R;
import com.sm.supere.constant.MqClientManager;
import com.sm.supere.imps.IGetMessageCallBack;
import com.sm.supere.imps.OnHandlerListener;
import com.sm.supere.model.HandlerMessage;
import com.sm.supere.process.LoginProcess;
import com.sm.supere.process.PushProgramProcess;
import com.sm.supere.services.MQTTService;
import com.sm.supere.services.MyServiceConnection;
import com.sm.supere.utils.HandlerManager;

public class MainActivity extends AppCompatActivity implements IGetMessageCallBack, OnHandlerListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MyServiceConnection serviceConnection;
    private TextView tvReceive;
    private EditText etSendMsg;
    private EditText etSubTopic;
    private EditText etSendTopic;
    private Button btnSend;
    private Button btnSub;
    private boolean isSubTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvReceive = findViewById(R.id.tv_receive_msg);
        etSendMsg = findViewById(R.id.et_send_msg);
        btnSend = findViewById(R.id.btn_send);
        btnSub = findViewById(R.id.btn_sub);
        etSendTopic = findViewById(R.id.et_send_topic);
        etSubTopic = findViewById(R.id.et_sub_topic);
        init();
        setupService();
        HandlerManager.getInstance().setListener(this);
    }

    private void init() {
        btnSub.setOnClickListener(this);
        btnSend.setOnClickListener(this);
    }

    /**
     * 开启服务
     */
    private void setupService() {
        serviceConnection = new MyServiceConnection();
        serviceConnection.setIGetMessageCallBack(MainActivity.this);
        Intent intent = new Intent(this, MQTTService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 后台服务传递消息，回调事件
     *
     * @param topic   主题
     * @param message 消息
     */
    @Override
    public void receiveMessage(String topic, String message) {
        // 收到从mqtt服务传过来的信息，传输给控制层处理
        MqClientManager.getInstance().receive(topic, message);
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    /**
     * 处理handler消息
     *
     * @param obj 消息
     */
    @Override
    public void handlerMessage(Object obj) {
        // 控制层处理完成的数据，返回给应用层，更新UI视图
        HandlerMessage message = (HandlerMessage) obj;
        Log.d(TAG, "handlerMessage：" + message.getContent());

        switch (message.getTopic()) {
            case LoginProcess.cmd:
                tvReceive.setText(message.getContent());
                break;
            case PushProgramProcess.cmd:

                break;
            default:
                tvReceive.setText(message.getContent());
                Log.d(TAG, "receive: 垃圾信息");
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                String topic = etSendTopic.getText().toString();
                String content = etSendMsg.getText().toString();
                if (!TextUtils.isEmpty(topic) && !TextUtils.isEmpty(content)) {
                    MqClientManager.getInstance().send(topic, content, false);
                } else {
                    Toast.makeText(MainActivity.this, "请输入发布主题或者信息", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_sub:
                String topicSub = etSubTopic.getText().toString();
                if (TextUtils.isEmpty(topicSub)) {
                    Toast.makeText(MainActivity.this, "请输入订阅主题", Toast.LENGTH_SHORT).show();
                    return;
                }
                isSubTopic = !isSubTopic;
                if (isSubTopic) {
                    etSubTopic.setEnabled(false);
                    btnSub.setText("取消订阅");
                    MqClientManager.getInstance().subscribe(topicSub);
                } else {
                    etSubTopic.setEnabled(true);
                    btnSub.setText("订阅");
                    MqClientManager.getInstance().unSubscribe(topicSub);
                }
                break;
        }
    }
}