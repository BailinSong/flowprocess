package com.flowprocess.cedf.components.log.logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.flowprocess.cedf.log.ILog;
import com.flowprocess.cedf.log.LogUtils;

public class Log4j2 implements ILog
{
	static Logger m_logger_business = null;
	static Logger m_logger = null;

	public static final String ROOT_LOGGER = "ROOT";
	public static final String DATA_LOGGER = "DATALOG";
	String m_log_config_path = null;

	public void init(Map config)
	{
		if (config == null || config.isEmpty())
		{
			throw new RuntimeException("Log4j2 config is null");
		}

		m_log_config_path = (String) config.get(LogUtils.PARAM_LOGGER_CONFIG);
		if (m_log_config_path == null || m_log_config_path.isEmpty())
		{
			throw new RuntimeException("Log4j2 config <LogPath> is null");
		}
		try
		{
			
			ConfigurationSource source = new  ConfigurationSource(new FileInputStream(m_log_config_path), new File(m_log_config_path));
			Configurator.initialize(null, source);
			
			m_logger_business = LoggerFactory.getLogger(DATA_LOGGER);
			m_logger = LoggerFactory.getLogger(ROOT_LOGGER);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Log4j2 init fail," , e);
		}
	}

	

	public void log(int level, String log)
	{
		switch (level)
		{
		case LogUtils.LEVEL_TRACE:
		{
			trace(log);
			break;
		}
		case LogUtils.LEVEL_DEBUG:
		{
			debug(log);
			break;
		}
		case LogUtils.LEVEL_INFO:
		{
			info(log);
			break;
		}
		case LogUtils.LEVEL_WARN:
		{
			warn(log);
			break;
		}
		case LogUtils.LEVEL_ERROR:
		{
			error(log);
			break;
		}
		default:
			break;
		}
	}

	public void log(int level, String format, Object... arguments)
	{
		switch (level)
		{
		case LogUtils.LEVEL_TRACE:
		{
			trace(format, arguments);
			break;
		}
		case LogUtils.LEVEL_DEBUG:
		{
			debug(format, arguments);
			break;
		}
		case LogUtils.LEVEL_INFO:
		{
			info(format, arguments);
			break;
		}
		case LogUtils.LEVEL_WARN:
		{
			warn(format, arguments);
			break;
		}
		case LogUtils.LEVEL_ERROR:
		{
			error(format, arguments);
			break;
		}
		default:
			break;
		}
	}

	public void log(int level, String log, Throwable t)
	{
		switch (level)
		{
		case LogUtils.LEVEL_TRACE:
		{
			trace(log, t);
			break;
		}
		case LogUtils.LEVEL_DEBUG:
		{
			debug(log, t);
			break;
		}
		case LogUtils.LEVEL_INFO:
		{
			info(log, t);
			break;
		}
		case LogUtils.LEVEL_WARN:
		{
			warn(log, t);
			break;
		}
		case LogUtils.LEVEL_ERROR:
		{
			error(log, t);
			break;
		}
		default:
			break;
		}
	}

	public void logStatData(String logType, Object statData)
	{
		if (logType!=null&&!logType.isEmpty()&&statData != null)
		{
			m_logger_business.error("{\"Time\":\"{}\",\"LogType\":\"{}\",\"Log\":{}}",System.currentTimeMillis(), logType,JSON.toJSONString(statData));
		}
	}
	
	private void trace(String msg)
	{
		if (m_logger.isTraceEnabled())
		{
			m_logger.trace(msg);
		}
	}
	
	private void trace(String format, Object ... arguments)
	{
		if (m_logger.isTraceEnabled())
		{
			m_logger.trace(format, arguments);
		}
	}

	private void trace(String msg, Throwable t)
	{
		if (m_logger.isTraceEnabled())
		{
			m_logger.trace(msg, t);
		}
	}
	
	private void debug(String msg)
	{
		if (m_logger.isDebugEnabled())
		{
			m_logger.debug(msg);
		}
	}

	private void debug(String format, Object ... arguments)
	{
		if (m_logger.isDebugEnabled())
		{
			m_logger.debug(format, arguments);
		}
	}
	
	private void debug(String msg, Throwable t)
	{
		if (m_logger.isDebugEnabled())
		{
			m_logger.debug(msg, t);
		}
	}

	private void info(String msg)
	{
		if (m_logger.isInfoEnabled())
		{
			m_logger.info(msg);
		}
	}
	
	private void info(String format, Object ... arguments)
	{
		if (m_logger.isInfoEnabled())
		{
			m_logger.info(format, arguments);
		}
	}

	private void info(String msg, Throwable t)
	{
		if (m_logger.isInfoEnabled())
		{
			m_logger.info(msg, t);
		}
	}

	private void warn(String msg, Throwable t)
	{
		if (m_logger.isWarnEnabled())
		{
			m_logger.warn(msg, t);
		}
	}

	private void warn(String msg)
	{
		if (m_logger.isWarnEnabled())
		{
			m_logger.warn(msg);
		}
	}
	
	private void warn(String format, Object ... arguments)
	{
		if (m_logger.isWarnEnabled())
		{
			m_logger.warn(format, arguments);
		}
	}

	private void error(String msg)
	{
		if (m_logger.isErrorEnabled())
		{
			m_logger.error(msg);
		}
	}
	
	private void error(String format, Object ... arguments)
	{
		if (m_logger.isErrorEnabled())
		{
			m_logger.error(format, arguments);
		}
	}

	private void error(String msg, Throwable t)
	{
		if (m_logger.isErrorEnabled())
		{
			m_logger.error(msg, t);
		}
	}

	public void setLevel(int value) {
		// TODO Auto-generated method stub
		
	}

}
