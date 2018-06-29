package cn.henio.tcp;

import io.vertx.core.net.NetSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/26 18:57].
 */
public class TcpSession {

  private NetSocket socket;
  private Map<String, Object> extra = new HashMap<>();

  public TcpSession(NetSocket socket) {
    this.socket = socket;
  }

  public NetSocket getSocket() {
    return socket;
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
    socket.close();
  }

}
