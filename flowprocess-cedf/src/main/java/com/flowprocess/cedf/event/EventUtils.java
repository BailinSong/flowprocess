package com.flowprocess.cedf.event;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;
import com.flowprocess.cedf.config.ConfigUtils;
import com.flowprocess.cedf.log.LogUtils;
import com.flowprocess.commons.ReflectUtils;

@SuppressWarnings("unchecked")
public class EventUtils {
	private EventUtils() {
	}

	private static final String PARAM_EVENT_QUEUE = "EventQueue";
	private static final String PARAM_ID = "id";
	private static final String PARAM_CLASS = "class";
	private static final String PARAM_EVENT_QUEUE_TEMPLATE = "EventQueueTemplate";
//	private static final String PARAM_KEYBASE = "keybase";

	private static Map<String, IEventQueue> m_event_queue_map = new ConcurrentHashMap<String, IEventQueue>();
	private static Map<String, IEventQueueTemplate> m_event_queue_template_map = new ConcurrentHashMap<String, IEventQueueTemplate>();

	// private static Map<String, IEventDataFactory> m_factory_map = new
	// ConcurrentHashMap<String, IEventDataFactory>();

	private static Map<String, AtomicInteger> m_event_queue_load_count_map = new ConcurrentHashMap<String, AtomicInteger>();
	private static Map<String, AtomicInteger> m_event_queue_template_load_count_map = new ConcurrentHashMap<String, AtomicInteger>();
	private static Map<String, AtomicBoolean> m_event_queue_status_map = new ConcurrentHashMap<String, AtomicBoolean>();
	private static Map<String, AtomicBoolean> m_event_queue_template_status_map = new ConcurrentHashMap<String, AtomicBoolean>();

	private static IEventQueueTemplate putEventQueueTemplate(String key, IEventQueueTemplate value) {
		return m_event_queue_template_map.put(key, value);
	}

	private static IEventQueue putEventQueue(String key, IEventQueue value) {
		return m_event_queue_map.put(key, value);
	}

	public static IEventQueue getEventQueue(String key) {
		return m_event_queue_map.get(key);
	}

	public static IEventQueue getEventQueue(String template_key, String field) {
		if (field == null) {
			return getEventQueue(template_key);
		} else {
			IEventQueueTemplate eqt = m_event_queue_template_map.get(template_key);
			return eqt.getEventQueue(field);
		}
	}

	// private static IEventQueue removeEventQueue(String key)
	// {
	// return m_event_queue_map.remove(key);
	// }

//	public static void add(String key, Event event) {
//		IEventQueue event_queue = m_event_queue_map.get(key);
//		if (event_queue == null) {
//			throw new RuntimeException("no such event queue");
//		}
//
//		event_queue.add(event);
//
//		LogUtils.traceFormat("%s\t%s\t%s\t%s", EventUtils.class.getSimpleName(), LogUtils.TYPE_ADD, key, event);
//	}

	public static void add(String key, String id, Map<String, Object> event_data) {
		add(key,null,id,event_data);
	}

	public static void add(String key, String field, String id, Map<String, Object> event_data) {
		IEventQueue event_queue = getEventQueue(key, field);
		if (event_queue == null) {
			if(field==null){
				throw new RuntimeException("no such event queue " + key);
			}else{
				throw new RuntimeException("no such event queue " + key + "_" + field);
			}
			
		}
		Event event = new Event();
		event.setTaskId(id);
		event.setData(event_data);
		event_queue.add(event);

		LogUtils.traceFormat("%s\t%s\t%s\t%s\t%s", EventUtils.class.getSimpleName(), LogUtils.TYPE_ADD, key, field,
				event);
	}

	public static Event take(String key, int timeout) throws InterruptedException {
		IEventQueue event_queue = m_event_queue_map.get(key);
		if (event_queue == null) {
				throw new RuntimeException("no such event queue " + key);
		}

		Event event = event_queue.take(timeout);

		LogUtils.traceFormat("%s\t%s\t%s\t%s", EventUtils.class.getSimpleName(), LogUtils.TYPE_ADD, key, event);
		return event;
	}

	public static Event poll(String key, String field) {
		IEventQueue event_queue = getEventQueue(key, field);
		if (event_queue == null) {
			throw new RuntimeException("no such event queue");
		}

		Event event = event_queue.poll();

		LogUtils.traceFormat("%s\t%s\t%s\t%s\t%s", EventUtils.class.getSimpleName(), LogUtils.TYPE_ADD, key, field,
				event);
		return event;
	}

