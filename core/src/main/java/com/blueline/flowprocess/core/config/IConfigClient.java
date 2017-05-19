package com.blueline.flowprocess.core.config;
import java.util.Map;
public interface IConfigClient
{
	public String generateId();
	public <V> V getConfig(String type, String name);
	public void init(Map<String, Object> param);
	public <V> V getConfig(String type, String name, String format, Map<String, Object> param_map);
}
