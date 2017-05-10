package com.flowprocess.cedf.components.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.flowprocess.cedf.config.ConfigUtils;
import com.flowprocess.commons.JedisUtil;

public class JedisUtilManager
{
	private JedisUtilManager(){}
	
	static Map<String, JedisUtil> m_jedisutil_instances_map = new ConcurrentHashMap<String, JedisUtil>();
	
	public static JedisUtil  getJedisUtilInstance(String storageName)
	{
		if(storageName == null || storageName.isEmpty())
		{
			throw new RuntimeException("when get JedisUtil Instance, parameter is null");
		}
		
		JedisUtil jedis_util = m_jedisutil_instances_map.get(storageName);
		
		if(jedis_util == null)
		{
			Map<String, Object> storage_config = ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_STORAGE, storageName);
			if (storage_config == null || storage_config.isEmpty())
			{
				throw new RuntimeException(String.format("when get %s JedisUtil Instance, call ConfigUtils.getConfigconfig method return null", storageName));
			}
			
			jedis_util =  new JedisUtil(storage_config);
			m_jedisutil_instances_map.put(storageName, jedis_util);
		}
		
		return jedis_util;
	}
	
}
