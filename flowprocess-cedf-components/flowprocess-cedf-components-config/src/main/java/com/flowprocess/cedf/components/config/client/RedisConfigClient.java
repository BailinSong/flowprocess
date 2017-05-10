package com.flowprocess.cedf.components.config.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;

import com.flowprocess.cedf.config.IConfigClient;
import com.flowprocess.commons.JedisUtil;
import com.flowprocess.commons.XmlUtils;

import redis.clients.jedis.JedisCommands;


public class RedisConfigClient implements IConfigClient {

	public JedisUtil jedis;

	public String generateId() {
		String id = "ProcessNode_" + getHostIp(getInetAddress());

		JedisCommands jc = jedis.getJedis();
		long count;
		try {
			count = jc.incr(id);
			return String.format("%s_%04d", id, count);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			jedis.returnResource(jc);
		}

	}

	public static InetAddress getInetAddress() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
		}
		return null;
	}

	public static String getHostIp(InetAddress netAddress) {
		if (null == netAddress) {
			return null;
		}
		String ip = netAddress.getHostAddress(); // get the ip address
		return ip;
	}

	public void init(Map<String, Object> param) {
		jedis = new JedisUtil(param);

	}

	public String getConfigString(String type, String name) {
		JedisCommands jc = jedis.getJedis();
		String str = null;
		try {
			str = jc.hget(type, name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			jedis.returnResource(jc);
		}

		return str;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> stringToMap(String xml_string) {

		try {
			return XmlUtils.XMLString2MAP(xml_string);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <V> V getConfig(String type, String name) {
		try {
			return (V) stringToMap(getConfigString(type, name)).get(type);
		} catch (Exception e) {
			throw new RuntimeException(String.format("RedisConfigClient::getConfig(type,name):type=%s\t name=%s", type, name),
					e);
		}
	}

	@SuppressWarnings("unchecked")
	public <V> V getConfig(String type, String name, String format, Map<String, Object> param_map) {
		try {
			if (param_map == null) {
				return getConfig(type, name);
			} else {
				String string = getConfigString(type, name);
				for (Entry<String, Object> entry : param_map.entrySet()) {
					string = string.replaceAll(String.format(format, entry.getKey()), (String) entry.getValue());
				}
				return (V) ((Map<String, Object>) stringToMap(string)).get(type);
			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("RedisConfigClient::getConfig(type,name,format,param_map):type=%s\t name=%s", type, name),
					e);
		}
	}

}
