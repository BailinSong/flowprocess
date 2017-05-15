package com.blueline.flowprocess.components.service.storage.redis;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.blueline.commons.JedisUtil;
import com.blueline.flowprocess.components.manager.JedisUtilManager;
import com.blueline.flowprocess.components.service.storage.api.MapStorageService;
import com.blueline.flowprocess.core.config.ConfigUtils;
import com.blueline.flowprocess.core.log.LogUtils;

import redis.clients.jedis.JedisCommands;

public class RedisMatchableStorageService extends MapStorageService<String, Map<String,Object>> {

	JedisUtil m_jedis_util = null;
	String m_base_key = "";
	static final String PARAM_BASE_KEY = "BaseKey";
	static final String PARAM_IP = "Ip";
	static final String PARAM_PORT = "Port";
	static final String PARAM_PASSWORD = "Password";
	static final String KEY_SEPARATOR = "_";
	
	@Override
	public void init(Map<String, Object> config) {
		if(config == null || config.isEmpty()){
			throw new RuntimeException("MatchableStorageService config is null");
		}
		
		Map<String, Object> storage_config = ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_STORAGE, (String) config.get(ConfigUtils.CONFIG_TYPE_STORAGE));
		if (storage_config == null || storage_config.isEmpty() )
		{
			throw new RuntimeException("RedisMatchableStorageService config <Storage> is null");
		}
		//storage_config = (Map<String, Object>) storage_config.get(Config.STORAGE);
		
		String ip = (String) storage_config.get(PARAM_IP);
		if(ip == null || ip.isEmpty()) {
			throw new RuntimeException("MatchableStorageService <Ip> config is null");
		}
		String pwd = (String) storage_config.get(PARAM_PASSWORD);
		int port = -1;
		try {
			port = Integer.valueOf((String) storage_config.get(PARAM_PORT));
		} catch (NumberFormatException e1) {
			//e1.printStackTrace();
			throw new RuntimeException("MatchableStorageService <Port> config is null");
			
		}
		//m_jedis_util = new JedisUtil(ip, port, pwd, 0);
		m_jedis_util = JedisUtilManager.getJedisUtilInstance((String)config.get(ConfigUtils.CONFIG_TYPE_STORAGE));
		m_base_key = (String)config.get(PARAM_BASE_KEY);
		if(m_base_key == null || m_base_key.isEmpty()) {
			throw new RuntimeException("MatchableStorageService <BaseKey> config is null");
		}
	}

	@Override
	public void start() {
		
		
	}

	@Override
	public void stop() {
		
		
	}

	/** 
	* @Title exist  
	* @param megid
	* @return     
	* @ReturnType boolean    �������� 
	* @Exception 
	*/ 
	public boolean exist(String megid) {
		return containsKey(megid);
	}

	@Override
	public Map<String, Object> put(String key, Map<String, Object> value) {
		if(key == null || key.isEmpty() || value == null || value.isEmpty()) {
			return null;
		}
		JedisCommands jedisCommands = null;
		try {
			jedisCommands = m_jedis_util.getJedis();
			String result = jedisCommands.set(m_base_key + KEY_SEPARATOR + key, JSON.toJSONString(value));
			if("OK".equalsIgnoreCase(result)) {
				return value;
			}
		} catch (Exception e) {
			LogUtils.warn(e);
			//e.printStackTrace();
		} finally{
			m_jedis_util.returnResource(jedisCommands);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> get(String key) {
		if(key == null || key.isEmpty()) {
			return null;
		}
		JedisCommands jedisCommands = null;
		try {
			jedisCommands = m_jedis_util.getJedis();
			String result = jedisCommands.get(m_base_key + KEY_SEPARATOR + key);
			if(result != null && !result.isEmpty()) {
				return JSON.parseObject(result, Map.class);
			}
		} catch (Exception e) {
			LogUtils.warn(e);
			//e.printStackTrace();
		} finally{
			m_jedis_util.returnResource(jedisCommands);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> remove(String key) {
		if(key == null || key.isEmpty()) {
			return null;
		}
		JedisCommands jedisCommands = null;
		String true_key = m_base_key + KEY_SEPARATOR + key;
		try {
			jedisCommands = m_jedis_util.getJedis();
			String result = jedisCommands.get(true_key);
			if(result != null && !result.isEmpty()) {
				long res = jedisCommands.del(true_key);
				return res < 1 ? null : JSON.parseObject(result, Map.class);
			}
		} catch (Exception e) {
			LogUtils.warn(e);
			//e.printStackTrace();
		} finally{
			m_jedis_util.returnResource(jedisCommands);
		}
		return null;
	}

	@Override
	public boolean containsKey(String key) {
		if(key == null || key.isEmpty()) {
			return false;
		}
		JedisCommands jedisCommands = null;
		try {
			jedisCommands = m_jedis_util.getJedis();
			return jedisCommands.exists(m_base_key + KEY_SEPARATOR + key);
		} catch (Exception e) {
			//e.printStackTrace();
			LogUtils.warn(e);
		} finally{
			m_jedis_util.returnResource(jedisCommands);
		}
		return false;
	}


}
