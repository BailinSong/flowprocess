package com.blueline.flowprocess.example.simple.flow.job;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import com.blueline.flowprocess.core.flow.IJob;
public class ConvertToDate implements IJob {
	@Override
	public void init(Map<String, Object> config) {
	}
	@Override
	public String exec(Map<String, Object> data) {
		Long time=(Long)data.get("msg");
		Date date=new Date(time);
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formatDate=sdf.format(date);
		data.put("formatDate", formatDate);
		return formatDate.substring(formatDate.length()-1,formatDate.length());
	}
}
