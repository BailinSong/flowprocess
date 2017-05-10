package com.flowprocess.cedf.log.logger;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.flowprocess.cedf.log.ILog;

@SuppressWarnings("rawtypes")
public class SystemOutLog implements ILog
{
	public static final int LEVEL_ERROR = 5;
	public static final int LEVEL_WARN = 4;
	public static final int LEVEL_INFO = 3;
	public static final int LEVEL_DEBUG = 2;
	public static final int LEVEL_TRACE = 1;
	
	private int m_level = LEVEL_INFO;

	
	
	public int getLevel()
	{
		return m_level;
	}
	
	public void setLevel(int value)
	{
		m_level = value;
	}
	
	public boolean contain(int level)
	{
		return (getLevel() <= level);
	}
	
	public static String toString(int level)
	{
		switch (level)
		{
			case LEVEL_ERROR:
			{
				return "ERROR";
			}
			
			case LEVEL_WARN:
			{
				return "WARN";
			}
	
			case LEVEL_INFO:
			{
				return "INFO";
			}
	
			case LEVEL_DEBUG:
			{
				return "DEBUG";
			}
	
			case LEVEL_TRACE:
			{
				return "TRACE";
			}
	
			default:
			{
				return "UNKNOWN";
			}
		}
	}
	
	public void init(Map config)
	{
		
	}
	
	public void log(int level, String log)
	{
		if (contain(level))
		{
			System.out.println(String.format("%d\t%s\t%s\t%s", System.currentTimeMillis(), Thread.currentThread().getName(), toString(level), log));
		}
	}

	public void log(int level, String log, Throwable t)
	{
		String exception_msg = t == null ? "" : t.getMessage();
		System.out.println(String.format("%d\t%s\t%s\t%s\t%s", System.currentTimeMillis(), Thread.currentThread().getName(), toString(level), log, exception_msg));
	}

	public void logStatData(String logType,Object data)
	{
		System.out.println(JSON.toJSONString(data));
	}

	public void log(int level, String format, Object... arguments)
	{
		
	}
}