	public static Event poll(String key) {
		return poll(key,null);
	}

	public static void init(Map<String, Object> config, Map<String, Object> params)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (config == null) {
			return;
		}

		Object event_queue_configs = config.get(PARAM_EVENT_QUEUE);
		if (event_queue_configs != null) {

			if (event_queue_configs instanceof List) {
				List<Map<String, Object>> event_queue_config_list = (List<Map<String, Object>>) event_queue_configs;
				for (int i = 0; i < event_queue_config_list.size(); i++) {
					Map<String, Object> event_queue_config = event_queue_config_list.get(i);
					loadEventQueue(event_queue_config);
				}
			} else {
				Map<String, Object> event_queue_config = (Map<String, Object>) event_queue_configs;
				loadEventQueue(event_queue_config);
			}
		}

		Object event_queue_template_configs = config.get(PARAM_EVENT_QUEUE_TEMPLATE);
		if (event_queue_template_configs != null) {

			if (event_queue_template_configs instanceof List) {
				List<Map<String, Object>> event_queue_template_config_list = (List<Map<String, Object>>) event_queue_template_configs;
				for (int i = 0; i < event_queue_template_config_list.size(); i++) {
					Map<String, Object> event_queue_template_config = event_queue_template_config_list.get(i);
					loadEventQueueTemplate(event_queue_template_config);
				}
			} else {
				Map<String, Object> event_queue_template_config = (Map<String, Object>) event_queue_template_configs;
				loadEventQueueTemplate(event_queue_template_config);
			}
		}

