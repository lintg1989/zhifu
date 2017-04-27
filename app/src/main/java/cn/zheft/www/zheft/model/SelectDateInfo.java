package cn.zheft.www.zheft.model;

import java.util.Calendar;

/**
 * 保存筛选的日期选中状态
 * 数据在MyApp类中维持
 */

public class SelectDateInfo {
    public static final int DATE_LABEL_NULL = 0;
    public static final int DATE_LABEL_WEEK = 1;
    public static final int DATE_LABEL_MONTH = 2;
    public static final int DATE_LABEL_SEASON = 3;

    private Calendar dateStart;
    private Calendar dateEnd;
    private int labelFlag;

    public SelectDateInfo(Calendar start, Calendar end, int flag) {
        dateStart = start;
        dateEnd = end;
        labelFlag = flag;
    }

    public Calendar getDateStart() {
        return dateStart;
    }

    public void setDateStart(Calendar dateStart) {
        this.dateStart = dateStart;
    }

    public Calendar getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Calendar dateEnd) {
        this.dateEnd = dateEnd;
    }

    public int getLabelFlag() {
        return labelFlag;
    }

    public void setLabelFlag(int labelFlag) {
        this.labelFlag = labelFlag;
    }

    public String getStartDateStr() {
        String dateStr = null;
        if (dateStart != null) {
            dateStr = calendarToStr(dateStart);
        }
        return dateStr;
    }

    public String getEndDateStr() {
        String dateStr = null;
        if (dateEnd != null) {
            dateStr = calendarToStr(dateEnd);
        }
        return dateStr;
    }

    // 把calendar转成2016-05-16形式
    public String calendarToStr(Calendar calendar) {
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
}
