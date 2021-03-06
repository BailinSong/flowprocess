package com.blueline.flowprocess.components.event.queue;
import java.util.Map;
import com.blueline.commons.JedisUtil;
import com.blueline.commons.RedisClusterItem;
public class RedisClusterEventQueueTemplateItem extends RedisClusterEventQueue {
	@Override
	public void init(Map<String, Object> config) {
		super.setId((String) config.get(PARAM_ID));
		super.m_key = (String) config.get(RedisEventQueueTemplate.PARAM_KEY);
		super.m_id=(String) config.get(RedisEventQueueTemplate.PARAM_ID);
		super.m_jedis_util=(JedisUtil) config.get(RedisEventQueueTemplate.PARAM_REDIS);
		super.m_queue=new RedisClusterItem(m_jedis_util, m_key, 300);
	}
}
