package com.jingrui.usercenter.service;

import com.jingrui.usercenter.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * 算法工具测试类
 */
public class AlgorithmUtilsTest {

   @Test
    void test() {
       String str1 = "鱼皮是狗";
       String str2 = "鱼皮不是狗";
       String str3 = "鱼皮是猫不是狗";
//       int score1 = AlgorithmUtils.minDistance(str1, str2);
//       int score2 = AlgorithmUtils.minDistance(str1, str3);
//       System.out.println(score1);
//       System.out.println(score2);
   }

   @Test
   void test1() {

      List<String> strings = Arrays.asList("java","大一","c++","python");
      List<String> strings1 = Arrays.asList("女","大一","python");
      List<String> strings2 = Arrays.asList("男","大一","python");
      List<String> strings3 = Arrays.asList("男","大四","python","java");

      int score1 = AlgorithmUtils.minDistance(strings, strings1);
       int score2 = AlgorithmUtils.minDistance(strings, strings2);
       int score3 = AlgorithmUtils.minDistance(strings, strings3);
      System.out.println(score1);
      System.out.println(score2);
      System.out.println(score3);


   }
}
