package com.flowprocess.cedf.command;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class CedfCommand
{
	public static final String PARAM_RANGE = "range";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_COMMAND_WORD = "commandWord";
	public static final String PARAM_ACTION = "action";
	public static final String PARAM_PARAMS = "params";
	
	private final Map<String, Object> m_map;

	public CedfCommand(Map<String, Object> map)
	{
		m_map = map;
	}

	public CedfCommand(String range, String name, String command_word, String action, Map<String, Object> params)
	{
		m_map = new HashMap<String, Object>();
		setRange(range);
		setName(name);
		setCommandWord(command_word);
		setAction(action);
		setParams(params);
	}
	
	public String getRange()
	{
		return (String) m_map.get(PARAM_RANGE);
	}
	
	public void setRange(String value)
	{
		m_map.put(PARAM_RANGE, value);
	}
	
	public String getName()
	{
		return (String) m_map.get(PARAM_NAME);
	}
	
	public void setName(String value)
	{
		m_map.put(PARAM_NAME, value);
	}

	public String getCommandWord()
	{
		return (String) m_map.get(PARAM_COMMAND_WORD);
	}
	
	public void setCommandWord(String value)
	{
		m_map.put(PARAM_COMMAND_WORD, value);
	}
	
	public String getAction()
	{
		return (String) m_map.get(PARAM_ACTION);
	}
	
	public void setAction(String value)
	{
		m_map.put(PARAM_ACTION, value);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getParams()
	{
		return (Map<String, Object>) m_map.get(PARAM_PARAMS);
	}
	
	public void setParams(Map<String, Object> value)
	{
		m_map.put(PARAM_PARAMS, value);
	}

	public Map<String, Object> toMap()
	{
		return m_map;
	}
	
	public static CedfCommand fromMap(Map<String, Object> map)
	{
		return new CedfCommand(map);
	}
	
	@Override
	public String toString()
	{
		return m_map.toString();
	}

	public String toJsonString()
	{
		return JSON.toJSONString(m_map);
	}
}
