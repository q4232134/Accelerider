package com.jiaozhu.accelerider.support;

import android.content.Context;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by apple on 15/10/30.
 */
public class Constants {

    public Constants(Context context) {
        FILE_DIR = context.getExternalFilesDir(null).getPath();
        CACHE_DIR = context.getExternalCacheDir().getPath();
        ANDROID_ID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        APK_VERSION = Tools.getVersion(context);
    }

    public static String FILE_DIR;
    public static String APK_VERSION;

    /**
     * 推送注册系统ID
     */
    public static final String SYS_Name = "rtm_002";
    /**
     * 推送注册用户ID
     */
    public static final String USER_UNIT = "nx_sw001";

    /**
     * 通用时间格式化
     */
    public static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");

    public static final SimpleDateFormat FORMAT_MINUTE = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final int PAGE_SIZE = 15;//一次加载条数

    /**
     * 缓存目录
     */
    public static String CACHE_DIR;

    /**
     * 数据库名称
     */
    public static final String DB_NAME = "rtm.db";
    /**
     * 数据库版本
     */
    public static final int DB_VERSION = 2;
    /**
     * UA标志
     */
    public static final String UA = "android";
    /**
     * 设备标识
     */
    public static String ANDROID_ID;
    /**
     * 历史数据显示条数
     */
    public static final int HISTORY_LENGTH = 20;

    public static boolean isDebug = true;

    public static final int RESULT_OK = 1;

    public static final int RESULT_NO = 0;

    public static final int CONNECT_TIME_OUT = 15000;
    public static final int RESPONSE_TIME_OUT = 45000;

    public static final String[] CONFIGS = {
    };

    /**
     * IP地址验证的正则表达式
     */
    public static final String IP_CHECK_STRING = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    /**
     * 缓存文件高度
     */
    public static final int CACHE_HEIGHT = 512;
    /**
     * 缓存文件宽度
     */
    public static final int CACHE_WIDTH = 512;

    /**
     * 默认查询区间
     */
    public static final long DURATION = 36L * 30L * 24L * 60L * 60L * 1000L;//查询区间为3年

    public static class Icon {
        public Integer src, bg;

        public Icon(Integer src, Integer bg) {
            this.src = src;
            this.bg = bg;
        }
    }


    /**
     * 文件类型对应的图标
     */
    public static final Map<String, Integer> FILE_ICON_MAP = getFileIconMap();

    private static Map<String, Integer> getFileIconMap() {
        Map<String, Integer> map = new Hashtable<>();

        return map;
    }

    /**
     * 文件请求表
     */
    public static final Map<String, String> MINE_TABLE = getMineTable();

    private static Map<String, String> getMineTable() {
        Map<String, String> temps = new HashMap<>();
        temps.put(".3gp", "video/3gpp");
        temps.put(".apk", "application/vnd.android.package-archive");
        temps.put(".asf", "video/x-ms-asf");
        temps.put(".avi", "video/x-msvideo");
        temps.put(".bin", "application/octet-stream");
        temps.put(".bmp", "image/bmp");
        temps.put(".c", "text/plain");
        temps.put(".class", "application/octet-stream");
        temps.put(".conf", "text/plain");
        temps.put(".cpp", "text/plain");
        temps.put(".doc", "application/msword");
        temps.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        temps.put(".xls", "application/vnd.ms-excel");
        temps.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        temps.put(".exe", "application/octet-stream");
        temps.put(".gif", "image/gif");
        temps.put(".gtar", "application/x-gtar");
        temps.put(".gz", "application/x-gzip");
        temps.put(".h", "text/plain");
        temps.put(".htm", "text/html");
        temps.put(".html", "text/html");
        temps.put(".jar", "application/java-archive");
        temps.put(".java", "text/plain");
        temps.put(".jpeg", "image/jpeg");
        temps.put(".jpg", "image/jpeg");
        temps.put(".js", "application/x-javascript");
        temps.put(".log", "text/plain");
        temps.put(".m3u", "audio/x-mpegurl");
        temps.put(".m4a", "audio/mp4a-latm");
        temps.put(".m4b", "audio/mp4a-latm");
        temps.put(".m4p", "audio/mp4a-latm");
        temps.put(".m4u", "video/vnd.mpegurl");
        temps.put(".m4v", "video/x-m4v");
        temps.put(".mov", "video/quicktime");
        temps.put(".mp2", "audio/x-mpeg");
        temps.put(".mp3", "audio/x-mpeg");
        temps.put(".mp4", "video/mp4");
        temps.put(".mpc", "application/vnd.mpohun.certificate");
        temps.put(".mpe", "video/mpeg");
        temps.put(".mpeg", "video/mpeg");
        temps.put(".mpg", "video/mpeg");
        temps.put(".mpg4", "video/mp4");
        temps.put(".mpga", "audio/mpeg");
        temps.put(".msg", "application/vnd.ms-outlook");
        temps.put(".ogg", "audio/ogg");
        temps.put(".pdf", "application/pdf");
        temps.put(".png", "image/png");
        temps.put(".pps", "application/vnd.ms-powerpoint");
        temps.put(".ppt", "application/vnd.ms-powerpoint");
        temps.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        temps.put(".prop", "text/plain");
        temps.put(".rc", "text/plain");
        temps.put(".rmvb", "audio/x-pn-realaudio");
        temps.put(".rtf", "application/rtf");
        temps.put(".sh", "text/plain");
        temps.put(".tar", "application/x-tar");
        temps.put(".tgz", "application/x-compressed");
        temps.put(".txt", "text/plain");
        temps.put(".wav", "audio/x-wav");
        temps.put(".wma", "audio/x-ms-wma");
        temps.put(".wmv", "audio/x-ms-wmv");
        temps.put(".wps", "application/vnd.ms-works");
        temps.put(".xml", "text/plain");
        temps.put(".z", "application/x-compress");
        temps.put(".zip", "application/x-zip-compressed");
        temps.put("", "*/*");
        return temps;
    }

}
