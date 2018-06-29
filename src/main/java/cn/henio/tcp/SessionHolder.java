package cn.henio.tcp;

import io.vertx.core.net.NetSocket;
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
 * @date: created in [2018/6/26 18:55].
 */
@Component
public class SessionHolder {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionHolder.class);

  private final Map<String, TcpSession> sessions = new ConcurrentHashMap<>();
  private final Map<String, String> idMappings = new ConcurrentHashMap<>();

  public void createSession(String id, NetSocket socket){
    TcpSession session = sessions.remove(id);
    if(null != session){
      idMappings.remove(session.getSocket().writeHandlerID());
      session.close();
    }
    sessions.put(id, new TcpSession(socket));
    // socketID------userId
    idMappings.put(socket.writeHandlerID(), id);
  }

  public TcpSession getSession(String id){
    return sessions.get(id);
  }

  public Collection<TcpSession> getSessions(String... ids) {
    Set<TcpSession> set = new HashSet<>();
    for (String id : ids) {
      set.add(sessions.get(id));
    }
    return set;
  }

  public Collection<TcpSession> allSessions(){
    return sessions.values();
  }

  public TcpSession removeSession(NetSocket socket){
    String id = getId(socket);
    TcpSession session = null;
    if(null != id && id.length() > 0){
      session = sessions.remove(id);
      if (null != session) {
        idMappings.remove(session.getSocket().writeHandlerID());
      }
    }
    return session;
  }

  public void closeSession(String id) {
    if (null != id && id.length() > 0){
      TcpSession session = sessions.remove(id);
      if (null != session) {
        idMappings.remove(session.getSocket().writeHandlerID());
        session.close();
        LOGGER.info("DISCONNECT CLIENT <{}>", id);
      } else {
        LOGGER.warn("SESSION HAS BEEN CLOSED <{}>", id);
      }
    }
  }

  public void closeSession(NetSocket socket) {
    closeSession(getId(socket));
  }

  public String getId(NetSocket socket){
    return idMappings.get(socket.writeHandlerID());
  }
}
