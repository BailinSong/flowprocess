package com.blueline.flowprocess.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.blueline.commons.ReflectUtils;
import com.blueline.flowprocess.core.config.ConfigUtils;
import com.blueline.flowprocess.core.log.LogUtils;

@SuppressWarnings("unchecked")
public class ServiceUtils {
	private static final String INSTANCE = "Instance";

	private ServiceUtils() {
	}

	private static final String PARAM_SERVICE = "Service";
	private static final String PARAM_ID = "id";
	private static final String PARAM_CLASS = "class";

	private static Map<String, IService> m_service_map = new ConcurrentHashMap<String, IService>();
	private static Map<String, AtomicInteger> m_service_load_count_map = new ConcurrentHashMap<String, AtomicInteger>();
	private static Map<String, AtomicBoolean> m_service_status_map = new ConcurrentHashMap<String, AtomicBoolean>();

	private static IService putService(String key, IService value) {
		return m_service_map.put(key, value);
	}

	public static <V> V getService(String key) {

		Object service = m_service_map.get(key);
		if (null == service) {
			throw new RuntimeException(key + " not found");
		} else {
			return (V) service;
		}
	}

	// private static IService removeService(String key)
	// {
	// return m_service_map.remove(key);
	// }

	public static void init(Map<String, Object> config, Map<String, Object> params)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (config == null) {
			return;
		}

		Object service_configs = config.get(PARAM_SERVICE);
		if (service_configs == null) {
			return;
		}

