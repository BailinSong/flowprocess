package com.flowprocess.cedf.log;

import java.util.Map;

import com.flowprocess.commons.ReflectUtils;


@SuppressWarnings("rawtypes")
public class LogUtils
{
	private LogUtils()
	{

	}

	public static final int LEVEL_ERROR = 5;
	public static final int LEVEL_WARN = 4;
	public static final int LEVEL_INFO = 3;
	public static final int LEVEL_DEBUG = 2;
	public static final int LEVEL_TRACE = 1;

	public static final String TYPE_CONFIG = "CONFIG";
	public static final String TYPE_INIT = "INIT";
	public static final String TYPE_LOAD = "LOAD";
	public static final String TYPE_UNLOAD = "UNLOAD";
	public static final String TYPE_START = "START";
	public static final String TYPE_STOP = "STOP";
	public static final String TYPE_ADJUST = "ADJUST";
	public static final String TYPE_PUT = "PUT";
	public static final String TYPE_GET = "GET";
	public static final String TYPE_REMOVE = "REMOVE";
	public static final String TYPE_ADD = "ADD";
	public static final String TYPE_TAKE = "TAKE";
	public static final String TYPE_EXEC = "EXEC";
	public static final String TYPE_EXCEPTION = "EXCEPTION";
	public static final String TYPE_COMMAND = "COMMAND";

	public static String CONTENT_SUCCESSFUL = "successful";
	public static String CONTENT_FAILED = "failed";


	private static ILog m_logger;
	private static final String DEFAULT_LOGGER = "com.flowprocess.cedf.log.SystemOutLog";
	private static final int DEFAULT_LEVEL = LEVEL_INFO;
	static
	{
		try
		{
			forName(DEFAULT_LOGGER, DEFAULT_LEVEL);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void forName(String log_class_name, int level)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		m_logger = ReflectUtils.newObject(log_class_name);
		m_logger.setLevel(level);
	}

	public static final String PARAM_LOGGER = "Logger";
	public static final String PARAM_CONFIG_PATH = "ConfigPath";
	public static final String PARAM_LEVEL = "Level";

	public static void init(Map config) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		String log_class = (String) config.get(PARAM_LOGGER);
		if (log_class == null)
		{
			log_class = DEFAULT_LOGGER;
		}

		String level = (String) config.get(PARAM_LEVEL);
		int ilevel = DEFAULT_LEVEL;
		if (level != null)
		{
			ilevel = Integer.parseInt(level);
		}

		forName(log_class, ilevel);
		m_logger.init(config);
	}

	public void setLevel(int value)
	{
		m_logger.setLevel(value);
	}

	private static void log(int level, String log)
	{
		m_logger.log(level, log);
	}

	private static void log(int level, String log, Throwable t)
	{
		m_logger.log(level, log, t);
	}

	public static void error(String log)
	{
		log(LEVEL_ERROR, log);
	}

	public static void warn(String log)
	{
		log(LEVEL_WARN, log);
	}

	public static void info(String log)
	{
		log(LEVEL_INFO, log);
	}

	public static void debug(String log)
	{
		log(LEVEL_DEBUG, log);
	}

	public static void trace(String log)
	{
		log(LEVEL_TRACE, log);
	}

	public static void error(String log, Throwable t)
	{
		log(LEVEL_ERROR, log, t);
	}

	public static void warn(String log, Throwable t)
	{
		log(LEVEL_WARN, log, t);
	}

	public static void info(String log, Throwable t)
	{
		log(LEVEL_INFO, log, t);
	}

	public static void debug(String log, Throwable t)
	{
		log(LEVEL_DEBUG, log, t);
	}

	public static void trace(String log, Throwable t)
	{
		log(LEVEL_TRACE, log, t);
	}

	public static void error(Throwable t)
	{
		log(LEVEL_ERROR, "", t);
	}

	public static void warn(Throwable t)
	{
		log(LEVEL_WARN, "", t);
	}

	public static void info(Throwable t)
	{
		log(LEVEL_INFO, "", t);
	}

	public static void debug(Throwable t)
	{
		log(LEVEL_DEBUG, "", t);
	}

	public static void trace(Throwable t)
	{
		log(LEVEL_TRACE, "", t);
	}

	public static void logFormat(int level, String format, Object... params)
	{
		if (format.contains("{}"))
		{
			m_logger.log(level, format, params);
		}
		else
		{
			m_logger.log(level, String.format(format, params));
		}
	}

	public static void errorFormat(String format, Object... params)
	{
		logFormat(LEVEL_ERROR, format, params);
	}

	public static void warnFormat(String format, Object... params)
	{
		logFormat(LEVEL_WARN, format, params);
	}

	public static void infoFormat(String format, Object... params)
	{
		logFormat(LEVEL_INFO, format, params);
	}

	public static void debugFormat(String format, Object... params)
	{
		logFormat(LEVEL_DEBUG, format, params);
	}

	public static void traceFormat(String format, Object... params)
	{
		logFormat(LEVEL_TRACE, format, params);
	}
	
	public static void logStatData(String logType, Object statData) {
		
		m_logger.logStatData(logType,statData);
		
	}

}
