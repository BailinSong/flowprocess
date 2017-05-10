package com.flowprocess.cedf.components.event.queue;

import java.util.List;
import java.util.Map;

import com.flowprocess.cedf.components.manager.JedisUtilManager;
import com.flowprocess.cedf.config.ConfigUtils;
import com.flowprocess.cedf.event.Event;
import com.flowprocess.cedf.event.EventUtils;
import com.flowprocess.cedf.event.IEventQueue;
import com.flowprocess.cedf.log.LogUtils;
import com.flowprocess.commons.JedisUtil;

import redis.clients.jedis.JedisCommands;

public class RedisEventQueue implements IEventQueue {

	/** 
	* @Fields STATUS_SUCCESSFUL TODO(��һ�仰�������������ʾʲô) 
	*/ 
	private static final String STATUS_SUCCESSFUL = "successful";
	/** 
	* @Fields PARAM_PASSWORD TODO(��һ�仰�������������ʾʲô) 
	*/ 
	private static final String PARAM_PASSWORD = "Password";
	/** 
	* @Fields PARAM_IP TODO(��һ�仰�������������ʾʲô) 
	*/ 
	private static final String PARAM_IP = "Ip";
	/** 
	* @Fields PARAM_PORT TODO(��һ�仰�������������ʾʲô) 
	*/ 
	private static final String PARAM_PORT = "Port";
	/** 
	* @Fields DB_INDEX TODO(��һ�仰�������������ʾʲô) 
	*/ 
	private static final String PARAM_DB_INDEX = "DBIndex";
	/** 
	* @Fields PARAM_KEY TODO(��һ�仰�������������ʾʲô) 
	*/ 
	protected static final String PARAM_KEY = "Key";
	protected static final String PARAM_ID = "id";
	protected String m_key;
	protected String m_id;

	protected JedisUtil m_jedis_util;
	protected int m_db_index = 0;
	protected String getId() {
		return m_id;
	}

	protected void setId(String value) {
		m_id = value;
	}

	@Override
	public void init(Map<String, Object> config) {
		String id = (String) config.get(PARAM_ID);
		setId(id);
		m_key = (String) config.get(PARAM_KEY);
		try {
			m_db_index = Integer.valueOf((String) config.get(PARAM_DB_INDEX));
		} catch (NumberFormatException e) {
		}
		
		

		//m_jedis_util = new JedisUtil((Map<String, Object>) ConfigUtils.getConfig(Config.STORAGE, (String)config.get(Config.STORAGE)));
		m_jedis_util = JedisUtilManager.getJedisUtilInstance((String)config.get(ConfigUtils.CONFIG_TYPE_STORAGE));
	}

	@Override
	public void start() {
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(),
				getId(), LogUtils.TYPE_START, STATUS_SUCCESSFUL);

	}

	@Override
	public void stop() {
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(),
				getId(), LogUtils.TYPE_STOP, STATUS_SUCCESSFUL);

	}

	@Override
	public void add(Event event) {
		if (event != null) {
			JedisCommands jedis = m_jedis_util.getJedis();
			try {
				jedis.lpush(m_key, event.toString());
			} catch (Exception e) {
				LogUtils.warn(m_id,e);
			}finally {
				m_jedis_util.returnResource(jedis);
			}
			
		}
	}

	@Override
	public Event take(int timeout) throws InterruptedException {
		LogUtils.traceFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), m_id, LogUtils.TYPE_GET, m_key);
		JedisCommands jedis = null;
		try {
			jedis = m_jedis_util.getJedis();
			List<String> list = jedis.brpop(timeout, m_key);
			
			if (list != null && list.size() > 1) {
				return EventUtils.fromString(list.get(1));
			}
		} catch (Exception e) {
			LogUtils.warn(m_id,e);
			//e.printStackTrace();
		} finally{
			m_jedis_util.returnResource(jedis);
		}
		return null;
	}

	/* (�� Javadoc) 
	* <p>Title: poll</p> 
	* <p>Description: </p> 
	* @return 
	* @see com.ws.cedf.event.IEventQueue#poll() 
	*/ 
	@Override
	public Event poll() {
		JedisCommands jedis = null;
		try {
			jedis = m_jedis_util.getJedis();
			String event = jedis.rpop(m_key);
			if(event==null||event.isEmpty()){
				return null;
			}else{
				return EventUtils.fromString(event);
			}
		} catch (Exception e) {
			LogUtils.warn(e);
			//e.printStackTrace();
		} finally{
			m_jedis_util.returnResource(jedis);
		}
		return null;
	}

}
