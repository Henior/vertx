package cn.henio.http;

import cn.henio.Application;
import cn.henio.AutoRegisterVerticle;
import cn.henio.utility.LocalFinal;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.topic.impl.DataAwareMessage;
import io.vertx.ext.web.Router;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/7/3 14:23].
 */
@Component
public class HazelcastVerticle extends AutoRegisterVerticle {

  @Autowired
  private Router router;

  @Override
  public void start() throws Exception {
    LocalFinal<String> str = new LocalFinal<>();
    ITopic<String> topic = Application.getHazelcastInstance().getTopic("hzel.test");
    topic.addMessageListener(msg -> {
      str.set(msg.getMessageObject());
    });
    router.get("/hazelcast/receive").handler(rc -> {
      rc.response().end(str.get() + "");
    });
  }
}
