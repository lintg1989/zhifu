package cn.zheft.www.zheft.util;


import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间控制以及日期格式转换
 */
public class DateUtil {

    public static final int DAY_TIME_NOW = 0;
    public static final int DAY_TIME_START = 1;
    public static final int DAY_TIME_END = 2;


    public static String lineToCharacter(String dateStr) {
        return lineToCharacter(dateStr, true);
    }

    /**
     * 输入 2017-03-30 18:00:00
     * 输出 2017年3月30日 18:00:00
     *
     * @param dateStr
     * @return
     */
    public static String lineToCharacter(String dateStr, boolean full) {
        try {
            String result = "";
            String[] arr = dateStr.split(" ", 2);
            String[] dateArr = arr[0].split("-", 3);

            if (full) {
                result += dateArr[0];
                result += "年";
            }

            result += Integer.valueOf(dateArr[1]);
            result += "月";
            result += Integer.valueOf(dateArr[2]);
            result += "日";
            result += " " + arr[1];
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return dateStr;
        }
    }

    public static String lineToCharacterNoYear(String dateStr, String year) {
        if (dateStr == null || year == null) {
            return dateStr;
        }
        try {
            String result = "";
            String[] arr = dateStr.split(" ", 2);
            String[] dateArr = arr[0].split("-", 3);

            if (!year.equals(dateArr[0])) {
                result += dateArr[0];
                result += "年";
            }

            result += Integer.valueOf(dateArr[1]);
            result += "月";
            result += Integer.valueOf(dateArr[2]);
            result += "日";
            result += " " + arr[1];
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return dateStr;
        }
    }

    public static String numToLineSlow(String numTime) {
        // 效率比手动解析字符串低
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            Date date = dateFormat.parse(numTime);
            SimpleDateFormat outDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String outDate = outDateFormat.format(date);
            return outDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return numTime;
    }

