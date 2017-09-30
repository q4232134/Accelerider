package com.jiaozhu.accelerider.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jiaozhu.accelerider.commonTools.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2014/6/20.
 */
public class Tools {
    /**
     * 从文件流中间获取字符串
     *
     * @param stream
     * @return
     */
    public static String getStringFromStream(InputStream stream) {
        try {
            InputStreamReader inputReader = new InputStreamReader(stream);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 写入文件
     *
     * @param file 需要写入的文件
     * @param str  需要写入的内容
     * @return 写入是否成功
     */
    public static boolean writeFile(File file, String str) {
        boolean flag = false;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = str.getBytes();
            fos.write(bytes);
            fos.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 检查是否为空
     *
     * @param texts 需要进行检查的view列表
     * @return 是否通过检查
     */
    public static boolean checkEmpty(TextView... texts) {
        for (TextView temp : texts) {
            if (temp.getText().length() < 1) {
                temp.requestFocus();
                temp.requestFocusFromTouch();
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否为平板设备
     *
     * @return
     */
    public static boolean isTabletDevice(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int type = telephony.getPhoneType();
        return type == TelephonyManager.PHONE_TYPE_NONE;
    }

    /**
     * 设置除了标题之外的字体大小
     *
     * @param view  父view
     * @param title 标题View
     * @param size  需要变更的大小
     */
    public static void setAllTextSize(View view, TextView title, int size) {
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewChild = vp.getChildAt(i);
                if (viewChild instanceof ViewGroup) {
                    setAllTextSize(viewChild, title, size);
                }
                if (viewChild instanceof TextView && title != viewChild) {
                    ((TextView) viewChild).setTextSize(size);
                }
            }
        }
    }


    private static void showDialog(Context context, final EditText view, final String... items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                context);
        builder.setTitle(Html.fromHtml("请选择数据"));
        builder.setSingleChoiceItems(items, -1, new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                view.setText((items[which].split("-", 2))[1]);
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 将Bitmap保存为文件
     *
     * @param bitmap
     * @param path   路径
     */
    public static void saveBitmap(Bitmap bitmap, String path, String fileName) {
        File file = new File(path, fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件成为bitMap
     *
     * @param path
     * @return
     */
    public static Bitmap getBitmap(String path) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path);
            bitmap = rotaImageView(readPictureDegree(path), bitmap);
        } catch (Exception e) {
        }
        return bitmap;
    }

    /**
     * 压缩图片
     *
     * @param image 图片
     * @param hh    目标高度
     * @param ww    目标宽度
     * @return
     */
    public static Bitmap comp(Bitmap image, float hh, float ww) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {
            //判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap;
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    private static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 根据Uri来获取图片
     *
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        BitmapDrawable bm = new BitmapDrawable(context.getResources(), getPathFromUri(context, uri));
        return bm.getBitmap();
    }

    /**
     * 根据Uri获取图片路径
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getPathFromUri(Context context, Uri uri) {
        String[] filePathColumns = {MediaStore.Images.Media.DATA};
        Cursor c = context.getContentResolver().query(uri, filePathColumns, null, null,
                null);
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePathColumns[0]);
        String picturePath = c.getString(columnIndex);
        c.close();
        return picturePath;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread;
            File oldfile = new File(oldPath);
            File newfile = new File(newPath);
            if (!newfile.exists()) {
                newfile.createNewFile();
            }
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            Log.e("", "复制单个文件操作出错");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     * 1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     * 2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     * 用这个工具生成的图像不会被拉伸。
     *
     * @param imagePath 图像的路径
     * @param width     指定输出图像的宽度
     * @param height    指定输出图像的高度
     * @return 生成的缩略图
     */
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap;
        int degree = readPictureDegree(imagePath);
        //如果照片为横向的话交换宽高比
        if (degree == 90 || degree == 270) {
            int temp = height;
            height = width;
            width = temp;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        //根据拍摄旋转图片
        return rotaImageView(readPictureDegree(imagePath), bitmap);
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                           int kind) {
        Bitmap bitmap;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return
     */
    public static Bitmap rotaImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = null;
        try {
            resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {

        }
        return resizedBitmap;
    }

    public static String md516(String paramString) {
        return md5Encode(paramString).substring(8, 24);
    }

    /**
     * MD5加密
     *
     * @param str
     * @return
     */
    public static String md5Encode(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuilder buf = new StringBuilder("");
            for (byte aB : b) {
                i = aB;
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void openFile(File file, Context context, String fileType) throws Exception {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型
        String type = Constants.MINE_TABLE.get(fileType);
        //设置intent的data和Type属性。
        intent.setDataAndType(Uri.fromFile(file), type);
        //跳转
        context.startActivity(intent);
    }

    /**
     * 字节数组转十进制
     *
     * @param paramArrayOfByte
     * @return
     */
    public static String getDecString(byte[] paramArrayOfByte) {
        long l1 = 0L;
        long l2 = 1L;
        for (int i = 0; ; ++i) {
            if (i >= paramArrayOfByte.length)
                return String.valueOf(l1);
            l1 += l2 * (0xFF & paramArrayOfByte[i]);
            l2 *= 256L;
        }
    }

    /**
     * 清除数据库
     *
     * @param context
     * @return
     */
    public static boolean deleteDataBase(Context context) {
        return context.deleteDatabase(Constants.DB_NAME);
    }


    /**
     * 获取或者生成缩略图
     *
     * @param fileName
     * @param path
     * @return
     */
    public static Bitmap getImageCache(String fileName, String path) {
        Bitmap temp = Tools.getBitmap(getCachePath(fileName, path));
        String filePath = path + "/" + fileName;
        //如果没有缩略图则生成缩略图
        if (temp == null) {
            File file = new File(filePath);
            //只有当源文件存在的时候才生成缩略图
            if (!file.exists()) return null;
            temp = Tools.getImageThumbnail(filePath, Constants.CACHE_WIDTH, Constants.CACHE_HEIGHT);
            Tools.saveBitmap(temp, Constants.CACHE_DIR, fileName + ".cache");
        }
        return temp;
    }

    /**
     * 获取缩略图路径
     *
     * @param fileName
     * @param path
     * @return
     */
    public static String getCachePath(String fileName, String path) {
        return Constants.CACHE_DIR + "/" + fileName + ".cache";
    }

    /**
     * 获取视频缩略图
     *
     * @param fileName
     * @param path
     * @return
     */
    public static Bitmap getVideoCache(String fileName, String path) {
        Bitmap temp = Tools.getBitmap(Constants.CACHE_DIR + "/" + fileName + ".cache");
        String filePath = path + "/" + fileName;
        //如果没有缩略图则生成缩略图
        if (temp == null) {
            File file = new File(filePath);
            //只有当源文件存在的时候才生成缩略图
            if (!file.exists()) return null;
            temp = Tools.getVideoThumbnail(filePath, Constants.CACHE_WIDTH, Constants.CACHE_HEIGHT,
                    MediaStore.Images.Thumbnails.MINI_KIND);
            Tools.saveBitmap(temp, Constants.CACHE_DIR, fileName + ".cache");
        }
        return temp;
    }

    /**
     * 检查是否可连接互联网
     *
     * @param context
     * @return
     */
    public static boolean isConnect(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    // 判断当前网络是否已经连接
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 检查是否为debug模式
     *
     * @param context
     * @return
     */
    public static boolean isDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * 导出数据库文件
     *
     * @param context
     */
    public static boolean getBDFile(Context context) {
        String path = context.getDatabasePath(Constants.DB_NAME).getPath();
        return copyFile(path, Environment.getExternalStorageDirectory().getPath() + File.separator + Constants.DB_NAME);
    }

    private static DecimalFormat format2 = new DecimalFormat("#0.00");
    private static DecimalFormat format5 = new DecimalFormat("#0.00000");


    public static String getHelloString(String name) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 5) {
            return "现在是凌晨了哦," + name;
        } else if (hour >= 5 && hour < 9) {
            return "早上好," + name;
        } else if (hour >= 9 && hour < 12) {
            return "上午好," + name;
        } else if (hour >= 12 && hour < 14) {
            return "中午好," + name;
        } else if (hour >= 14 && hour < 19) {
            return "下午好," + name;
        } else if (hour >= 19 && hour < 12) {
            return "晚上好," + name;
        } else {
            return "你好," + name;
        }
    }

    /**
     * 是否为wifi连接
     *
     * @param mContext
     * @return
     */
    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 获取随机汉字
     *
     * @param len
     * @return
     */
    public static String getRandom(int len) {
        String ret = "";
        for (int i = 0; i < len; i++) {
            String str = null;
            int hightPos, lowPos; // 定义高低位
            Random random = new Random();
            hightPos = (176 + Math.abs(random.nextInt(39))); //获取高位值
            lowPos = (161 + Math.abs(random.nextInt(93))); //获取低位值
            byte[] b = new byte[2];
            b[0] = (new Integer(hightPos).byteValue());
            b[1] = (new Integer(lowPos).byteValue());
            try {
                str = new String(b, "GBk"); //转成中文
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            ret += str;
        }
        return ret;
    }

    /**
     * 首字母大写
     *
     * @param name
     * @return
     */
    public static String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);

    }

    /**
     * 随机数据生成器
     *
     * @param clazz 需要生成的数据类型
     * @param num   生成的数量
     * @param map   可选值<属性名称,List<可选值>>
     * @param <T>
     * @return 生成好的数据
     */
    public static Random random = new Random();

    public static <T> List<T> getRandomObject(Class<T> clazz, int num, Map<String, List<Object>> map) {
        List<T> list = new ArrayList<>();
        ArrayList<Field> fields = new ArrayList<>();
        Class tempClazz = clazz;
        while (tempClazz != null) {
            for (Field temp : tempClazz.getDeclaredFields()) {
                //去除静态属性
                if (Modifier.isStatic(temp.getModifiers())) continue;
                //特殊处理安卓特有属性
                if (temp.getName().startsWith("shadow$_")) continue;
                temp.setAccessible(true);
                fields.add(temp);
            }
            tempClazz = tempClazz.getSuperclass();
        }
        int count = 0;
        try {
            //创建对应的对象并进行赋值
            for (int i = 0; i < num; i++) {
                T entity = (T) clazz.newInstance();
                for (Field temp : fields) {
                    Class type = temp.getType();
                    if (null == temp.get(entity) || temp.get(entity).equals(0) || (map != null && map.containsKey(temp.getName()))) {
                        Object value;
                        List valueList;
                        if (map == null)
                            valueList = null;
                        else
                            valueList = map.get(temp.getName());
                        //是否包含对应的指定值
                        if (valueList == null) {
                            value = null;
                        } else {
                            value = randomTake(valueList);
                        }
                        if ((Integer.TYPE == type) || (Integer.class == type)) {//是否为integer
                            setValue(temp, entity, random.nextInt(10000), value);
                        } else if (String.class == type) {//是否为String
                            setValue(temp, entity, Tools.getRandom(random.nextInt(30)), value);
                        } else if ((Long.TYPE == type) || (Long.class == type)) {//是否为long
                            setValue(temp, entity, random.nextLong(), value);
                        } else if ((Float.TYPE == type) || (Float.class == type)) {//是否为float
                            setValue(temp, entity, random.nextFloat(), value);
                        } else if ((Short.TYPE == type) || (Short.class == type)) {//是否为short
                            setValue(temp, entity, random.nextInt(100), value);
                        } else if ((Double.TYPE == type) || (Double.class == type)) {//是否为double
                            setValue(temp, entity, random.nextDouble() * 1000, value);
                        } else if (Date.class == type) {//是否为date
                            Date date = new Date(random.nextLong());
                            setValue(temp, entity, date, value);
                        } else if (Boolean.TYPE == type || Boolean.class == type) {
                            setValue(temp, entity, random.nextBoolean(), value);
                        } else if (Character.TYPE == type || Character.class == type) {//是否为char
                            setValue(temp, entity, random.nextInt(225), value);
                        }
                    }
                }
                System.out.println(count++);
                list.add(entity);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 为指定对象的属性赋值
     *
     * @param field  指定属性
     * @param entity 指定对象
     * @param def    默认值（当value为null时使用默认值）
     * @param value  指定值
     * @throws IllegalAccessException
     */
    private static void setValue(Field field, Object entity, Object def, Object value) throws IllegalAccessException {
        Object temp;
        if (value == null) temp = def;
        else
            temp = value;
        field.set(entity, temp);
    }


    /**
     * 随机获取列表中的元素
     */
    public static <T> T randomTake(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }


}
