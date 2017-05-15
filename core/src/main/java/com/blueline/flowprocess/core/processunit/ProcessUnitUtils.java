package com.blueline.flowprocess.core.processunit;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.blueline.flowprocess.core.config.ConfigUtils;
import com.blueline.flowprocess.core.log.LogUtils;

@SuppressWarnings("unchecked")
public class ProcessUnitUtils
{
	private static final Object PARAM_PROCESS_UNIT = "ProcessUnit";
	private static final Object PARAM_CATEGORY = "category";
	private static final Object PARAM_COUNT = "count";
	private static final Object PARAM_ID = "id";
	
	private static Map<String, Map<String, Object>> m_process_unit_params_map = new ConcurrentHashMap<String, Map<String, Object>>();
	private static Map<String, LinkedList<ProcessUnit>> m_process_unit_map = new ConcurrentHashMap<String, LinkedList<ProcessUnit>>();

	public static void init(Map<String, Object> config) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		LogUtils.debugFormat("%s[%s]\t%s\t%s", ProcessUnitUtils.class.getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_CONFIG, config);
		
		Object process_units = config.get(PARAM_PROCESS_UNIT);
		if (process_units instanceof List)
		{
			List<Map<String, Object>> process_unit_list = (List<Map<String, Object>>) process_units;
			for (Map<String, Object> process_unit_config : process_unit_list)
			{
				loadProcessUnitParams(process_unit_config);
			}
		}
		else if (process_units instanceof Map)
		{
			Map<String, Object> process_unit_config = (Map<String, Object>) process_units;
			loadProcessUnitParams(process_unit_config);
		}
		
		LogUtils.infoFormat("%s[%s]\t%s\t%s", ProcessUnitUtils.class.getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_INIT, LogUtils.CONTENT_SUCCESSFUL);
	}
	
	private static Map<String,Object> getProcessUnitParams(String id){
		Map<String,Object> params= m_process_unit_params_map.get(id);
		if(params==null){
			throw new RuntimeException("invalid process unit id!");
		}
		
		return params;
	}

	private static void loadProcessUnitParams(Map<String, Object> params)
	{
		String category = (String) params.get(PARAM_CATEGORY);
		String id = (String) params.get(PARAM_ID);
		if (id == null)
		{
			id = category;
		}
		Map<String, Object> map = m_process_unit_params_map.put(id, params);
		if (map != null)
		{
			throw new RuntimeException("duplicate process unit id!");
		}
		
		LogUtils.debugFormat("%s[%s]\t%s\t%s", ProcessUnitUtils.class.getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_LOAD, params);
	}
	
	private static void unloadProcessUnitParams(String id)
	{
		m_process_unit_params_map.remove(id);
	}
	
	private static void startProcessUnit(String id, Map<String, Object> params) throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		//mandatory inspection
//		if (m_process_unit_params_map.get(id) == null)
//		{
//			throw new RuntimeException("no such process unit, load it first");
//		}
		
		String category = (String) params.get(PARAM_CATEGORY);
		Map<String, Object> config = ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_PROCESS_UNIT, category);
