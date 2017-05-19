package com.blueline.flowprocess.components.service.storage.redis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.alibaba.fastjson.JSON;
import com.blueline.commons.JedisUtil;
import com.blueline.commons.ReflectUtils;
import com.blueline.flowprocess.components.manager.JedisUtilManager;
import com.blueline.flowprocess.components.service.storage.api.ExceeStorageService;
import com.blueline.flowprocess.components.service.storage.api.IHandler;
import com.blueline.flowprocess.core.config.ConfigUtils;
import com.blueline.flowprocess.core.event.EventUtils;
import com.blueline.flowprocess.core.log.LogUtils;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Tuple;
public class RedisExpireStorageService extends ExceeStorageService<Map<String,Object>> {
	static final String PARAM_BASE_KEY = "BaseKey";
	static final String PARAM_DEST_PROCESS_QUEUE = "DestProcessQueue";
	static final String PARAM_MAX_PROCESS_QUEUE_SIZE = "MaxProcessQueueSize";
	static final String PARAM_IP = "Ip";
	static final String PARAM_PORT = "Port";
	static final String PARAM_PASSWORD = "Password";
	static final String PARAM_EXPIRED_HANDLER = "ExpiredHandler";
	static final String PARAM_INTERVAL = "Interval";
	static final String KEY_SEPARATOR = "_";
	static final String KEY_SUFFIX = "ExpireIndex";
	static final String HANDLE_TYPE_DEFAULT = "Default";
	static final String HANDLE_TYPE_BYUSER = "ByUser";
	JedisUtil m_jedis_util = null;
	String m_base_key = "";
	String m_dest_process_queue;
	int m_max_process_queue_size = 1;
	String m_expired_handler = HANDLE_TYPE_DEFAULT;
	long m_default_interval = 0;
	IHandler<Map<String, Object>> m_handler = null;
	ExecutorService m_executor = Executors.newCachedThreadPool();
	final AtomicInteger processor_count = new AtomicInteger(0);
	final Lock processor_count_lock=new ReentrantLock();
	AtomicBoolean m_exit_flag = new AtomicBoolean(false);
	@Override
	public void init(Map<String, Object> config) {
		if(config == null || config.isEmpty()){
			throw new RuntimeException("RedisExpireStorageService config is null");
		}
		Map<String, Object> storage_config = ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_STORAGE, (String) config.get(ConfigUtils.CONFIG_TYPE_STORAGE));
		if (storage_config == null || storage_config.isEmpty())
		{
			throw new RuntimeException("RedisExpireStorageService config <Storage> is null");
		}
		String ip = (String) storage_config.get(PARAM_IP);
		if(ip == null || ip.isEmpty()) {
			throw new RuntimeException("RedisExpireStorageService <Ip> config is null");
		}
		String pwd = (String) storage_config.get(PARAM_PASSWORD);
		int port = -1;
		try {
			port = Integer.valueOf((String) storage_config.get(PARAM_PORT));
		} catch (Exception e1) {
			LogUtils.warn(e1);
			throw new RuntimeException("RedisExpireStorageService <Port> config is null");
		}
		m_jedis_util = JedisUtilManager.getJedisUtilInstance((String)config.get(ConfigUtils.CONFIG_TYPE_STORAGE));
		m_base_key = (String)config.get(PARAM_BASE_KEY);
		if(m_base_key == null || m_base_key.isEmpty()) {
			throw new RuntimeException("RedisExpireStorageService <BaseKey> config is null");
		}
		try {
			m_max_process_queue_size = Integer.valueOf((String) config.get(PARAM_MAX_PROCESS_QUEUE_SIZE));
		} catch (Exception e) {
			LogUtils.warn(e);
			m_max_process_queue_size = 1;
		}
		try {
			m_default_interval = Integer.valueOf((String) config.get(PARAM_INTERVAL));
		} catch (Exception e) {
			m_default_interval = 0;
			LogUtils.warnFormat("RedisExpireStorageService <Interval> is used dafault value {}", m_default_interval);
		}
		m_dest_process_queue = (String) config.get(PARAM_DEST_PROCESS_QUEUE);
		m_expired_handler = (String) config.get(PARAM_EXPIRED_HANDLER);
		if(null!=m_expired_handler&&!m_expired_handler.isEmpty()){
			try {
				m_handler = ReflectUtils.newObject(m_expired_handler);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}else{
			m_handler = new DefaultHandler();
		}
	}
	@Override
	public void start() {
		m_executor.execute(processor());
	}
	@Override
	public void stop() {
		m_exit_flag.set(true);
		m_executor.shutdown();
	}
	@Override
	public void setDefaultInterval(long interval) {
		if(interval >= 0) {
			m_default_interval = interval;
		}
	}
	@Override
	public void regHandler(IHandler<Map<String, Object>> handler) {
		m_handler = handler;
	}
	@Override
	public String add(Map<String, Object> data) {
		if(data == null || data.isEmpty()) {
			return null;
		}
		long time_stamp = System.currentTimeMillis();
		long next_scheduletime = getNextScheduletime(time_stamp, m_default_interval);
		JedisCommands jedisCommands = null;
		try {
			jedisCommands = m_jedis_util.getJedis();
			String key = m_base_key + KEY_SEPARATOR + next_scheduletime;
			long count = jedisCommands.lpush(key, JSON.toJSONString(data));
			if(count%10 == 1)
			{
				jedisCommands.zadd(m_base_key + KEY_SUFFIX, next_scheduletime, key);
			}
			return key;
		} catch (Exception e) {
			LogUtils.warn(e);
		} finally{
			m_jedis_util.returnResource(jedisCommands);
		}
		return null;
	}
	@Override
	public String add(Map<String, Object> data, long ouvertime) {
		if(data == null || data.isEmpty()) {
			return null;
		}
		if(ouvertime < 0){
			ouvertime = 0;
		}
		long time_stamp = System.currentTimeMillis();
		long next_scheduletime = getNextScheduletime(time_stamp, ouvertime);
		JedisCommands jedisCommands = null;
		try {
			jedisCommands = m_jedis_util.getJedis();
			String key = m_base_key + KEY_SEPARATOR + next_scheduletime;
			long count = jedisCommands.lpush(key, JSON.toJSONString(data));
			if(count%10 == 1)
			{
				jedisCommands.zadd(m_base_key + KEY_SUFFIX, next_scheduletime, key);
			}
			return key;
		} catch (Exception e) {
			LogUtils.warn(e);
		} finally{
			m_jedis_util.returnResource(jedisCommands);
		}
		return null;
	}
	public Runnable processor() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
	            while (!m_exit_flag.get())
	            {
	                try
	                {
	                    int count = arrivalScheduletimeQueueProcessor();
	                    if(count == 0){
	                    	Thread.sleep(1000);
	                    }
	                }
	                catch (Exception e)
	                {
	                	continue;
	                }
	            }
	            m_exit_flag.set(true);
			}
		};
		return runnable;
	}
	public int arrivalScheduletimeQueueProcessor(){
		final String key = m_base_key + KEY_SUFFIX;
		List<String> arrival_scheduletime_queue_names = getArrivalScheduletimeQueueNames(key);
		if(arrival_scheduletime_queue_names == null || arrival_scheduletime_queue_names.isEmpty()){
			return 0;
		}
		for(final String queue_name : arrival_scheduletime_queue_names){
			if(queue_name == null || queue_name.isEmpty()){
				continue;
			}
			long exec_time = System.currentTimeMillis();
			final long accuracy_time = exec_time / 1000 * 1000;
			Runnable runnable = new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					JedisCommands jedisCommands = null;
					try {
						jedisCommands = m_jedis_util.getJedis();
						String data_json_str = null;
						Map<String,Object> handle_data;
						while((data_json_str = jedisCommands.rpop(queue_name)) != null)
						{
							if (data_json_str.isEmpty())
							{
								continue;
							}
							handle_data=new HashMap<String,Object>();
							Map<String, Object> data = JSON.parseObject(data_json_str, Map.class);
							handle_data.put(IHandler.PARAM_DEST_PROCESS_QUEUE, m_dest_process_queue);
							handle_data.put(IHandler.PARAM_EXPIRED_DATA, data);
							m_handler.handle(handle_data);
						}
						if(!queue_name.endsWith(String.valueOf(accuracy_time))){
							jedisCommands.zrem(key, queue_name);
						}
					} catch (Exception e) {
						LogUtils.warn(e);
					} finally{
						m_jedis_util.returnResource(jedisCommands);
						synchronized (processor_count_lock)
						{
							processor_count.decrementAndGet();
							processor_count_lock.notify();
						}
					}
				}
			};
			try {
				synchronized (processor_count_lock)
				{
					if(processor_count.get() >= m_max_process_queue_size){
						processor_count_lock.wait();
					}
					processor_count.incrementAndGet();
					m_executor.execute(runnable);
				}
			} catch (Exception e) {
				LogUtils.warn(e);
			}
		}
		return arrival_scheduletime_queue_names.size();
	}
	private long getNextScheduletime(long begin_time, long seconds_interval){
		long time = begin_time + seconds_interval * 1000;
		return (time / 1000) * 1000;
	}
	public List<String> getArrivalScheduletimeQueueNames(String queue_name){
		if(queue_name == null || queue_name.isEmpty()){
			return null;
		}
		long now = System.currentTimeMillis()-1000;
		List<String> scheduletime_queue_names = new ArrayList<String>();
		JedisCommands jedisCommands = null;
		try {
			jedisCommands = m_jedis_util.getJedis();
			Set<Tuple> tuples = jedisCommands.zrangeByScoreWithScores(queue_name, 0, now);
			if(tuples == null || tuples.isEmpty()){
				return null;
			}
			for (Tuple tuple : tuples) {
				String scheduletime_queue_name = tuple.getElement();
				if(scheduletime_queue_name != null && !scheduletime_queue_name.isEmpty()){
					scheduletime_queue_names.add(scheduletime_queue_name);
				}
			}
			return scheduletime_queue_names;
		} catch (Exception e) {
			LogUtils.warn(e);
		} finally{
			m_jedis_util.returnResource(jedisCommands);
		}
		return null;
	}
	class DefaultHandler implements IHandler<Map<String,Object>>{
		@Override
		public boolean handle(Map<String, Object> param) {
			try {
				EventUtils.add((String)param.get(IHandler.PARAM_DEST_PROCESS_QUEUE), null, (Map<String,Object>)param.get(IHandler.PARAM_EXPIRED_DATA));
				return true;
			} catch (Exception e) {
				LogUtils.warn(e);
			}
			return false;
		}
	}
	@Override
	public boolean remove(String expireKey, Map<String, Object> data)
	{
		if(expireKey == null || expireKey.isEmpty() || data == null || data.isEmpty())
		{
			return false;
		}
		JedisCommands jedisCommands = null;
		try {
			jedisCommands = m_jedis_util.getJedis();
			long rem_count = jedisCommands.lrem(expireKey, -1, JSON.toJSONString(data));
			return rem_count > 0 ? true : false;
		} catch (Exception e) {
			LogUtils.warn(e);
		} finally{
			m_jedis_util.returnResource(jedisCommands);
		}
		return false;
	}
}
