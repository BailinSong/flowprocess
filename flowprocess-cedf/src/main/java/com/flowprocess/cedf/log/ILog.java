package com.flowprocess.cedf.log;

import java.util.Map;

@SuppressWarnings("rawtypes")
public interface ILog
{
	public void init(Map config);
	
	public void setLevel(int value);
	
	public void log(int level, String log);
	public void log(int level, String format, Object ... arguments);
	public void log(int level, String log, Throwable t);
	/**
	 * ��¼ƽ̨��ͳ����־����
	 * @param statData ͳ������
	 */
	public void logStatData(String logType,Object statData);
}