//		System.out.println("----ProcessUnitUtils-startProcessUnit");
//		System.out.println(id);
//		System.out.println(category);
//		System.out.println(params);
//		System.out.println(config);
		ProcessUnit process_unit = new ProcessUnit(config, params);
		addProcessUnit(id, process_unit);
		process_unit.start();
		
		LogUtils.debugFormat("%s\t%s\t%s\t%s", ProcessUnitUtils.class.getSimpleName(), LogUtils.TYPE_START, category, LogUtils.CONTENT_SUCCESSFUL);
	}
	
	private static void stopProcessUnit(String id)
	{
		ProcessUnit process_unit = removeProcessUnit(id);
		process_unit.stop();
		LogUtils.debugFormat("%s\t%s\t%s\t%s", ProcessUnitUtils.class.getSimpleName(), LogUtils.TYPE_STOP, id, LogUtils.CONTENT_SUCCESSFUL);
	}
	
	public static void startProcessUnit(String id, Map<String, Object> params, int count) throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		for (int i = 0; i < count; i ++)
		{
			startProcessUnit(id, params);
		}
	}
	
	public static void stopProcessUnit(String id, int count)
	{
		for (int i = 0; i < count; i ++)
		{
			stopProcessUnit(id);
		}
	}
	
	public static  int adjustProcessUnit(String id) throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException{
		Map<String,Object> params=getProcessUnitParams(id);
		int count = Integer.parseInt((String) params.get(PARAM_COUNT));
		return adjustProcessUnit(id,params,count);
	} 
	
	public static int adjustProcessUnit(String id, Map<String, Object> params, int count) throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		if (count < 0)
		{
			throw new RuntimeException("invalid param count:" + count);
		}
		
		int current_count = getProcessUnitCount(id);
		int diff = count - current_count;
		if (diff < 0)
		{
			stopProcessUnit(id, Math.abs(diff));
		}
		else if (diff > 0)
		{
			startProcessUnit(id, params, diff);
		}
		else
		{
			;
		}
		return diff;
	}
	
	private static boolean addProcessUnit(String key, ProcessUnit process_unit)
	{
		LinkedList<ProcessUnit> list = getLinkedList(key);
		return list.add(process_unit);
	}
	
	private static ProcessUnit removeProcessUnit(String key)
	{
		LinkedList<ProcessUnit> list = getLinkedList(key);
		return list.removeFirst();
	}
	
	private static int getProcessUnitCount(String key)
	{
		LinkedList<ProcessUnit> list = getLinkedList(key);
		return list.size();
	}

	private static LinkedList<ProcessUnit> getLinkedList(String key)
	{
		LinkedList<ProcessUnit> list = m_process_unit_map.get(key);
		if (list == null)
		{
			list = new LinkedList<ProcessUnit>();
			m_process_unit_map.put(key, list);
		}
		return list;
	}

	
	
	public static boolean startAllProcessUnits() throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		for (Entry<String, Map<String, Object>> entry : m_process_unit_params_map.entrySet())
		{
			String id = entry.getKey();
			Map<String, Object> params = entry.getValue();
			int count = Integer.parseInt((String) params.get(PARAM_COUNT));

			try
			{
				adjustProcessUnit(id, params, count);
			}
			catch (Exception e)
			{
				LogUtils.warn(e);
				return false;
			}
		}
		
		LogUtils.debugFormat("%s\t%s\t%s", ProcessUnitUtils.class.getSimpleName(), LogUtils.TYPE_START, LogUtils.CONTENT_SUCCESSFUL);
		return true;
	}

	public static boolean stopAllProcessUnits()
	{
		for (String id : m_process_unit_params_map.keySet())
		{
			try
			{
				adjustProcessUnit(id, null, 0);
			}
			catch (Exception e)
			{
				LogUtils.warn(e);
				return false;
			}
		}
		
		LogUtils.debugFormat("%s\t%s\t%s", ProcessUnitUtils.class.getSimpleName(), LogUtils.TYPE_STOP, LogUtils.CONTENT_SUCCESSFUL);
		return true;
	}

	public static boolean load(String category, String id, Map<String, Object> params)
	{
		loadProcessUnitParams(params);
		return true;
	}

	public static boolean unload(String id) throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		adjustProcessUnit(id, null, 0);
		unloadProcessUnitParams(id);
		return true;
	}

	public static void unloadAllProcessUnits() throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		for (Entry<String, Map<String, Object>> entry: m_process_unit_params_map.entrySet())
		{
			String id = entry.getKey();
			adjustProcessUnit(id, null, 0);
		}
		m_process_unit_params_map = new ConcurrentHashMap<String, Map<String,Object>>();
	}
}
