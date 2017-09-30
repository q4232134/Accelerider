package com.jiaozhu.accelerider.commonTools;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.DatePicker;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jiaozhu on 16/8/12.
 * 用于处理字符串时间的时间选择器
 */
public class DatePickerUtils {
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    static DecimalFormat decimalFormat = new DecimalFormat("00");

    public interface OnDateSetListener {
        void onDateSet(String dateStr);
    }

    public static void showDialog(Context context, String dateStr, @Nullable final OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(string2Date(dateStr));
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(context, new android.app.DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (listener != null) {
                    listener.onDateSet(year + "-" + decimalFormat.format(monthOfYear + 1) + "-"
                            + decimalFormat.format(dayOfMonth));
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    /**
     * 字符串转换日期
     *
     * @param dateStr
     * @return
     */
    public static Date string2Date(String dateStr) {
        Date date;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }


    /**
     * 日期转换字符串
     *
     * @param date
     * @return
     */
    public static String date2String(Date date) {
        return dateFormat.format(date);
    }
}
