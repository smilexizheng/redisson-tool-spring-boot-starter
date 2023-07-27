package org.smilexizheng.idgeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestID {

    public static void main(String[] args) {
        SnowflakeId id = new SnowflakeId(2, 31);
//        SnowflakeId id2 = new SnowflakeId(1,31);
        int i = 0;
        long a = System.currentTimeMillis();
        List<Long> Arr = new ArrayList<>();
//        while (i<10000){
//            Arr.add(id.getNext());
//            i++;
////            System.out.println(id.getNextId());
//        }
//        System.out.printf("耗时: %dms ,生成ID: %d个",System.currentTimeMillis()-a,Arr.size());


        int n = 666;
        int m = 7777;
        n = n ^ m;
        m = m ^ n;  //m = m ^ (n ^ m) => m=n
        n = n ^ m;  //n = (n ^ m)^[m ^ (n ^ m)] => n=m
        System.out.println(n + ";" + m); //3;2
        int count = 0;
        int[] t = {10, 6, 4, 7, 8, 9, 1, 2, 5, 3};

//        for ( i = 0; i < t.length-1; i++) {
//            //第一轮，两两比较
//            for (int j = 0; j < t.length-1-i; j++) {
//                if (t[j]<t[j+1]) {
//                    int temp=t[j];
//                    t[j]=t[j+1];
//                    t[j+1]=temp;
//                }
//                count++;
//            }
//        }

        for (i = 0; i < t.length - 1; i++) {
            for (int j = 0; j < t.length - 1 - i; j++) {
                if (t[j] < t[j + 1]) {
                    t[j] ^= t[j + 1];
                    t[j + 1] ^= t[j];
                    t[j] ^= t[j + 1];
                }
                count++;
            }

        }
        System.out.println(Arrays.toString(t));
        System.out.println(count);


    }
}