		if (service_configs instanceof List) {
			List<Map<String, Object>> service_config_list = (List<Map<String, Object>>) service_configs;
			for (int i = 0; i < service_config_list.size(); i++) {
				Map<String, Object> service_config = service_config_list.get(i);
				String instance_id = loadService((Map<String, Object>) ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_SERVICE,
						(String) service_config.get(PARAM_ID)));
				List<String> instance_list = (List<String>) service_config.get(INSTANCE);
				if (instance_list == null) {
					instance_list = new ArrayList<String>();
					service_config.put(INSTANCE, instance_list);
				}
				if (!instance_list.contains(instance_id)) {
					instance_list.add(instance_id);
				}
			}
		} else {
			Map<String, Object> service_config = (Map<String, Object>) service_configs;
			String instance_id = loadService(
					(Map<String, Object>) ConfigUtils.getConfig(ConfigUtils.CONFIG_TYPE_SERVICE, (String) service_config.get(PARAM_ID)));
			List<String> instance_list = (List<String>) service_config.get(INSTANCE);
			if (instance_list == null) {
				instance_list = new ArrayList<String>();
				service_config.put(INSTANCE, instance_list);
			}
			if (!instance_list.contains(instance_id)) {
				instance_list.add(instance_id);
			}
		}

		LogUtils.debugFormat("%s\t%s\t%s", ServiceUtils.class.getSimpleName(), LogUtils.TYPE_INIT, "successful");
	}

	private static String loadService(Map<String, Object> service_config)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		String id = (String) service_config.get(PARAM_ID);

		Map<String, Object> config = service_config;// (Map<String,Object>)ConfigUtils.getConfig("Service",id);

		String service_class = (String) config.get(PARAM_CLASS);

		if (!(loadServiceCount(id) > 1)) {

			IService service = ReflectUtils.newObject(service_class);

			if (service != null) {
				service.init(config);
				putService(id, service);

				LogUtils.infoFormat("%s\t%s\t%s", ServiceUtils.class.getSimpleName(), LogUtils.TYPE_INIT, id);

			}

		} else {
			;
		}
		return id;

	}

	public static void start() {
		Set<Entry<String, IService>> set = m_service_map.entrySet();

		for (Entry<String, IService> service : set) {
			if (!setServiceStatus(service.getKey(), true)) {
				service.getValue().start();
				LogUtils.infoFormat("%s\t%s\t%s", ServiceUtils.class.getSimpleName(), LogUtils.TYPE_START,
						service.getKey());
			}
		}

		LogUtils.debugFormat("%s\t%s\t%s", ServiceUtils.class.getSimpleName(), LogUtils.TYPE_START, "successful");
	}

	public static void stop() {

		Set<Entry<String, IService>> set = m_service_map.entrySet();

		for (Entry<String, IService> service : set) {
			if (surplusLoadserviceCount(service.getKey()) <= 0) {
				if (setServiceStatus(service.getKey(), false)) {
					m_service_map.remove(service.getKey()).stop();
					LogUtils.infoFormat("%s\t%s\t%s", ServiceUtils.class.getSimpleName(), LogUtils.TYPE_STOP,
							service.getKey());
				}
			}
		}

		LogUtils.debugFormat("%s\t%s\t%s", ServiceUtils.class.getSimpleName(), LogUtils.TYPE_STOP, "successful");
	}

	private synchronized static int loadServiceCount(String id) {
		AtomicInteger count = m_service_load_count_map.get(id);
		if (count == null) {
			count = new AtomicInteger(1);
			m_service_load_count_map.put(id, count);
			return 1;

		} else {
			return count.incrementAndGet();
		}
	}

	private synchronized static int surplusLoadserviceCount(String id) {
		AtomicInteger count = m_service_load_count_map.get(id);
		if (count == null) {
			count = new AtomicInteger(0);
			m_service_load_count_map.put(id, count);
			return 0;

		} else {
			return count.decrementAndGet();
		}
	}

	private synchronized static boolean setServiceStatus(String id, boolean status) {
		AtomicBoolean count = m_service_status_map.get(id);
		if (count == null) {
			count = new AtomicBoolean(false);
			m_service_status_map.put(id, count);

		}
		return count.getAndSet(status);

	}

	public static void start(Map<String, Object> m_service_config) {
		if (m_service_config == null) {
			return;
		}

		Object service_configs = m_service_config.get(PARAM_SERVICE);
		if (service_configs == null) {
			return;
		}

		if (service_configs instanceof List) {
			List<Map<String, Object>> service_config_list = (List<Map<String, Object>>) service_configs;
			for (int i = 0; i < service_config_list.size(); i++) {
				Map<String, Object> service_config = service_config_list.get(i);

				List<String> instance_list = (List<String>) service_config.get(INSTANCE);
				if (instance_list != null && !instance_list.isEmpty()) {
					for (String instance_id : instance_list) {
						if (!setServiceStatus(instance_id, true)) {
							LogUtils.infoFormat("%s\t%s\t%s\t%s", ServiceUtils.class.getSimpleName(),
									LogUtils.TYPE_START, instance_id, "starting");
							m_service_map.get(instance_id).start();
							LogUtils.infoFormat("%s\t%s\t%s\t%s", ServiceUtils.class.getSimpleName(),
									LogUtils.TYPE_START, instance_id, "success");
						}
					}
				}

			}
		} else {
			Map<String, Object> service_config = (Map<String, Object>) service_configs;
			List<String> instance_list = (List<String>) service_config.get(INSTANCE);
			if (instance_list != null && !instance_list.isEmpty()) {
				for (String instance_id : instance_list) {
					if (!setServiceStatus(instance_id, true)) {
						LogUtils.infoFormat("%s\t%s\t%s\t%s", ServiceUtils.class.getSimpleName(), LogUtils.TYPE_START,
								instance_id, "starting");
						m_service_map.get(instance_id).start();
						LogUtils.infoFormat("%s\t%s\t%s\t%s", ServiceUtils.class.getSimpleName(), LogUtils.TYPE_START,
								instance_id, "success");
					}
				}
			}
		}

	}

	public static void stop(Map<String, Object> m_service_config) {
		if (m_service_config == null) {
			return;
		}

		Object service_configs = m_service_config.get(PARAM_SERVICE);
		if (service_configs == null) {
			return;
		}

		if (service_configs instanceof List) {
			List<Map<String, Object>> service_config_list = (List<Map<String, Object>>) service_configs;
			for (int i = 0; i < service_config_list.size(); i++) {
				Map<String, Object> service_config = service_config_list.get(i);

				List<String> instance_list = (List<String>) service_config.get(INSTANCE);
				if (instance_list != null && !instance_list.isEmpty()) {
					for (String instance_id : instance_list) {
						if (surplusLoadserviceCount(instance_id) <= 0) {

							if (setServiceStatus(instance_id, false)) {
								LogUtils.infoFormat("%s\t%s\t%s\t%s", ServiceUtils.class.getSimpleName(),
										LogUtils.TYPE_STOP, instance_id, "stopping");
								m_service_map.get(instance_id).stop();
								LogUtils.infoFormat("%s\t%s\t%s\t%s", ServiceUtils.class.getSimpleName(),
										LogUtils.TYPE_STOP, instance_id, "success");
							}
						}
					}
				}
			}
		} else {
			Map<String, Object> service_config = (Map<String, Object>) service_configs;
			List<String> instance_list = (List<String>) service_config.get(INSTANCE);
			if (instance_list != null && !instance_list.isEmpty()) {
				for (String instance_id : instance_list) {
					if (surplusLoadserviceCount(instance_id) <= 0) {

						if (setServiceStatus(instance_id, false)) {
							LogUtils.infoFormat("%s\t%s\t%s\t%s", ServiceUtils.class.getSimpleName(),
									LogUtils.TYPE_STOP, instance_id, "stopping");
							m_service_map.get(instance_id).stop();
							LogUtils.infoFormat("%s\t%s\t%s\t%s", ServiceUtils.class.getSimpleName(),
									LogUtils.TYPE_STOP, instance_id, "success");
						}
					}
				}
			}
//			String id = (String) service_config.get(PARAM_ID);
//			if (surplusLoadserviceCount(id) <= 0) {
//				if (setServiceStatus(id, false)) {
//					m_service_map.remove(id).stop();
//					LogUtils.infoFormat("%s\t%s\t%s", ServiceUtils.class.getSimpleName(), LogUtils.TYPE_STOP, id);
//				}
//			}
		}

	}
}
