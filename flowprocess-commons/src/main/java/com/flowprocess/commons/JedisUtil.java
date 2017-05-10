package com.flowprocess.commons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.util.JedisClusterCRC16;

public class JedisUtil {
	private static final String PARAM_SERVER = "Server";
	private static final String PARAM_PASSWORD = "Password";
	private static final String PARAM_PORT = "Port";
	private static final String PARAM_IP = "Ip";
	private String m_ip = null;
	private int m_port = -1;
	private String m_password = null;
	private List<Map<String, String>> m_cluster_congfig = null;

	private JedisPool m_jedis_pool = null;
	private JedisCluster m_jedis_cluster = null;
	private int m_index = 0;

	public static void main(String[] args) throws DOMException, InstantiationException, IllegalAccessException,
			ParserConfigurationException, SAXException, IOException {
		Map<String, Object> config = XmlUtils.XMLString2MAP(
				"<Storage type=\"redis\"><Server><Ip>127.0.0.1</Ip>	<Port>7000</Port></Server><Server><Ip>192.168.71.71</Ip>	<Port>7000</Port></Server></Storage>");
		JedisUtil ju = new JedisUtil((Map<String, Object>) config.get("Storage"));
		// JedisCommands jc=ju.getJedis();
		// System.out.println(jc.llen("MessageWaitingReport_1474609863000"));
		// ju.returnResource(jc);
		System.out.println(ju.grab("grab-test", 50));
		// ju.release("grab-test");

	}

