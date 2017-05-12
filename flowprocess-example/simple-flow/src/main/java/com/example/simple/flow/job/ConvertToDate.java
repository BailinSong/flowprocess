package com.example.simple.flow.job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.flowprocess.cedf.flow.IJob;

public class ConvertToDate implements IJob {

	@Override
	public void init(Map<String, Object> config) {
		// TODO Auto-generated method stub

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
