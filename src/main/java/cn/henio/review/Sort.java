package cn.henio.review;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/4/15 11:34].
 */
public class Sort {

  /**
   * 交换数值
   * @param so
   * @param a
   * @param b
   */
  private static  void swap(Integer[] so, int a, int b){
    so[a] = so[a] ^ so[b];
    so[b] = so[a] ^ so[b];
    so[a] = so[a] ^ so[b];
  }

  /**
   * 选择
   * @param so
   */
  public static void select(Integer[] so){
    for (int i = 0; i < so.length - 1; i++) {
      for(int j = i + 1; j < so.length; j++){
        if(so[i] > so[j]){
          swap(so, i, j);
        }
      }
    }
  }

  /**
   * 冒泡
   * @param so
   */
  public static void bubble(Integer[] so){
    for (int i = 0; i < so.length - 1; i++) {
      for (int j = 0; j < so.length - 1 - i; j++){
        if(so[j] > so[j + 1]){
          swap(so, j, j+1);
        }
      }
    }
  }

  /**
   * 快排
   * @param so
   * @param left
   * @param right
   */
  public static void quick(Integer[] so, int left, int right){
    Integer temp = so[left];
    int l = left,r = right;
    while(left < right){
      while(left < right && so[right] >= temp){
        right--;
      }
      so[left] = so[right];
      while(left < right && so[left] <= temp){
        left++;
      }
      so[right] = so[left];
    }
    so[left] = temp;
    if(l < r){
      quick(so, l,left - 1);
      quick(so,left + 1, r);
    }
  }

  /**
   * 插入
   * @param so
   */
  public static void insert(Integer[] so){
    for (int i = 0; i < so.length - 1; i++) {
      for (int j = i + 1; j > 0 && so[j] < so[j - 1]; j--){
        swap(so, j, j - 1);
      }
    }
  }

  /**
   * 希尔
   * @param so
   */
  public static void shell(Integer[] so){
    for (int step = so.length /  2; step > 0; step /= 2) {
      for (int j = step; j < so.length; j++){
        for (int k = j; k >= step && so[k] < so[k - step]; k -= step){
          swap(so, k, k - step);
        }
      }
    }
  }

  /**
   * 桶排序（只适用于整数）
   * @param so
   */
  public static void bucket(Integer[] so){
    int max = so[0], min = so[0];
    for (int i = 1; i < so.length; i++) {
      max = max < so[i] ? so[i] : max;
      min = min > so[i] ? so[i] : min;
    }
    int[] buck = new int[max - min + 1];
    for (int i = 0; i < so.length; i++) {
      buck[so[i] - min] += 1;
    }
    List<Integer> result = new LinkedList<>();
    for (int i = 0; i < buck.length; i++) {
      if(buck[i] == 1){
        result.add(i + min);
      }else if (buck[i] > 1){
        for (int j = 0; j < buck[i]; j++) {
          result.add(i + min);
        }
      }
    }
    for (int i = 0; i < so.length; i++) {
      so[i] = result.get(i);
    }
  }

  /**
   * 归并
   * @param so
   * @param left
   * @param right
   */
  public static void merge(Integer[] so, int left, int right){
    if (left < right){
      int center = (left + right) / 2;
      merge(so, left, center);
      merge(so, center + 1, right);

      int l1 = left, l2 = center + 1;
      int origin = left;
      Integer[] tmp = new Integer[so.length];
      while (l1 <= center && l2 <= right){
        if(so[l1] < so[l2]){
          tmp[left++] = so[l1++];
        }else{
          tmp[left++] = so[l2++];
        }
      }
      while(l1 <= center){
        tmp[left++] = so[l1++];
      }
      while(l2 <= right){
        tmp[left++] = so[l2++];
      }
      while(origin <= right){
        so[origin] = tmp[origin++];
      }
    }
  }

  /**
   * 堆排序
   * @param so
   */
  public static void heap(Integer[] so){
    for (int i = so.length - 1; i > 0; i--) {
      //非叶子节点
      for (int j = (i - 1) / 2; j >= 0; j--){
        int maxIndex = 2 * j + 1;
        //左右叶子节点比较，前提是有右叶子节点
        if (i >= maxIndex + 1 && so[maxIndex] < so[maxIndex + 1]){
          maxIndex += 1;
        }
        if (so[maxIndex] > so[j]){
          swap(so, maxIndex, j);
        }
      }
      swap(so, 0, i);
    }
  }

  public static void main(String[] args) {
    Integer[] source = new Integer[]{23,6,1,90,-2,-5,11,5,9,25,12,22,32,71,35,51,9};
    System.out.println("source:         " + Arrays.asList(source));
//    select(source);
//    System.out.println("select resource: " + Arrays.asList(source));
//    bubble(source);
//    System.out.println("bubble resource: " + Arrays.asList(source));
//    quick(source, 0, source.length - 1);
//    System.out.println("quick resource: " + Arrays.asList(source));
//    shell(source);
//    System.out.println("shell resource: " + Arrays.asList(source));
//    insert(source);
//    System.out.println("insert resource: " + Arrays.asList(source));
//    bucket(source);
//    System.out.println("bucket resource: " + Arrays.asList(source));
//    merge(source, 0, source.length - 1);
//    System.out.println("merge resource: " + Arrays.asList(source));
    heap(source);
    System.out.println("heap resource: " + Arrays.asList(source));
  }
}