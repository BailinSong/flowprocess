package com.blueline.flowprocess.components.service.storage.redis;

import java.util.HashMap;
import java.util.Map;

import com.blueline.commons.JedisUtil;
import com.blueline.commons.ReflectUtils;
import com.blueline.flowprocess.components.service.storage.api.ExceeStorageService;
import com.blueline.flowprocess.components.service.storage.api.IHandler;
import com.blueline.flowprocess.components.service.storage.api.MapStorageService;
import com.blueline.flowprocess.core.event.EventUtils;
import com.blueline.flowprocess.core.log.LogUtils;

public class RedisMatchAndExpireStorageService extends MapStorageService<String, Map<String, Object>> {

	JedisUtil m_jedis_util = null;
	String m_base_key = "";
	static final String PARAM_BASE_KEY = "BaseKey";
	static final String PARAM_DEST_PROCESS_QUEUE = "DestProcessQueue";
	static final String PARAM_MAX_PROCESS_QUEUE_SIZE = "MaxProcessQueueSize";
	static final String PARAM_IP = "Ip";
	static final String PARAM_PORT = "Port";
	static final String PARAM_PASSWORD = "Password";
	static final String PARAM_EXPIRED_HANDLER = "ExpiredHandler";
	static final String PARAM_INTERVAL = "Interval";
	
	static final String PARAM_EXPIRED_STORAGE_CLASS = "ExpiredStorageClass";
	static final String PARAM_MATCHABLE_STORAGE_CLASS = "MatchableStorageClass";
	
	static final String KEY_SEPARATOR = "_";
	static final String KEY_SUFFIX = "ExpireIndex";
	static final String HANDLE_TYPE_DEFAULT = "Default";
	static final String HANDLE_TYPE_BYUSER = "ByUser";
	static final String HANDLE_TYPE_WAITING_ANSWER_COLLECT = "WaitingAnswerCollectHandler";
	static final String HANDLE_TYPE_WAITING_REPORT_COLLECT = "WaitingReportCollectHandler";
	static final String HANDLE_TYPE_BY_ACC_SVCID_AND_USER="ByAccSvcIdAndUser";
	
	static final String KEY_DATA = "Data"; 
	static final String KEY_EXPIRE_KEY = "ExpireKey"; 
	
	
	private static final String DATA_KEY = "key";
	
	String m_dest_process_queue;
	String m_expired_handler = HANDLE_TYPE_DEFAULT;
	IHandler<Map<String, Object>> m_handler = null;
	
