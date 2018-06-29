package cn.henio.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttTopicSubscription;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/27 13:26].
 */
public class MqttSession {

  private MqttEndpoint endpoint;
  private Map<String, Object> extra = new HashMap<>();

  public MqttSession(MqttEndpoint endpoint) {
    this.endpoint = endpoint;
    //init(endpoint);
  }

  public void init(MqttEndpoint endpoint){
    endpoint.disconnectHandler(v -> {
      System.out.println("Received disconnect from client");
    });

    endpoint.subscribeHandler(subscribe -> {
      List<MqttQoS> grantedQosLevels = new ArrayList<>();
      for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
        System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
        grantedQosLevels.add(s.qualityOfService());
      }
      // 确认订阅请求
      endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
    });

    endpoint.unsubscribeHandler(unsubscribe -> {
      for (String t : unsubscribe.topics()) {
        System.out.println("Unsubscription for " + t);
      }
      // 确认订阅请求
      endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
    });

    endpoint.publishHandler(message -> {
      System.out.println("Just received message [" + message.payload().toString(Charset.defaultCharset()) + "] with QoS [" + message.qosLevel() + "] AND id [" + message.messageId() + "]");
      endpoint.publish(message.topicName(), message.payload(), message.qosLevel(), false, false);
      if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
        endpoint.publishAcknowledge(message.messageId());
      } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
        System.out.println("send received");
        endpoint.publishReceived(message.messageId());
      }
    }).publishReleaseHandler(messageId -> {
      System.out.println("send complete");
      endpoint.publishComplete(messageId);
    });

    // 例子, 发布一个QoS级别为2的消息
    endpoint.publish("my_topic", Buffer.buffer("Hello from the Vert.x MQTT server"), MqttQoS.EXACTLY_ONCE, false, false);
    // 选定handlers处理QoS 1与QoS 2
    endpoint.publishAcknowledgeHandler(messageId -> {
      System.out.println("Publish ack for message = " + messageId);
    }).publishReceivedHandler(messageId -> {
      System.out.println("Received ack for message = " + messageId);
      endpoint.publishRelease(messageId);
    }).publishCompleteHandler(messageId -> {
      System.out.println("Complete ack for message = " + messageId);
    });

    endpoint.pingHandler(v -> {
      System.out.println("ping!");
      // eclipse 客户端有bug，pong 不被支持，客户端没有单独ping方法，隐藏在定时器了，keepAliveInterval 参数开启
      //endpoint.pong();
    });
  }

  public MqttEndpoint getEndpoint() {
    return endpoint;
  }

  public void add(String key, Object val){
    extra.put(key,val);
  }

  public <T> T get(String key, Class<T> clazz){
    return clazz.cast(get(key));
  }

  public Object get(String key){
    return extra.get(key);
  }

  public void close(){
    endpoint.close();
  }
}
