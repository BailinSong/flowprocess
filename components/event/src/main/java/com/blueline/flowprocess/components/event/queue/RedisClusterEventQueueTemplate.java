package com.blueline.flowprocess.components.event.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.blueline.commons.JedisUtil;
import com.blueline.flowprocess.components.manager.JedisUtilManager;
import com.blueline.flowprocess.core.config.ConfigUtils;
import com.blueline.flowprocess.core.event.Event;
import com.blueline.flowprocess.core.event.IEventQueue;
import com.blueline.flowprocess.core.event.IEventQueueTemplate;
import com.blueline.flowprocess.core.log.LogUtils;

public class RedisClusterEventQueueTemplate implements IEventQueueTemplate {

	/**
	 * @Fields PARAM_KEYBASE TODO(��һ�仰�������������ʾʲô)
	 */
	private static final String PARAM_KEYBASE = "BaseKey";
	private Map<String, Object> m_config;
	private String m_key_base;

	private Map<String, IEventQueue> m_map = new ConcurrentHashMap<String, IEventQueue>();

	private JedisUtil m_jedis_util;
	private int m_db_index = 0;

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

	protected static final String PARAM_REDIS = "redis";

	
	private String m_id;

	private String getId() {
		return m_id;
	}

	private void setId(String value) {
		m_id = value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(Map<String, Object> config) {

		m_config = config;

		m_key_base = (String) m_config.get(PARAM_KEYBASE);

		String id = (String) config.get(PARAM_ID);

		setId(id);

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
		
		Set<Entry<String, IEventQueue>> set = m_map.entrySet();

		for (Entry<String, IEventQueue> event_queue : set) {
			
					event_queue.getValue().stop();
		}
		
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(),
				getId(), LogUtils.TYPE_STOP, STATUS_SUCCESSFUL);

	}

	@Override
	public void add(String field, Event event) {
		if (event != null) {
			IEventQueue eq = getEventQueue(field);
			try {
				eq.add(event);
			}catch (RuntimeException e) {
				throw e;
			} 
			catch (Exception e) {
				LogUtils.error(e);
				throw new RuntimeException(e);
			}
			
		}
	}

	/*
	 * (�� Javadoc) <p>Title: getEventQueue</p> <p>Description: </p>
	 * 
	 * @param field
	 * 
	 * @return
	 * 
	 * @see
	 * com.ws.cedf.event.IEventQueueTemplate#getEventQueue(java.lang.String)
	 */
	@Override
	public IEventQueue getEventQueue(String field) {

		IEventQueue eq = m_map.get(getItemKey(field));
		if (eq == null) {
			try {
				eq = newEventQueue(field);
			} catch (RuntimeException e) {
				throw e;
			}catch (Exception e) {
				LogUtils.error(e);
				throw new RuntimeException(e);
			}
		}
		return eq;
	}

	private synchronized IEventQueue newEventQueue(final String field) {

		IEventQueue eq = m_map.get(getItemKey(field));
		if (eq != null) {
			return eq;
		}

		eq = new RedisClusterEventQueueTemplateItem();

		Map<String, Object> config = new HashMap<String, Object>() ;
		config.put(PARAM_ID,getItemID(field));
		config.put(PARAM_KEY, getItemKey(field));
		config.put(PARAM_REDIS, m_jedis_util);
		
		eq.init(config);
		eq.start();
		m_map.put(getItemKey(field), eq);
		return eq;
	}

	protected String getItemKey(String field) {
		return new StringBuffer().append(m_key_base).append("_").append(field)
				.toString();
	}
	
	protected String getItemID(String field) {
		return new StringBuffer().append(m_id).append("[").append(field).append("]")
				.toString();
	}

}
