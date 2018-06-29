package cn.henio.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/28 10:22].
 */
@Component("mqttDispatcher")
public class MessageDispatcher {

  @Autowired
  @Qualifier("mqttSessionHolder")
  private cn.henio.mqtt.SessionHolder mqttHolder;

  /**
   * 分发PUBLISH消息,数据交互核心方法
   * @param message
   * @param id
   */
  public void dispatchPub(MqttPublishMessage message, String id){

  }

  /**
   * 分发SUBSCRIBE消息
   * @param message
   * @param id
   */
  public void dispatchSub(MqttSubscribeMessage message, String id){

  }

  /**
   * 分发UNSUBSCRIBE消息
   * @param message
   * @param id
   */
  public void dispatchUnsub(MqttUnsubscribeMessage message, String id){

  }

  /**
   * 分发DISCONNECT消息
   * @param id
   */
  public void dispatchDiscon(String id){

  }

  /**
   * 分发PING消息
   * @param id
   */
  public void dispatchPing(String id){

  }

  /**
   * 发送消息
   * @param id
   * @param topic
   * @param payload
   */
  public void publish(String id, String topic, Buffer payload){
    publish(MqttQoS.AT_LEAST_ONCE, false, false, topic, payload, id);
  }

  public void publish(MqttQoS qosLevel, boolean isDup, boolean isRetain, String topicName, Buffer payload, String id){
    mqttHolder.getSession(id).getEndpoint().publish(topicName, payload, qosLevel, isDup, isRetain);
  }
}
