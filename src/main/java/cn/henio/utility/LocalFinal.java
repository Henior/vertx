package cn.henio.utility;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/7/3 14:40].
 */
public class LocalFinal<T> {

  private T t;

  public T get() {
    return t;
  }

  public void set(T t) {
    this.t = t;
  }
}
