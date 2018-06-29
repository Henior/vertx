package cn.henio.mqtt;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/27 15:07].
 */
@Component
public class MqttFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttFunction.class);

  @Autowired
  private RedisClient redisClient;
  @Autowired
  private Vertx vertx;
  @Autowired
  @Qualifier("mqttSessionHolder")
  private cn.henio.mqtt.SessionHolder mqttHolder;
  @Autowired
  private MessageDispatcher messageDispatcher;

  /**
   * 自动对请求确认，特殊情况要手动确认的，设置false后自行处理
   * @param endpoint
   */
  public void autoAck(MqttEndpoint endpoint){
    endpoint.subscriptionAutoAck(true);
    endpoint.publishAutoAck(true);
  }

  /**
   * 处理连接断开请求
   * @param id
   */
  public void disconnectHandler(String id){
    LOGGER.info("Received disconnect from client");
    // 业务
    messageDispatcher.dispatchDiscon(id);
    mqttHolder.closeSession(id);
  }

  /**
   * 处理订阅请求
   * @param message
   * @param id
   */
  public void subscribeHandler(MqttSubscribeMessage message, String id){
    for (MqttTopicSubscription s : message.topicSubscriptions()) {
      LOGGER.info("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
    }
    //业务
    messageDispatcher.dispatchSub(message, id);
    redisClient.subscribeMany(message.topicSubscriptions().stream().map(MqttTopicSubscription::topicName).collect(Collectors.toList()),res -> {
      if(res.succeeded()){
        LOGGER.info("[{}]-{}",id, res.result().toString());
        vertx.eventBus().consumer("io.vertx.redis." + res.result().getString(1), msg -> {
          LOGGER.info("message-{}", msg.body().toString());
        });
      }
    });
  }

  /**
   * 处理取消订阅请求
   * @param message
   * @param id
   */
  public void unsubscribeHandler(MqttUnsubscribeMessage message, String id){
    //业务
    messageDispatcher.dispatchUnsub(message, id);
    redisClient.unsubscribe(message.topics(),res -> {
      if(res.succeeded()){
        LOGGER.info("[{}]-{}",id, "Unsubscription for " + Json.encode(message.topics()));
      }
    });
  }

  /**
   * 接受请求消息
   * @param message
   * @param id
   */
  public void publishHandler(MqttPublishMessage message, String id){
    //业务
    messageDispatcher.dispatchPub(message, id);
    redisClient.publish(message.topicName(),message.payload().toString(),res -> {
      if(res.succeeded()){
        LOGGER.info("Just received message [" + message.payload().toString(Charset.defaultCharset()) + "] with QoS [" + message.qosLevel() + "] AND id [" + message.messageId() + "]");
      }
    });
  }

  /**
   * 处理心跳请求
   * @param id
   */
  public void pingHandler(String id){
    LOGGER.info("ping!");
    // eclipse 客户端有bug，pong 不被支持，客户端没有单独ping方法，隐藏在定时器了，keepAliveInterval 参数开启
    //业务
    messageDispatcher.dispatchPing(id);
  }
}
