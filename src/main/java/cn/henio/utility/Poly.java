package cn.henio.utility;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/8/27 16:43].
 */
public class Poly {

  public static void poly(List<Integer> source, List<Integer> target, int limit){
    if(target.size() == limit){
      System.out.println(target);
      return;
    }
    for (Integer num : source){
      List<Integer> s = new ArrayList<>(source),t = new ArrayList<>(target);
      s.remove(num);
      t.add(num);
      poly(s,t,limit);
    }
  }
}