		LogUtils.infoFormat("%s\t%s\t%s", EventUtils.class.getSimpleName(), LogUtils.TYPE_INIT, "successful");
	}

	private static void loadEventQueue(Map<String, Object> event_queue_config)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String id = (String) event_queue_config.get(PARAM_ID);

		Map<String, Object> config = (Map<String, Object>) ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_EVENT_QUEUE, id);

		String event_queue_class = (String) config.get(PARAM_CLASS);

		if (!(loadEventQueueCount(id) > 1)) {
			IEventQueue event_queue = ReflectUtils.newObject(event_queue_class);

			if (event_queue != null) {
				LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(), LogUtils.TYPE_INIT,
						id,"loading");
				event_queue.init(config);
				putEventQueue(id, event_queue);
				LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(), LogUtils.TYPE_INIT,
						id,"success");
			}
		} else {
			;
		}

	}

	private static void loadEventQueueTemplate(Map<String, Object> event_queue_config)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String id = (String) event_queue_config.get(PARAM_ID);
		Map<String, Object> config = (Map<String, Object>) ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_EVENT_QUEUE_TEMPLATE, id);
		String eqt_class = (String) config.get(PARAM_CLASS);
		if (!(loadEventQueueTemplateCount(id) > 1)) {
			IEventQueueTemplate eqt = ReflectUtils.newObject(eqt_class);
			eqt.init(config);
			if (eqt != null) {
				LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(), LogUtils.TYPE_INIT,
						id,"loading");
				putEventQueueTemplate(id, eqt);
				LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(), LogUtils.TYPE_INIT,
						id,"success");
			}

		} else {
			;
		}
	}

	public static void start() {

		Set<Entry<String, IEventQueue>> set = m_event_queue_map.entrySet();

		for (Entry<String, IEventQueue> event_queue : set) {
			if (!setEventQueueStatus(event_queue.getKey(), true)) {
				LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(),
						LogUtils.TYPE_START, event_queue.getKey(), "starting");
				event_queue.getValue().start();
				LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(), LogUtils.TYPE_START,
						event_queue.getKey(), "successful");
			}
		}

		Set<Entry<String, IEventQueueTemplate>> template_set = m_event_queue_template_map.entrySet();

		for (Entry<String, IEventQueueTemplate> template : template_set) {
			if (!setEventQueueTemplateStatus(template.getKey(), true)) {
				LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(),
						LogUtils.TYPE_START, template.getKey(), "starting");
				template.getValue().start();
				LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(), LogUtils.TYPE_START,
						template.getKey(), "successful");
			}
		}

		// LogUtils.infoFormat("%s\t%s\t%s", EventUtils.class.getSimpleName(),
		// LogUtils.TYPE_START, "successful");
	}

	public static void start(Map<String, Object> config) {

		if (config == null) {
			return;
		}

		Object event_queue_configs = config.get(PARAM_EVENT_QUEUE);
		if (event_queue_configs != null) {

			if (event_queue_configs instanceof List) {
				List<Map<String, Object>> event_queue_config_list = (List<Map<String, Object>>) event_queue_configs;
				for (int i = 0; i < event_queue_config_list.size(); i++) {
					Map<String, Object> event_queue_config = event_queue_config_list.get(i);
					String id = (String) event_queue_config.get(PARAM_ID);

					if (!setEventQueueStatus(id, true)) {
						LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(),
								LogUtils.TYPE_START, id, "starting");
						m_event_queue_map.get(id).start();
						LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(), LogUtils.TYPE_START,
								id, "successful");
					}
				}
			} else {
				Map<String, Object> event_queue_config = (Map<String, Object>) event_queue_configs;
				String id = (String) event_queue_config.get(PARAM_ID);
				if (!setEventQueueStatus(id, true)) {
					LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(),
							LogUtils.TYPE_START, id, "starting");
					m_event_queue_map.get(id).start();
					LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(), LogUtils.TYPE_START, id,
							"successful");
				}
			}
		}

		Object event_queue_template_configs = config.get(PARAM_EVENT_QUEUE_TEMPLATE);
		if (event_queue_template_configs != null) {

			if (event_queue_template_configs instanceof List) {
				List<Map<String, Object>> event_queue_template_config_list = (List<Map<String, Object>>) event_queue_template_configs;
				for (int i = 0; i < event_queue_template_config_list.size(); i++) {
					Map<String, Object> event_queue_template_config = event_queue_template_config_list.get(i);
					String id = (String) event_queue_template_config.get(PARAM_ID);

					if (!setEventQueueTemplateStatus(id, true)) {
						LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(),
								LogUtils.TYPE_START, id, "starting");
						m_event_queue_template_map.get(id).start();
						LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(),
								LogUtils.TYPE_START, id, "successful");
					}
				}
			} else {
				Map<String, Object> event_queue_template_config = (Map<String, Object>) event_queue_template_configs;
				String id = (String) event_queue_template_config.get(PARAM_ID);
				if (!setEventQueueTemplateStatus(id, true)) {
					LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(),
							LogUtils.TYPE_START, id, "starting");
					m_event_queue_template_map.get(id).start();
					LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(),
							LogUtils.TYPE_START, id, "successful");
				}
			}
		}

	}

	public static void stop() {

		Set<Entry<String, IEventQueue>> set = m_event_queue_map.entrySet();

		for (Entry<String, IEventQueue> event_queue : set) {
			if (surplusLoadEventQueueCount(event_queue.getKey()) <= 0) {
				if (setEventQueueStatus(event_queue.getKey(), false)) {
					m_event_queue_map.remove(event_queue.getKey()).stop();
					LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(), LogUtils.TYPE_STOP,
							event_queue.getKey(), "successful");
				}
			}
		}

		Set<Entry<String, IEventQueueTemplate>> template_set = m_event_queue_template_map.entrySet();
		for (Entry<String, IEventQueueTemplate> template : template_set) {
			if (surplusLoadEventQueueCount(template.getKey()) <= 0) {
				if (setEventQueueTemplateStatus(template.getKey(), false)) {
					m_event_queue_map.remove(template.getKey()).stop();
					LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(), LogUtils.TYPE_STOP,
							template.getKey(), "successful");
				}
			}
		}

	}

	public static Event fromString(String string) {

		return JSON.parseObject(string, Event.class);
	}

	private synchronized static int loadEventQueueCount(String id) {
		AtomicInteger count = m_event_queue_load_count_map.get(id);
		if (count == null) {
			count = new AtomicInteger(1);
			m_event_queue_load_count_map.put(id, count);
			return 1;

		} else {
			return count.incrementAndGet();
		}
	}

	private synchronized static int loadEventQueueTemplateCount(String id) {
		AtomicInteger count = m_event_queue_template_load_count_map.get(id);
		if (count == null) {
			count = new AtomicInteger(1);
			m_event_queue_template_load_count_map.put(id, count);
			return 1;
		} else {
			return count.incrementAndGet();
		}
	}

	private synchronized static int surplusLoadEventQueueCount(String id) {
		
		AtomicInteger count = m_event_queue_load_count_map.get(id);
		if (count == null) {
			count = new AtomicInteger(0);
			m_event_queue_load_count_map.put(id, count);
			return 0;

		} else {
			return count.decrementAndGet();
		}
	}

	private synchronized static int surplusLoadEventQueueTemplateCount(String id) {
		AtomicInteger count = m_event_queue_template_load_count_map.get(id);
		if (count == null) {
			count = new AtomicInteger(0);
			m_event_queue_template_load_count_map.put(id, count);
			return 0;

		} else {
			return count.decrementAndGet();
		}
	}

	private synchronized static boolean setEventQueueStatus(String id, boolean status) {
		AtomicBoolean count = m_event_queue_status_map.get(id);
		if (count == null) {
			count = new AtomicBoolean(false);
			m_event_queue_status_map.put(id, count);

		}
		return count.getAndSet(status);

	}

	private synchronized static boolean setEventQueueTemplateStatus(String id, boolean status) {
		AtomicBoolean count = m_event_queue_template_status_map.get(id);
		if (count == null) {
			count = new AtomicBoolean(false);
			m_event_queue_template_status_map.put(id, count);

		}
		return count.getAndSet(status);

	}

	public static void stop(Map<String, Object> m_event_config) {
		if (m_event_config == null) {
			return;
		}

		Object event_queue_configs = m_event_config.get(PARAM_EVENT_QUEUE);
		if (event_queue_configs != null) {

			if (event_queue_configs instanceof List) {
				List<Map<String, Object>> event_queue_config_list = (List<Map<String, Object>>) event_queue_configs;
				for (int i = 0; i < event_queue_config_list.size(); i++) {
					Map<String, Object> event_queue_config = event_queue_config_list.get(i);
					String id = (String) event_queue_config.get(PARAM_ID);

					if (surplusLoadEventQueueCount(id) <= 0) {
						if (setEventQueueStatus(id, false)) {
							m_event_queue_map.remove(id).stop();
							LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(), LogUtils.TYPE_STOP,
									id, "successful");
						}
					}
				}
			} else {
				Map<String, Object> event_queue_config = (Map<String, Object>) event_queue_configs;
				String id = (String) event_queue_config.get(PARAM_ID);
				if (surplusLoadEventQueueCount(id) <= 0) {
					if (setEventQueueStatus(id, false)) {
						m_event_queue_map.remove(id).stop();
						LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueue.class.getSimpleName(), LogUtils.TYPE_STOP,
								id, "successful");
					}
				}
			}
		}

		Object event_queue_template_configs = m_event_config.get(PARAM_EVENT_QUEUE_TEMPLATE);
		if (event_queue_template_configs != null) {

			if (event_queue_template_configs instanceof List) {
				List<Map<String, Object>> event_queue_template_config_list = (List<Map<String, Object>>) event_queue_configs;
				for (int i = 0; i < event_queue_template_config_list.size(); i++) {
					Map<String, Object> event_queue_template_config = event_queue_template_config_list.get(i);
					String id = (String) event_queue_template_config.get(PARAM_ID);

					if (surplusLoadEventQueueTemplateCount(id) <= 0) {
						if (setEventQueueTemplateStatus(id, false)) {
							m_event_queue_template_map.remove(id).stop();
							LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(),
									LogUtils.TYPE_STOP, id, "successful");
						}
					}
				}
			} else {
				Map<String, Object> event_queue_template_config = (Map<String, Object>) event_queue_template_configs;
				String id = (String) event_queue_template_config.get(PARAM_ID);
				if (surplusLoadEventQueueTemplateCount(id) <= 0) {
					if (setEventQueueTemplateStatus(id, false)) {
						m_event_queue_template_map.remove(id).stop();
						LogUtils.infoFormat("%s\t%s\t%s\t%s", IEventQueueTemplate.class.getSimpleName(),
								LogUtils.TYPE_STOP, id, "successful");
					}
				}
			}
		}

	}
	
	public static  String getFieldString(String...fields){
		int len=fields.length;
		if(len==1){
			return fields[0];
		}else{
			StringBuffer sb=new StringBuffer(fields[0]);
			for(int i=1;i<len;i++){
				sb.append("_").append(fields[i]);
			}
			return sb.toString();
		}
		
	}
}
