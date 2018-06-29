package cn.henio.mqtt;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.mqtt.MqttAuth;
import io.vertx.mqtt.MqttEndpoint;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/27 13:25].
 */
@Component("mqttSessionHolder")
public class SessionHolder {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionHolder.class);

  private final Map<String, MqttSession> sessions = new ConcurrentHashMap<>();

  public void createSession(String user, String pass, MqttEndpoint endpoint){
    MqttAuth auth = endpoint.auth();
    if(null != auth){
      LOGGER.info("MQTT client [{}] request to connect, clean session = {}" , endpoint.clientIdentifier(), endpoint.isCleanSession());
      String id = endpoint.clientIdentifier();
      if(user.equals(auth.userName()) && pass.equals(auth.password())){
        // 接受远程客户端连接
        endpoint.accept(true);
        if(sessions.containsKey(id)){
          if(endpoint.isCleanSession()){
            sessions.remove(id).close();
            sessions.put(id, new MqttSession(endpoint));
          }
        }else {
          sessions.put(id, new MqttSession(endpoint));
        }
      }else{
        // 拒绝
        endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
      }
    }
  }

  public MqttSession getSession(String id){
    return sessions.get(id);
  }

  public Collection<MqttSession> getSessions(String... ids) {
    Set<MqttSession> set = new HashSet<>();
    for (String id : ids) {
      set.add(sessions.get(id));
    }
    return set;
  }

  public Collection<MqttSession> allSessions(){
    return sessions.values();
  }

  public MqttSession removeSession(String id){
    return sessions.remove(id);
  }

  public void closeSession(String id) {
    if (null != id && id.length() > 0){
      return;
    }
    MqttSession session = removeSession(id);
    if (null != session) {
      session.close();
      LOGGER.info("DISCONNECT CLIENT <{}>", id);
    } else {
      LOGGER.warn("SESSION HAS BEEN CLOSED <{}>", id);
    }
  }
}
