package com.vise.bledemo.myClass;

import android.util.Log;

/**
 * Created by 3ivr on 2018/1/9.
 */

public class MyLogClass {
    public static void Log_d(String tag, String msg){
        Log.d("tag", msg+ "__["+ Thread.currentThread().getStackTrace()[2].getMethodName()+ "__"+Thread.currentThread().getStackTrace()[2].getLineNumber()+"]");
    }
}

