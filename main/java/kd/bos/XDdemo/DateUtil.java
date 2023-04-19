package kd.bos.XDdemo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {
	public static Date getTomorrow() {
		LocalDate localdate=LocalDate.now().plusDays(1);
		return Date.from(localdate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	public static Long diffDate(String format, Date startDate, Date endDate) {
		// 设置转换的日期格式
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			// 开始时间
			startDate = sdf.parse(sdf.format(startDate));
			// 结束时间
			endDate = sdf.parse(sdf.format(endDate));
		} catch (ParseException e) {
			System.out.println(e);
		}
		// 得到相差的天数
		long betweenDate = (endDate.getTime() - startDate.getTime()) / (60 * 60 * 24 * 1000);

		return betweenDate;
	}
}
