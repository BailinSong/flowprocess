package com.blueline.flowprocess.core.log;
import java.util.Map;
@SuppressWarnings("rawtypes")
public interface ILog
{
	public void init(Map config);
	public void setLevel(int value);
	public void log(int level, String log);
	public void log(int level, String format, Object ... arguments);
	public void log(int level, String log, Throwable t);
	public void logStatData(String logType,Object statData);
}
