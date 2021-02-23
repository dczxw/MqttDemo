# MqttDemo
使用Mqtt搭建的demo，包括服务创建，主题订阅，消息发送，断线重连...

### MQTTService
 服务层，用于后台运行，连接服务端，传输消息。
 
### MqClientManager
 控制层，用于处理消息，监听订阅主题。

### HandlerManager
本地线程间消息传输，用以收到消息后，触发UI更新
