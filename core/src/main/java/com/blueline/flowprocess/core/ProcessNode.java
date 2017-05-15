package com.blueline.flowprocess.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.blueline.commons.XmlUtils;
import com.blueline.flowprocess.core.command.CedfCommand;
import com.blueline.flowprocess.core.command.CommandUtils;
import com.blueline.flowprocess.core.config.ConfigUtils;
import com.blueline.flowprocess.core.log.LogUtils;
import com.blueline.flowprocess.core.log.logger.SystemOutLog;
import com.blueline.flowprocess.core.monitor.MonitorUtils;
import com.blueline.flowprocess.core.processunit.ProcessUnitUtils;

public class ProcessNode
{
	private static ProcessNode m_instance;
	
	private synchronized static ProcessNode getInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException
	{
		return getInstance(new HashMap<String, Object>());
	}
	
	private synchronized static ProcessNode getInstance(Map<String, Object> config) throws ClassNotFoundException, InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException
	{
		if (m_instance == null)
		{
			m_instance = new ProcessNode(config);
		}
		
		return m_instance;
	}
	
	private static final String PARAM_ID = "id";
	private static final String PARAM_CLUSTER = "cluster";
	private static final String PARAM_FILE = "file";

	private ProcessNode(Map<String, Object> config) throws ClassNotFoundException, InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException
	{
		init(config);
	}
	
	@SuppressWarnings("unchecked")
	private void init(Map<String, Object> config) throws ClassNotFoundException, InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException
	{
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), config.get(PARAM_ID), LogUtils.TYPE_CONFIG, config);
		
		//config
		String file = (String) config.get(PARAM_FILE);
		if ((file != null) && (!file.isEmpty()))
		{
			Map<String, Object> file_config = (Map<String, Object>) XmlUtils.XML2MAP(file).get(ConfigUtils.CONFIG_TYPE_PROCESS_NODE);
			file_config.putAll(config);
			config = file_config;
		}
		ConfigUtils.init(config);

		if (ConfigUtils.getCluster() != null)
		{
			Map<String, Object> process_unit_config = ConfigUtils.getProcessUnitConfig();
//			System.out.println(process_unit_config);
			ProcessUnitUtils.init(process_unit_config);
		}
		
		//command
//		Map<String, Object> command_config = ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_COMMAND, PARAM_COMMAND_CHANNEL);
		CommandUtils.init(config);
		CommandUtils.subscribe();
		
		//init
		MonitorUtils.init(config);
		
//		ProcessUnitUtils.init(cluster_config);
		LogUtils.infoFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_INIT, LogUtils.CONTENT_SUCCESSFUL);
	}

	protected static boolean load(String cluster, Map<String, Object> params) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		if (!ConfigUtils.loadCluster(cluster, params))
		{
			return false;
		}
		else
		{
			Map<String, Object> process_unit_config = ConfigUtils.getProcessUnitConfig();
	//		System.out.println(process_unit_config);
			ProcessUnitUtils.init(process_unit_config);
			return true;
		}
	}

	protected static boolean unload() throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		if (!ConfigUtils.unloadCluster())
		{
			return false;
		}
		else
		{
			ProcessUnitUtils.unloadAllProcessUnits();
			return true;
		}
	}
	
	protected static boolean start() throws ClassNotFoundException, InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException
	{
		MonitorUtils.start();
		boolean  ret =  getInstance().startAllProcessUnits();
		Map<String,Object> collect_data=new HashMap<String, Object>();
		collect_data.put("id", ConfigUtils.getId());
		collect_data.put("stat", String.valueOf(ret));
		collect_data.put("iplist", MonitorUtils.getAllHostAddress());
		MonitorUtils.collect(MonitorUtils.TYPE_PROCESS_NODE_START, collect_data);
		return ret;
	}

	protected static boolean stop() throws ClassNotFoundException, InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException
	{
		
		boolean  ret =   getInstance().stopAllProcessUnits();
		
		return ret;
	}
	
	protected static boolean exit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException
	{
		boolean ret = getInstance().stopAllProcessUnits();
		
		Map<String,Object> collect_data=new HashMap<String, Object>();
		collect_data.put("id", ConfigUtils.getId());
		collect_data.put("stat", String.valueOf(ret));
		collect_data.put("iplist", MonitorUtils.getAllHostAddress());
		MonitorUtils.collect(MonitorUtils.TYPE_PROCESS_NODE_STOP, collect_data);
		MonitorUtils.stop();
		exitAll();
		return ret;
	}
	
	private boolean startAllProcessUnits() throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		return ProcessUnitUtils.startAllProcessUnits();
	}
	
	private boolean stopAllProcessUnits()
	{
		return ProcessUnitUtils.stopAllProcessUnits();
	}
