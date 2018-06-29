package cn.henio.tcp;

import io.vertx.core.json.JsonObject;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/27 10:40].
 */
public class SimpleProtocol {

  // 连接标识
  private String identifier;
  private String name;
  private String password;
  // 状态 0-开始连接，1-已连接
  private Integer type;
  private String payload;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  @Override
  public String toString(){
    return String.valueOf(JsonObject.mapFrom(this));
  }
}
