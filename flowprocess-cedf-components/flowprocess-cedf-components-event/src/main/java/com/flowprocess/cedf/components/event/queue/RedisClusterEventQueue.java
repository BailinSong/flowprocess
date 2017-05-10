package com.flowprocess.cedf.components.event.queue;

import java.util.Map;

import com.flowprocess.cedf.components.manager.JedisUtilManager;
import com.flowprocess.cedf.config.ConfigUtils;
import com.flowprocess.cedf.event.Event;
import com.flowprocess.cedf.event.EventUtils;
import com.flowprocess.cedf.event.IEventQueue;
import com.flowprocess.cedf.log.LogUtils;
import com.flowprocess.commons.JedisUtil;
import com.flowprocess.commons.RedisClusterItem;

public class RedisClusterEventQueue implements IEventQueue {

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
	protected RedisClusterItem m_queue;

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

		//m_jedis_util = new JedisUtil((Map<String, Object>) ConfigUtils.getConfig(Config.STORAGE, (String) config.get(Config.STORAGE)));
		m_jedis_util = JedisUtilManager.getJedisUtilInstance((String)config.get(ConfigUtils.CONFIG_TYPE_STORAGE));
		
		m_queue = new RedisClusterItem(m_jedis_util, m_key, 300);
	}

	@Override
	public void start() {
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), getId(), LogUtils.TYPE_START,
				STATUS_SUCCESSFUL);

	}

	@Override
	public void stop() {
		m_queue.stop();
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), getId(), LogUtils.TYPE_STOP,
				STATUS_SUCCESSFUL);

	}

	@Override
	public void add(Event event) {
		if (event != null) {
			m_queue.add(event.toString());
		}
	}

	@Override
	public Event take(int timeout) throws InterruptedException {
		LogUtils.traceFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), m_id, LogUtils.TYPE_GET, m_key);
		String event_json = m_queue.take(timeout);
		if(event_json == null)
		{
			return null;
		}
		return EventUtils.fromString(event_json);

	}

	/*
	 * (�� Javadoc) <p>Title: poll</p> <p>Description: </p>
	 * 
	 * @return
	 * 
	 * @see com.ws.cedf.event.IEventQueue#poll()
	 */
	@Override
	public Event poll() {
		String event = m_queue.poll();
		if (event == null || event.isEmpty()) {
			return null;
		} else {
			return EventUtils.fromString(event);
		}

	}

}
