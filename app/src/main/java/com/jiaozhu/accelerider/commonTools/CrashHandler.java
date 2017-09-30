package com.jiaozhu.accelerider.commonTools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jiaozhu on 16/3/16.
 * 错误日志记录
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static String logPath;
    private static Context context;
    private static Thread.UncaughtExceptionHandler defaultHandler;
    private static CrashHandler crashHandler;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private CrashHandler(Context context, String logPath) {
        this.context = context;
        this.logPath = logPath;
    }


    public static void init(Context context, String logPath) {
        if (crashHandler == null) {
            crashHandler = new CrashHandler(context, logPath);
        }
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(crashHandler);
    }

    /**
     * 获取log文件路径
     *
     * @return
     */
    public static String getLogFilePath() {
        return logPath;
    }


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        StringBuilder content = new StringBuilder();
        content.append(getPhoneInfo()).append(ex.toString()).append("\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            content.append(element).append("\n");
        }
        content.append(format.format(new Date())).append("---------------------------------------------------------------\n");
        writeFile(new File(logPath), content.toString());
        if (defaultHandler == null) {
            android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            defaultHandler.uncaughtException(thread, ex);
        }

    }

    /**
     * 获取手机信息
     *
     * @return
     */
    public static Info getPhoneInfo() {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        Info info = new Info();
        try {
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            info.appName = context.getPackageName();
            info.appVersion = pi.versionName + "_" + pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        info.oSVersion = Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT;
        info.vendor = Build.MANUFACTURER;
        info.model = Build.MODEL;
        info.cpuABI = Build.CPU_ABI;
        return info;
    }

    /**
     * 写入文件
     *
     * @param file 需要写入的文件
     * @param str  需要写入的内容
     * @return 写入是否成功
     */
    private static boolean writeFile(File file, String str) {
        boolean flag = false;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            byte[] bytes = str.getBytes();
            fos.write(bytes);
            fos.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }


    public static class Info {
        public String appName;
        public String appVersion;
        public String oSVersion;
        public String vendor;
        public String model;
        public String cpuABI;

        @Override
        public String toString() {
            return "AppName:'" + appName + '\'' +
                    "\nAppVersion:'" + appVersion + '\'' +
                    "\nOSVersion:'" + oSVersion + '\'' +
                    "\nVendor:'" + vendor + '\'' +
                    "\nModel:'" + model + '\'' +
                    "\nCpuABI:'" + cpuABI + '\'' +
                    "\n\n";
        }
    }
}
