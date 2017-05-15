package com.blueline.flowprocess.core.config;

import java.util.Map;

public interface IConfigClient
{
	public String generateId();
	public <V> V getConfig(String type, String name);
	public void init(Map<String, Object> param);
//	public String getConfigString(String type, String name);
//	public <V> V string2Map(String config_str);
//	public Map<String, Object> stringToMap(String string);
	public <V> V getConfig(String type, String name, String format, Map<String, Object> param_map);
}
