package com.cbis.jira.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Utils {

    public static void main(String[] args) {
       // System.out.println(getPercent(10.0,10.0));
    }

    public static String getPercent(double number,double total){

        if(total == 0){
            return "0%";
        }
        //0表示的是小数点  之前没有这样配置有问题例如  num=1 and total=1000  结果是.1  很郁闷
        DecimalFormat df = new DecimalFormat("0%");
        //可以设置精确几位小数
        df.setMaximumFractionDigits(1);
        //模式 例如四舍五入
        df.setRoundingMode(RoundingMode.HALF_UP);
        double accuracy_num = number/total;
        return String.valueOf(df.format(accuracy_num));
    }
}
