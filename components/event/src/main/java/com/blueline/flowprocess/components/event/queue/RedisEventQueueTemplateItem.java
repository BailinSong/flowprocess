package com.blueline.flowprocess.components.event.queue;

import java.util.Map;

import com.blueline.commons.JedisUtil;

public class RedisEventQueueTemplateItem extends RedisEventQueue {

//	protected JedisUtil m_jedis;
	
	@Override
	public void init(Map<String, Object> config) {
		
		super.setId((String) config.get(PARAM_ID));
		super.m_key = (String) config.get(RedisEventQueueTemplate.PARAM_KEY);
		super.m_id=(String) config.get(RedisEventQueueTemplate.PARAM_ID);
		super.m_jedis_util=(JedisUtil) config.get(RedisEventQueueTemplate.PARAM_REDIS);
		
	}

}
