package com.xmg.smacktest.utils;

import java.util.Stack; 

import android.app.Activity; 
import android.app.ActivityManager; 
import android.app.Application;
import android.content.Context; 
 
/**
 * @date 2015-3-6
 * 应用程序Activity管理类
 */
public class AppManager extends Application{ 
     
    private static Stack<Activity> activityStack; 
    private static AppManager instance; 
     
    private AppManager(){} 
    /**
     * 单一实例
     */ 
    public static AppManager getAppManager(){ 
        if(instance==null){ 
            instance=new AppManager(); 
        } 
        return instance; 
    } 
    /**
     * 添加Activity到堆栈
     */ 
    public void addActivity(Activity activity){ 
        if(activityStack==null){ 
            activityStack=new Stack<Activity>(); 
        } 
        activityStack.add(activity); 
    } 
    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */ 
    public Activity currentActivity(){ 
        Activity activity=activityStack.lastElement(); 
        return activity; 
    } 
    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */ 
    public void finishActivity(){ 
        Activity activity=activityStack.lastElement(); 
        if(activity!=null){ 
            activity.finish(); 
            activity=null; 
        } 
    } 
    /**
     * 结束指定的Activity
     */ 
    public void finishActivity(Activity activity){ 
        if(activity!=null){ 
            activityStack.remove(activity); 
            activity.finish(); 
            activity=null; 
        } 
    } 
    /**
     * 结束指定类名的Activity
     */ 
    public void finishActivity(Class<?> cls){ 
        for (Activity activity : activityStack) { 
            if(activity.getClass().equals(cls) ){ 
                finishActivity(activity); 
            } 
        } 
    } 
    /**
     * 结束所有Activity
     */ 
    public void finishAllActivity(){ 
        for (int i = 0, size = activityStack.size(); i < size; i++){ 
            if (null != activityStack.get(i)){ 
                activityStack.get(i).finish(); 
            } 
        } 
        activityStack.clear(); 
    } 
    /**
     * 退出应用程序
     */ 
	public void AppExit(Context context) { 
        try { 
            finishAllActivity(); 
            ActivityManager activityMgr= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE); 
            activityMgr.restartPackage(context.getPackageName()); 
            System.exit(0); 
        } catch (Exception e) { } 
    } 
    
    //杀进程  
    public void onLowMemory() {   
        super.onLowMemory();       
        System.gc();   
    }    
} 