	private static boolean isCluster(String m_ip, int m_port, String m_password) {
		if (m_password == null || m_password.trim().isEmpty()) {
			Jedis ju = new Jedis(m_ip, m_port);
			String server_info = ju.info("Server");
			ju.close();

			String flag = "redis_mode:";
			int begin_index = server_info.indexOf(flag) + flag.length();
			int end_index = server_info.indexOf('\n', begin_index);
			// System.out.println(end_index + ":" + begin_index);
			String mode = server_info.substring(begin_index, end_index);
			// System.out.println(mode);
			if (mode.trim().equalsIgnoreCase("cluster")) {
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}
	}

	public JedisUtil(Map<String, Object> params) {

		super();

		load(params);

	}

	@SuppressWarnings("unchecked")
	private void load(Map<String, Object> params) {

		Object server = params.get(PARAM_SERVER);

		if (server != null) {
			if (server instanceof Map) {
				params = (Map<String, Object>) server;
			} else if (server instanceof List) {
				load((List<Map<String, Object>>) server);
				return;
			}

		}

		this.m_ip = (String) params.get(PARAM_IP);
		this.m_port = Integer.valueOf((String) params.get(PARAM_PORT));
		this.m_password = (String) params.get(PARAM_PASSWORD);

		if (isCluster(m_ip, m_port, m_password)) {

			List<Map<String, String>> cluster_congfig = getNodeConfig(m_ip, m_port);

			init(cluster_congfig);

		} else {
			init(m_ip, m_port, m_password, 0);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void load(List params) {

		List<Map<String, Object>> redis_config_list = (List<Map<String, Object>>) params;

		Exception temp = null;

		for (Map<String, Object> config : redis_config_list) {
			try {
				load(config);
			} catch (Exception e) {
				temp = e;
				continue;
			}
			return;
		}

		throw new RuntimeException("lost all redis server.", temp);

	}

	public JedisUtil() {

		super();

		this.m_ip = "127.0.0.1";
		this.m_port = 6379;

		if (isCluster(m_ip, m_port, null)) {

			List<Map<String, String>> cluster_congfig = getNodeConfig(m_ip, m_port);

			init(cluster_congfig);

		} else {
			init(m_ip, m_port, null, 0);
		}

	}

	public JedisUtil(String m_ip) {

		super();

		this.m_ip = m_ip;
		this.m_port = 6379;

		if (isCluster(m_ip, m_port, null)) {

			List<Map<String, String>> cluster_congfig = getNodeConfig(m_ip, m_port);

			init(cluster_congfig);

		} else {
			init(m_ip, m_port, null, 0);
		}

	}

	public JedisUtil(String m_ip, int m_port) {

		super();

		this.m_ip = m_ip;
		this.m_port = m_port;

		if (isCluster(m_ip, m_port, null)) {

			List<Map<String, String>> cluster_congfig = getNodeConfig(m_ip, m_port);

			init(cluster_congfig);

		} else {
			init(m_ip, m_port, null, 0);
		}

	}

	public JedisUtil(String m_ip, int m_port, String m_password) {

		super();

		this.m_ip = m_ip;
		this.m_port = m_port;
		this.m_password = m_password;

		if (isCluster(m_ip, m_port, m_password)) {

			List<Map<String, String>> cluster_congfig = getNodeConfig(m_ip, m_port);

			init(cluster_congfig);

		} else {
			init(m_ip, m_port, m_password, 0);
		}

	}

	public JedisUtil(String m_ip, int m_port, String m_password, int m_index) {

		super();

		this.m_ip = m_ip;
		this.m_port = m_port;
		this.m_password = m_password;

		if (isCluster(m_ip, m_port, m_password)) {

			List<Map<String, String>> cluster_congfig = getNodeConfig(m_ip, m_port);

			init(cluster_congfig);

		} else {
			init(m_ip, m_port, m_password, m_index);
		}

	}

	private List<Map<String, String>> getNodeConfig(String m_ip, int m_port) {
		Jedis jedis = new Jedis(m_ip, m_port);

		String nodes_string = jedis.clusterNodes();

		String[] nodes = nodes_string.split("\n");

		jedis.close();
		// System.out.println(Arrays.toString(nodes));

		List<Map<String, String>> cluster_congfig = new ArrayList<Map<String, String>>();

		for (String node : nodes) {
			String[] ip_port = node.split(" ")[1].split(":");
			String ip = ip_port[0];
			String port = ip_port[1];

			Map<String, String> host = new HashMap<String, String>();
			host.put(PARAM_IP, ip);
			host.put(PARAM_PORT, port);

			cluster_congfig.add(host);
		}
		return cluster_congfig;
	}

	public void init(String m_ip, int m_port, String m_password, int m_index) {
		this.m_ip = m_ip;
		this.m_port = m_port;
		this.m_password = m_password;
		this.m_index = m_index;

		JedisPoolConfig config = new JedisPoolConfig();
		config.setMinIdle(5);
		config.setMaxIdle(10);
		config.setMaxTotal(100);
		config.setBlockWhenExhausted(true); // ���ӳ���������ʱ�����ȴ�
		config.setMaxWaitMillis(-1);
		//config.setTestOnBorrow(true);
		config.setTestOnCreate(true);
		// config.setTestOnReturn(true);
		config.setTestWhileIdle(true);

		try {
			// ��ʼ��jedis_pool
			if (m_password == null || m_password.isEmpty()) {
				m_jedis_pool = new JedisPool(config, m_ip, m_port, 3000);
			} else {
				m_jedis_pool = new JedisPool(config, m_ip, m_port, 3000, m_password);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void init(List<Map<String, String>> m_cluster_congfig) {
		this.m_cluster_congfig = m_cluster_congfig;

		JedisPoolConfig config = new JedisPoolConfig();
		config.setMinIdle(5);
		config.setMaxIdle(10);
		config.setMaxTotal(100);
		config.setBlockWhenExhausted(true); // ���ӳ���������ʱ�����ȴ�
		config.setMaxWaitMillis(-1);
		//config.setTestOnBorrow(true);
		config.setTestOnCreate(true);
		// config.setTestOnReturn(true);
		config.setTestWhileIdle(true);

		try {
			Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
			for (Map<String, String> redis_cluster_config : m_cluster_congfig) {
				jedisClusterNodes.add(new HostAndPort(redis_cluster_config.get(PARAM_IP),
						Integer.valueOf(redis_cluster_config.get(PARAM_PORT))));
			}
			m_jedis_cluster = new JedisCluster(jedisClusterNodes, config);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * ��ȡredis����
	 * 
	 * @return JedisCommands or Null
	 */
	public JedisCommands getJedis() {
		if (m_jedis_pool != null) {
			Jedis jedis = m_jedis_pool.getResource();
			if (m_index != 0) {
				jedis.select(m_index);
			}
			return jedis;
		} else if (m_jedis_cluster != null) {
			return m_jedis_cluster;
		}
		return null;
	}

	/**
	 * redis��Դ����
	 * 
	 * @param JedisCommands
	 */
	public void returnResource(JedisCommands jedis) {
		if (jedis != null) {
			try {
				if (jedis instanceof Jedis) {
					((Jedis) jedis).close();
				} else if (jedis instanceof JedisCluster) {
					// nothing to do.
				}
			} catch (Exception e) {
			}
		}
	}

	final Map<String, JedisPubSub> pub_sub_map = new ConcurrentHashMap<String, JedisPubSub>();

	public void sub(JedisPubSub pub_sub, final String channel) {

		JedisPubSub old = pub_sub_map.put(channel, pub_sub);
		if (old != null) {
			old.unsubscribe(channel);
		}
		Thread thread = new Thread(new Runnable() {

			public void run() {
				JedisPubSub sub = pub_sub_map.get(channel);
				while ((sub = pub_sub_map.get(channel)) != null) {
					Jedis jedis = null;
					try {
						if (m_jedis_pool != null) {
							jedis = m_jedis_pool.getResource();
							jedis.subscribe(sub, channel);

						} else if (m_jedis_cluster != null) {
							m_jedis_cluster.subscribe(sub, channel);
						}
					} catch (Exception e) {
						e.printStackTrace();
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} finally {
						if (jedis != null) {
							jedis.close();
						}
					}
				}
			}
		});
		thread.setName("RedisSub[" + channel + "]");
		thread.setDaemon(true);
		thread.start();

	}

	public void unSub(String channel) {

		JedisPubSub old = pub_sub_map.remove(channel);
		if (old != null) {
			old.unsubscribe(channel);
		}
	}

	public void pub(String message, String channel) {
		Jedis jedis = null;
		try {
			if (m_jedis_pool != null) {
				jedis = m_jedis_pool.getResource();
				jedis.publish(channel, message);
			} else if (m_jedis_cluster != null) {
				m_jedis_cluster.publish(channel, message);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	public String getIp() {
		return m_ip;
	}

	public void setIp(String m_ip) {
		this.m_ip = m_ip;
	}

	public int getPort() {
		return m_port;
	}

	public void setPort(int m_port) {
		this.m_port = m_port;
	}

	public String getPassword() {
		return m_password;
	}

	public void setPassword(String m_password) {
		this.m_password = m_password;
	}

	public List<Map<String, String>> getClusterCongfig() {
		return m_cluster_congfig;
	}

	public void setClusterCongfig(List<Map<String, String>> m_cluster_congfig) {
		this.m_cluster_congfig = m_cluster_congfig;
	}

	Jedis time_jedis;

	public long getTime() {
		return getMicroTime() / 1000;
	}

	public synchronized long getMicroTime() {

		List<String> time = null;
		for (int i = 0; i < 3; i++) {

			while (time_jedis == null) {
				if (m_jedis_pool != null) {
					try {
						Jedis jedis = m_jedis_pool.getResource();
						time_jedis = jedis;
						time = time_jedis.time();
					} catch (Exception e) {
						if (time_jedis != null) {
							time_jedis.close();
							time_jedis = null;
						}
					}
				} else if (m_jedis_cluster != null) {
					Collection<JedisPool> nodes = m_jedis_cluster.getClusterNodes().values();
					for (JedisPool jedispool : nodes) {
						try {
							time_jedis = jedispool.getResource();
							time = time_jedis.time();
							break;
						} catch (Exception e) {
							if (time_jedis != null) {
								time_jedis.close();
								time_jedis = null;
							}
						}
					}

				}
			}

			if (time_jedis != null) {
				if (time != null && !time.isEmpty()) {
					return toMicroTime(time);
				} else {
					try {
						return toMicroTime(time_jedis.time());

					} catch (Exception e) {
						if (time_jedis != null) {
							time_jedis.close();
							time_jedis = null;
						}
					}
				}
			}
		}
		return System.currentTimeMillis() * 1000;

	}

	Map<String, Long> grab_map = new ConcurrentHashMap<String, Long>();

	public boolean grab(String key, long time_out) {
		JedisCommands cmd = getJedis();
		try {
			// long expireAt = (getTime() + time_out * 1000) / 1000;
			long expireAt = System.currentTimeMillis() + (time_out * 1000);
			long count = cmd.setnx(key, String.valueOf(expireAt));
			if (count > 0) {
				grab_map.put(key, expireAt);
				cmd.expire(key, (int) time_out);
				return true;
			} else {
				return false;

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			returnResource(cmd);
		}
	}

	public boolean release(String key) {

		Long expire_time = grab_map.remove(key);
		if (expire_time == null) {
			return false;
		}
		if (expire_time > System.currentTimeMillis()) {
			JedisCommands cmd = getJedis();
			try {
				cmd.del(key);

			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				returnResource(cmd);
			}
		}

		return true;
	}

	private long toMicroTime(List<String> time) {
		return (Long.parseLong(time.get(0)) * 1000000) + (Long.parseLong(time.get(1)));
		// StringBuffer sb=new StringBuffer(time.get(0));
		// sb.append(time.get(1).substring(3));
		// return Long.parseLong(sb.toString());
	}

	public Map<String, int[]> getClusterNodeSlotInfo() {

		if (m_jedis_cluster != null) {

			Map<String, JedisPool> cluster_nodes = m_jedis_cluster.getClusterNodes();
			Collection<JedisPool> pools = cluster_nodes.values();
			Iterator<JedisPool> it = pools.iterator();
			String nodes_str = null;
			Jedis j = null;
			JedisPool jp = null;
			while (it.hasNext()) {

				try {
					jp = it.next();
					j = jp.getResource();
					nodes_str = j.clusterNodes();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (j != null) {
						j.close();
					}
				}
				if (nodes_str != null && !nodes_str.isEmpty()) {
					break;
				}
			}
			String[] nodes = nodes_str.split("\n");
			Map<String, int[]> nodes_info = new HashMap<String, int[]>();
			String[] info = null;
			int[] slot;
			String[] slot_str;
			for (String node : nodes) {
				info = node.split(" ");
				if (info[2].indexOf("master") > -1) {

					if (info.length >= 9 && (!info[8].trim().isEmpty()) && info[8].indexOf("-")>-1) {
						
						slot_str = info[8].split("-");

						slot = new int[2];

						slot[0] = Integer.parseInt(slot_str[0]);
						slot[1] = Integer.parseInt(slot_str[1]);
					} else {
						slot = new int[]{0,0};
					}
					nodes_info.put(info[1], slot);

				}
			}
			return nodes_info;

		} else {
			Map<String, int[]> nodes_info = new HashMap<String, int[]>();
			nodes_info.put(m_ip + ":" + m_port, new int[] { 0, 16384 });
			return nodes_info;
		}

	}

	public final static String getKeyOfSlot(String key, int min, int max) {
		int crc = -1;
		String data_temp = key;
		int num = 0;
		for (; !((crc >= min) && (crc <= max));) {
			data_temp = key + ":" + (num++);
			crc = JedisClusterCRC16.getSlot(data_temp);
		}
		return data_temp;
	}

	public final static int GetKeyHash(String key) {
		return JedisClusterCRC16.getSlot(key);
	}

	public void close() {
		if (m_jedis_pool != null) {
			m_jedis_pool.close();
			m_jedis_pool = null;
		}
		if (m_jedis_cluster != null) {
			try {
				m_jedis_cluster.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			m_jedis_cluster = null;
		}
	}

}
