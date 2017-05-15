package com.blueline.flowprocess.core.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.blueline.commons.ReflectUtils;
import com.blueline.flowprocess.core.log.LogUtils;

@SuppressWarnings("unchecked")
public class ConfigUtils
{
	private ConfigUtils()
	{}

	public static final String EVENTQUEUE = "EventQueue";
	public static final String EVENTQUEUETEMPLATE = "EventQueueTemplate";
	public static final String PROCESSCLUSTER = "ProcessCluster";
	public static final String PROCESSNODE = "ProcessNode";
	public static final String PROCESSUNIT = "ProcessUnit";
	public static final String SERVICE = "Service";
	public static final String STORAGE = "Storage";
	public static final String ACCESS = "Access";
	public static final String GATEWAY = "Gateway";
	public static final String ROUTE = "Route";
	public static final String MONITOR = "Monitor";
	
	public static final String CONFIG_TYPE_PROCESS_NODE = "ProcessNode";
	public static final String CONFIG_TYPE_PROCESS_CLUSTER = "ProcessCluster";
	public static final String CONFIG_TYPE_PROCESS_UNIT = "ProcessUnit";
	public static final String CONFIG_TYPE_EVENT_QUEUE = "EventQueue";
	public static final String CONFIG_TYPE_EVENT_QUEUE_TEMPLATE = "EventQueueTemplate";
	public static final String CONFIG_TYPE_SERVICE = "Service";
	public static final String CONFIG_TYPE_STORAGE = "Storage";
	
	public static final String PARAM_MONITOR = "Monitor";
	public static final String PARAM_COLLECT = "Collect";
	public static final String PARAM_ID = "id";
	public static final String PARAM_CLUSTER = "cluster";
	public static final String PARAM_CONFIG_CLIENT = "ConfigClient";
	public static final String PARAM_CLASS = "class";
	
	
	
	public static final String CLUSTER_PARAM_PARAMS = "Params";
	public static final String CLUSTER_PARAM_PROCESS_UNIT_CONFIG = "ProcessUnitConfig";
	public static final String CLUSTER_PARAM_PROCESS_UNIT = "ProcessUnit";

	private static final Map<String, Object> DEFAULT_CONFIG_CLIENT_CONFIG = new HashMap<String, Object>();
	private static final String XML_FILE_PARAM_FILE_PATH = "FilePath";
	private static final String XML_FILE_PARAM_RESOURCE_PATH = "ResourcePath";
	static
	{
		DEFAULT_CONFIG_CLIENT_CONFIG.put(PARAM_CLASS, "com.blueline.flowprocess.core.config.client.XmlFileConfigClient");
		DEFAULT_CONFIG_CLIENT_CONFIG.put(XML_FILE_PARAM_FILE_PATH, "./conf/");
		DEFAULT_CONFIG_CLIENT_CONFIG.put(XML_FILE_PARAM_RESOURCE_PATH, "/com/wisdom/csmp/conf/");
	};

	private static String VARIABLE_FORMAT = "\\$\\{%s\\}";

	private static String m_id;
	private static String m_cluster;
	
	private static IConfigClient m_config_client;

	private static Map<String, Object> m_param_config = new ConcurrentHashMap<String, Object>();
	private static Map<String, Object> m_process_unit_config = new ConcurrentHashMap<String, Object>();
	
	public static void init(Map<String,Object> config) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		LogUtils.debugFormat("%s[%s]\t%s\t%s", ConfigUtils.class.getSimpleName(), config.get(PARAM_ID), LogUtils.TYPE_CONFIG, config);
		
		//config client
		Map<String, Object> config_client_config = (Map<String,Object>) config.get(PARAM_CONFIG_CLIENT);
		if (config_client_config == null)
		{
			config_client_config = DEFAULT_CONFIG_CLIENT_CONFIG;
		}
		m_config_client = ReflectUtils.newObject((String)config_client_config.get(PARAM_CLASS));
		m_config_client.init(config_client_config);

		//id
		m_id = (String) config.get(PARAM_ID);
		if ((m_id == null) || (m_id.isEmpty()))
		{
			m_id = m_config_client.generateId();
		}

		//cluster
		m_cluster = (String) config.get(PARAM_CLUSTER);
		if ((m_cluster != null) && (!m_cluster.isEmpty()))
		{
			loadClusterConfig();
		}
		
		LogUtils.infoFormat("%s[%s]\t%s\t%s", ConfigUtils.class.getSimpleName(), getId(), LogUtils.TYPE_INIT, LogUtils.CONTENT_SUCCESSFUL);
	}
	
	public static String getId()
	{
		return m_id;
	}
	
	public static String getCluster()
	{
		return m_cluster;
	}
	
	protected static void setId(String value)
	{
		m_id = value;
	}
	
	protected static void setCluster(String value)
	{
		m_cluster = value;
	}
	
	public static <V> V getConfig(String type, String name)
	{
		return getConfigWithReplace(type, name, true);
	}
	
	public static String getPropertie(String propertie,String defaultValue){
		
		

		
		String value=System.getProperty(propertie);
		if(null==value||value.isEmpty()){
			return defaultValue;
		}else{
			return value;
		}
	}
	
	public static void showPropertys(){
		Set<Entry<Object, Object>> sets=System.getProperties().entrySet();
		
		for(Entry<Object, Object> entry:sets){
			
			System.out.println(entry.getKey()+"\t"+entry.getValue());
		}
		
	}
	
	public static <V> V getConfigWithReplace(String type, String name, boolean replace)
	{
		if (replace)
		{
			return m_config_client.getConfig(type, name, VARIABLE_FORMAT, m_param_config);
		}
		else
		{
			return m_config_client.getConfig(type, name);
		}
	}

	private static void loadClusterConfig()
	{
		Map<String, Object> cluster_config = ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_PROCESS_CLUSTER, m_cluster);
		
		m_param_config = (Map<String, Object>) cluster_config.get(CLUSTER_PARAM_PARAMS);
		m_process_unit_config = (Map<String, Object>) cluster_config.get(CLUSTER_PARAM_PROCESS_UNIT_CONFIG);
//		System.out.println(m_param_config);
//		System.out.println(m_process_unit_config);
//		System.out.println(cluster_config);
		
		LogUtils.debugFormat("%s[%s]\t%s\t%s", ConfigUtils.class.getSimpleName(), getId(), LogUtils.TYPE_LOAD, cluster_config);
	}

	public static Map<String, Object> getProcessUnitConfig()
	{
		return m_process_unit_config;
	}

	public static boolean loadCluster(String cluster, Map<String, Object> params)
	{
		if ((m_cluster != null) && (!m_cluster.isEmpty()))
		{
			return false;
		}
		else
		{
			m_cluster = cluster;
			loadClusterConfig();
			if (params != null)
			{
				m_param_config.putAll(params);
			}
			return true;
		}
	}

	public static boolean unloadCluster()
	{
		m_cluster = null;
		m_param_config = new ConcurrentHashMap<String, Object>();
		m_process_unit_config = new ConcurrentHashMap<String, Object>();
		return true;
	}
}
