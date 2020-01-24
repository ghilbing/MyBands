package com.hilbing.mybands.utils;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public MyApplication(){
        super();
        instance = this;
    }

    public static MyApplication getApplication(){
        if(instance == null){
            instance = new MyApplication();
        }
        return instance;
    }

}