//
//	public int adjustProcessUnit(String process_unit_key, String process_unit_id, int count) throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
//	{
//		Map<String, Object> config = ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_PROCESS_UNIT, process_unit_key);
//		int ret = ProcessUnitUtils.adjustProcessUnit(process_unit_id, config, count);
//
//		LogUtils.infoFormat("%s[%s]\t%s\t%s\t%s\t%d\t%d", this.getClass().getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_ADJUST, process_unit_key, process_unit_id, count, ret);
//		return ret;
//	}

	private static final AtomicBoolean m_exit_flag = new AtomicBoolean(false);
	
	private static void exitAll()
	{
		m_exit_flag.set(true);
	}
	
	public static void main(String[] args) throws Exception
	{
//		String log_class_name = "com.ws.log.SystemOutLog";
//		int log_level = LogUtils.LEVEL_TRACE;
//		int log_level = LogUtils.LEVEL_INFO;
	
		Map<String, Object> config = new HashMap<String, Object>();
		int args_length = args.length;
		String action="start";
		
		config.put(PARAM_ID, ConfigUtils.getPropertie("NodeId",""));
		config.put(PARAM_CLUSTER, ConfigUtils.getPropertie("Cluster",""));
		config.put(PARAM_FILE, ConfigUtils.getPropertie("NodeConfig",""));
		action=ConfigUtils.getPropertie("Action","start");
		
		if(((String)config.get(PARAM_CLUSTER)).isEmpty()||((String)config.get(PARAM_FILE)).isEmpty()){
		System.out.println("invalid param:\t" + config);
		System.out.println("Usage: pn [-i[d]:<id>] [-c[luster]:<cluster>]\n" +
										   "   or: pn [-f[ile]:<file>]");
						System.exit(0);
					
		}
		
//		System.out.println(log_class_name);
//		System.out.println(log_level);
//		LogUtils.forName(log_class_name, log_level);
		
		Map<String, Object> log_config = new HashMap<String, Object>();
		String log_config_path = "./conf/log4j2.xml";
		//log_config.put(LogUtils.PARAM_LOGGER, Logback.class.getName());
		log_config.put(LogUtils.PARAM_LOGGER, ConfigUtils.getPropertie(LogUtils.PARAM_LOGGER,SystemOutLog.class.getName()));
		log_config.put(LogUtils.PARAM_LOGGER_CONFIG, ConfigUtils.getPropertie(LogUtils.PARAM_LOGGER_CONFIG,""));
		LogUtils.init(log_config);
		
		if(action.equalsIgnoreCase("Stop"))
		{
			
			LogUtils.debugFormat("%s[%s]\t%s\t%s", ProcessNode.class.getSimpleName(), config.get(PARAM_ID), LogUtils.TYPE_CONFIG, config);
			String id=(String)config.get(PARAM_ID);
			if(id==null||id.isEmpty())
			{
				LogUtils.warnFormat("%s[%s]\t%s\t%s", ProcessNode.class.getSimpleName(), config.get(PARAM_ID), LogUtils.TYPE_STOP, LogUtils.CONTENT_FAILED);
				return ;
			}
			//config
			String file = (String) config.get(PARAM_FILE);
			if ((file != null) && (!file.isEmpty()))
			{
				Map<String, Object> file_config = (Map<String, Object>) XmlUtils.XML2MAP(file).get(ConfigUtils.CONFIG_TYPE_PROCESS_NODE);
				file_config.putAll(config);
				
				config = file_config;
				
				config.remove(ConfigUtils.PARAM_CLUSTER);
			}
			
			ConfigUtils.init(config);

			CommandUtils.init(config);
			CedfCommand command=new CedfCommand(CommandUtils.RANGE_NODE, id, CommandUtils.COMMAND_PROCESS_NODE, CommandUtils.ACTION_STOP, null);
			CommandUtils.publish(command);
			
			LogUtils.infoFormat("%s[%s]\t%s\t%s", ProcessNode.class.getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_STOP, LogUtils.CONTENT_SUCCESSFUL);
		
			return;
			
		}
		else if(action.equalsIgnoreCase("Exit"))
		{
			
			LogUtils.debugFormat("%s[%s]\t%s\t%s", ProcessNode.class.getSimpleName(), config.get(PARAM_ID), LogUtils.TYPE_CONFIG, config);
			String id=(String)config.get(PARAM_ID);
			if(id==null||id.isEmpty())
			{
				LogUtils.warnFormat("%s[%s]\t%s\t%s", ProcessNode.class.getSimpleName(), config.get(PARAM_ID), LogUtils.TYPE_STOP, LogUtils.CONTENT_FAILED);
				return ;
			}
			//config
			String file = (String) config.get(PARAM_FILE);
			if ((file != null) && (!file.isEmpty()))
			{
				Map<String, Object> file_config = (Map<String, Object>) XmlUtils.XML2MAP(file).get(ConfigUtils.CONFIG_TYPE_PROCESS_NODE);
				file_config.putAll(config);
				
				config = file_config;
				
				config.remove(ConfigUtils.PARAM_CLUSTER);
			}
			
			ConfigUtils.init(config);

			CommandUtils.init(config);
			CedfCommand command=new CedfCommand(CommandUtils.RANGE_NODE, id, CommandUtils.COMMAND_PROCESS_NODE, CommandUtils.ACTION_EXIT, null);
			CommandUtils.publish(command);
			
			LogUtils.infoFormat("%s[%s]\t%s\t%s", ProcessNode.class.getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_STOP, LogUtils.CONTENT_SUCCESSFUL);
		
			return;
			
			
		}else if(action.equalsIgnoreCase("start"))
		{


			ProcessNode.getInstance(config);
			
			ProcessNode.start();
			
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					if(!m_exit_flag.get()){
					try {
						ProcessNode.exit();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						LogUtils.warn(e);
					}
					}
				}
			}));
			
			
		}else
		{
			return ;
		}

		
		while (!m_exit_flag.get())
		{
			Thread.sleep(1000);
		}
		

	}
}
