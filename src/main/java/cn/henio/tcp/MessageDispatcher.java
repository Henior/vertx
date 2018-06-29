package cn.henio.tcp;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.net.NetSocket;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/27 10:30].
 */
@Component
public class MessageDispatcher {

  @Autowired
  private SessionHolder sessionHolder;

  /**
   * 分发消息
   * @param buffer
   */
  public void dispatch(Buffer buffer){
    SimpleProtocol simpleProtocol = Json.decodeValue(buffer, SimpleProtocol.class);
    System.out.println(simpleProtocol);
    //NetSocket socket = sessionHolder.getSession(simpleProtocol.getIdentifier()).getSocket();
    for (TcpSession session : sessionHolder.allSessions()){
      session.getSocket().write(simpleProtocol + "[" + new Date() + "]");
    }
  }
}
