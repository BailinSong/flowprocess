package com.flowprocess.cedf.components.event.queue;

import java.util.Map;

import com.flowprocess.commons.JedisUtil;
import com.flowprocess.commons.RedisClusterItem;

public class RedisClusterEventQueueTemplateItem extends RedisClusterEventQueue {

//	protected JedisUtil m_jedis;
	
	@Override
	public void init(Map<String, Object> config) {
		
		super.setId((String) config.get(PARAM_ID));
		super.m_key = (String) config.get(RedisEventQueueTemplate.PARAM_KEY);
		super.m_id=(String) config.get(RedisEventQueueTemplate.PARAM_ID);
		super.m_jedis_util=(JedisUtil) config.get(RedisEventQueueTemplate.PARAM_REDIS);
		super.m_queue=new RedisClusterItem(m_jedis_util, m_key, 300);
		
	}

}