    public static String numToLine(String numTime) {
        try {
            String result = "";
            for (int i = 0; i < numTime.length(); i++) {
                result += numTime.charAt(i);
                if (i == 3 || i == 5) {
                    result += "-";
                }
                if (i == 7) {
                    result += " ";
                }
                if (i == 9 || i == 11) {
                    result += ":";
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numTime;
    }

    // 服务器返回的时间是20160512021000，转成2016-05-12 02:10:00
    public static String timeFormat(String timeStr) {
        String result = "";
        if (timeStr != null && timeStr.length() == 14) {
            for (int i = 0; i < 14; i++) {
                result += timeStr.charAt(i);
                if (i == 3 || i == 5) {
                    result += "-";
                }
                if (i == 7) {
                    result += " ";
                }
                if (i == 9 || i == 11) {
                    result += ":";
                }
            }
        }
        return result;
    }

    // 把calendar转成2016-05-16形式
    public static String calendarToStr(Calendar calendar) {
        String strDate;
        if ((calendar.get(Calendar.MONTH)+1) < 10) {
            strDate = String.valueOf(calendar.get(Calendar.YEAR)) + "-0" +String.valueOf(calendar.get(Calendar.MONTH)+1);
        } else {
            strDate = String.valueOf(calendar.get(Calendar.YEAR)) + "-" +String.valueOf(calendar.get(Calendar.MONTH)+1);
        }
        if (calendar.get(Calendar.DATE) < 10) {
            return strDate + "-0" + String.valueOf(calendar.get(Calendar.DATE));
        } else {
            return strDate + "-" + String.valueOf(calendar.get(Calendar.DATE));
        }
    }

    // 把calendar转成20160516形式
    public static String calendarToStrNoLine(Calendar calendar) {
        String strDate;
        if ((calendar.get(Calendar.MONTH)+1) < 10) {
            strDate = String.valueOf(calendar.get(Calendar.YEAR)) + "0" +String.valueOf(calendar.get(Calendar.MONTH)+1);
        } else {
            strDate = String.valueOf(calendar.get(Calendar.YEAR)) + String.valueOf(calendar.get(Calendar.MONTH)+1);
        }
        if (calendar.get(Calendar.DATE) < 10) {
            return strDate + "0" + String.valueOf(calendar.get(Calendar.DATE));
        } else {
            return strDate + String.valueOf(calendar.get(Calendar.DATE));
        }
    }

    // 把calendar转成 5月16日 形式
    public static String calendarToChar(Calendar calendar) {
        return String.valueOf(calendar.get(Calendar.MONTH)+1) + "月"
                + String.valueOf(calendar.get(Calendar.DATE)) + "日";
    }

    // 把年月日数字转换成2016-05-16形式
    public static String dateToStr(int year, int month, int day) {
        String strDate;
        if (month < 10) {
            strDate = String.valueOf(year) + "-0" + String.valueOf(month);
        } else {
            strDate = String.valueOf(year) + "-" + String.valueOf(month);
        }
        if (day < 10) {
            return strDate + "-0" + String.valueOf(day);
        } else {
            return strDate + "-" + String.valueOf(day);
        }
    }

    // 获取日历时间，转成 22:33:44 的形式
    public static String getTimeStr(Calendar calendar) {
        String strTime = "";
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        if (hour < 10) {
            strTime += "0";
        }
        strTime += String.valueOf(hour);
        strTime += ":";
        if (minute < 10) {
            strTime += "0";
        }
        strTime += String.valueOf(minute);
        strTime += ":";
        if (second < 10) {
            strTime += "0";
        }
        strTime += String.valueOf(second);
        return strTime;
    }

    public static String getNowFullStr() {
        Calendar calendar = Calendar.getInstance();
        return calendarToStr(calendar) + " " + getTimeStr(calendar);
    }

    // 返回今日时间串 2016-05-20 形式
    public static String getTodayStr() {
        Calendar calendar = Calendar.getInstance();
        return calendarToStr(calendar);
    }

    // 非当日的话将 2016-07-22 18:00 转换成 2016-07-22
    public static String changeDateStr(String today, String date) {
        try {
            if (!today.equals(date.substring(0,10))) {
                return date.substring(0,10);
            }
            return date;
        } catch (Exception e) {
            return date != null ? date : "";
        }
    }

    public static Calendar getAfter(int field, int amount) {
        return getAfter(null, field, amount);
    }

    public static Calendar getAfter(Calendar calendar, int field, int amount) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.add(field, amount);
        return calendar;
    }

    public static Calendar getDate(int yearOffset, int monthOffset, int dayOffset) {
        return getDate(yearOffset, monthOffset, dayOffset, DAY_TIME_NOW);
    }

    public static Calendar getDate(int yearOffset, int monthOffset, int dayOffset, int flag) {
        return getDate(null, yearOffset, monthOffset, dayOffset, flag);
    }

    public static Calendar getDate(Calendar srcCalendar, int yearOffset, int monthOffset, int dayOffset, int flag) {
        if (srcCalendar == null) {
            srcCalendar = Calendar.getInstance();
        }
        srcCalendar = getAfter(srcCalendar, Calendar.YEAR, yearOffset);
        srcCalendar = getAfter(srcCalendar, Calendar.MONTH, monthOffset);
        srcCalendar = getAfter(srcCalendar, Calendar.DATE, dayOffset);
        return resetTime(srcCalendar, flag);
    }

    public static Calendar getAfterDays(Calendar calendar, int days) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar;
    }

    public static Calendar getAfterMonth(Calendar calendar, int months) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.add(Calendar.MONTH, months);
        return calendar;
    }

    public static Calendar getAfterYears(Calendar calendar, int years) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.add(Calendar.YEAR, years);
        return calendar;
    }

    public static Calendar resetTime(int flag) {
        return resetTime(null, flag);
    }

    public static Calendar resetTime(Calendar calendar, int flag) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        if (flag == DAY_TIME_START) {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));
        } else if (flag == DAY_TIME_END) {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getMaximum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getMaximum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getMaximum(Calendar.MILLISECOND));
        }
        return calendar;
    }

    public static Calendar copyFrom(Calendar calendar) {
        Calendar ret = Calendar.getInstance();
        if (calendar != null) {
            ret.setTimeInMillis(calendar.getTimeInMillis());
        }
        return ret;
    }

    public static void logDate(Calendar calendar,int order) {
//        String strDate;
        String orderStr;
        switch (order) {
            case 1:orderStr = "CalSta";break;
            case 2:orderStr = "CalMin";break;
            case 3:orderStr = "CalMax";break;
            case 4:orderStr = "CalTod";break;
            case 5:orderStr = "CalEnd";break;
            default:orderStr = "None";
                break;
        }
        LogUtil.e(orderStr,calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH)+1) + "月"+ calendar.get(Calendar.DATE) + "日");
    }
}