	ExceeStorageService<Map<String,Object>> m_redis_expire_storage_service = null;//new RedisExpireStorageService();  
	MapStorageService<String,Map<String,Object>> m_redis_matchable_storage_service = null;//new RedisMatchableStorageService();
	@Override
	public void init(Map<String, Object> config) {
		
		
		String expire_class=(String)config.get(PARAM_EXPIRED_STORAGE_CLASS);
		if(expire_class!=null&&!expire_class.isEmpty()){
			try{
				m_redis_expire_storage_service=ReflectUtils.newObject(expire_class);
			}catch (Exception e) {
				LogUtils.errorFormat("{} get class {} error ", PARAM_EXPIRED_STORAGE_CLASS,expire_class);
				throw new RuntimeException(e);
			}
		}else{
			m_redis_expire_storage_service=new RedisExpireStorageService();
		}
		
		String matchable_class=(String)config.get(PARAM_MATCHABLE_STORAGE_CLASS);
		if(matchable_class!=null&&!matchable_class.isEmpty()){
			try{
				m_redis_matchable_storage_service=ReflectUtils.newObject(matchable_class);
			}catch (Exception e) {
				LogUtils.errorFormat("{} get class {} error ", PARAM_MATCHABLE_STORAGE_CLASS,matchable_class);
				throw new RuntimeException(e);
			}
		}else{
			m_redis_matchable_storage_service=new RedisMatchableStorageService();
		}
		
		
		m_dest_process_queue = (String) config.get(PARAM_DEST_PROCESS_QUEUE);
		m_expired_handler = (String) config.get(PARAM_EXPIRED_HANDLER);
		if(null!=m_expired_handler&&!m_expired_handler.isEmpty()){
			try {
				m_handler = new DefaultHandler((IHandler<Map<String, Object>>)ReflectUtils.newObject(m_expired_handler));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}else{
			m_handler = new DefaultHandler(null);
		}
		
		m_redis_matchable_storage_service.init(config);
		m_redis_expire_storage_service.init(config);
		
		m_redis_expire_storage_service.regHandler(m_handler);
		
	}
	public void setDefaultInterval(long interval) {
		m_redis_expire_storage_service.setDefaultInterval(interval);
	}
	
	
	
	@Override
	public Map<String, Object> put(String key, Map<String, Object> value) {
		
		Map<String, Object> result = null;
		Map<String,Object> map = new HashMap<String, Object>();
		map.put(DATA_KEY, key);
		String current_expire_key = m_redis_expire_storage_service.add(map);
		if(current_expire_key != null && !current_expire_key.isEmpty())
		{
			map.clear();
			map.put(KEY_EXPIRE_KEY, current_expire_key);
			map.put(KEY_DATA, value);
			result = m_redis_matchable_storage_service.put(key, map);

		}
		
		return result;
	}
	
	public Map<String, Object> put(String key, Map<String, Object> value,
			long overtime) {
		/*Map<String, Object> result = m_redis_matchable_storage_service.put(key, value);
		if(result != null && !result.isEmpty()){
			Map<String,Object> map=new HashMap<String, Object>();
			map.put(DATA_KEY, key);
			m_redis_expire_storage_service.add(map,overtime);
		}*/
		Map<String, Object> result = null;
		Map<String,Object> map = new HashMap<String, Object>();
		map.put(DATA_KEY, key);
		String current_expire_key = m_redis_expire_storage_service.add(map, overtime);
		if(current_expire_key != null && !current_expire_key.isEmpty())
		{
			map.clear();
			map.put(KEY_EXPIRE_KEY, current_expire_key);
			map.put(KEY_DATA, value);
			result = m_redis_matchable_storage_service.put(key, map);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> get(String key) {
		//return m_redis_matchable_storage_service.get(key);
		Map<String,Object> result = m_redis_matchable_storage_service.get(key);
		if(result != null && !result.isEmpty())
		{
			return (Map<String, Object>) result.get(KEY_DATA);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> remove(String key) {
		Map<String, Object> result = m_redis_matchable_storage_service.remove(key);
		if(result != null && !result.isEmpty())
		{
			String expire_key = (String) result.get(KEY_EXPIRE_KEY);
			Map<String,Object> map = new HashMap<String, Object>();
			map.put(DATA_KEY, key);
			m_redis_expire_storage_service.remove(expire_key, map);
			
			result = (Map<String, Object>) result.get(KEY_DATA);
		}
		return result;
	}
	@Override
	public boolean containsKey(String key) {
		return m_redis_matchable_storage_service.containsKey(key);
	}
	@Override
	public void start() {
		m_redis_expire_storage_service.start();
	}
	@Override
	public void stop() {
		m_redis_expire_storage_service.stop();
	}
	
	class DefaultHandler implements IHandler<Map<String,Object>>{

		IHandler<Map<String,Object>> m_handler=null;
		public DefaultHandler(IHandler<Map<String,Object>> handler) {
			m_handler=handler;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean handle(Map<String, Object> param) {
			try {
				String key = (String)((Map<String,Object>)param.get(IHandler.PARAM_EXPIRED_DATA)).get(DATA_KEY);
				Map<String,Object> object=m_redis_matchable_storage_service.remove(key);
				
				if(object==null){
					return true;
				}else{
					param.put(IHandler.PARAM_EXPIRED_DATA, (Map<String,Object>)object.get(KEY_DATA));
				}
				if(m_handler!=null){
					return m_handler.handle(param);
				}else{
					EventUtils.add((String)param.get(IHandler.PARAM_DEST_PROCESS_QUEUE), null, (Map<String,Object>)param.get(IHandler.PARAM_EXPIRED_DATA));
				}
				return true;
			} catch (Exception e) {
				LogUtils.warn(e);
				//e.printStackTrace();
			}
			return false;
		}
		
	}
	
	
}
