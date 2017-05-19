package com.blueline.commons;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Tuple;
public class RedisClusterItem {
	private static final String SPACE = ":";
	private static final String READ_ITEMS = "READ_ITEMS";
	final JedisUtil m_ju;
	final String m_item_name;
	final RingArray<SubItem> m_read_item_names = new RingArray<SubItem>();
	final RingArray<SubItem> m_write_item_names = new RingArray<SubItem>();
	Timer m_update_item_names = new Timer(true);
	final AtomicLong last_update_time = new AtomicLong();
	final long m_period;
	public RedisClusterItem(JedisUtil ju, String queue_name, long period) {
		this.m_ju = ju;
		this.m_item_name = queue_name;
		this.m_period = period;
		JedisCommands jc = ju.getJedis();
		try {
			long update_time = System.currentTimeMillis();
			Map<String, String> read_queue_data = jc.hgetAll(queue_name + SPACE + READ_ITEMS);
			List<SubItem> read_queues = null;
			if (read_queue_data != null && !read_queue_data.isEmpty()) {
				read_queues = new ArrayList<SubItem>();
				Set<Entry<String, String>> entrys = read_queue_data.entrySet();
				for (Entry<String, String> entry : entrys) {
					read_queues.add(new SubItem(entry.getKey(),update_time));
				}
			}
			if (read_queues != null && !read_queues.isEmpty()) {
				System.out.println("read item");
				m_read_item_names.fill(read_queues);
				if (jc.exists(m_item_name)) {
					m_read_item_names.add(new SubItem(m_item_name, update_time));
				}
			} else {
				m_read_item_names.add(new SubItem(m_item_name, update_time));
			}
			Map<String, int[]> slotinfo = m_ju.getClusterNodeSlotInfo();
			last_update_time.set(update_time);
			List<SubItem> write_queues = new ArrayList<SubItem>();
			Set<Entry<String, int[]>> set = slotinfo.entrySet();
			SubItem temp = null;
			for (Entry<String, int[]> e : set) {
				int max = e.getValue()[1];
				int min = e.getValue()[0];
				if (max == 0){
					continue;
				}
				temp = new SubItem(JedisUtil.getKeyOfSlot(m_item_name, max, max), update_time);
				jc.hset(m_item_name + SPACE + READ_ITEMS, temp.getItemName(), String.valueOf(temp.getTime()));
				write_queues.add(temp);
				m_read_item_names.add(temp);
			}
			System.out.println("write item");
			m_write_item_names.fill(write_queues);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			m_ju.returnResource(jc);
		}
		m_update_item_names.schedule(new TimerTask() {
			@Override
			public void run() {
				Map<String, int[]> slotinfo = m_ju.getClusterNodeSlotInfo();
				if (m_write_item_names.size() == slotinfo.size()) {
					return;
				}
				long update_time = System.currentTimeMillis();
				last_update_time.set(update_time);
				List<SubItem> sub_queue_list = new ArrayList<SubItem>();
				Set<Entry<String, int[]>> set = slotinfo.entrySet();
				SubItem temp = null;
				JedisCommands jc = null;
				try {
					jc = m_ju.getJedis();
					for (Entry<String, int[]> e : set) {
						int max = e.getValue()[1];
						int min = e.getValue()[0];
						if (max == 0){
							continue;
						}
						temp = new SubItem(JedisUtil.getKeyOfSlot(m_item_name, max, max), update_time);
						jc.hset(m_item_name + SPACE + READ_ITEMS, temp.getItemName(), String.valueOf(temp.getTime()));
						if (!sub_queue_list.contains(temp)) {
							sub_queue_list.add(temp);
						}
						m_read_item_names.add(temp);
					}
					m_write_item_names.fill(sub_queue_list);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} finally {
					m_ju.returnResource(jc);
				}
			}
		}, 0, this.m_period * 1000);
	}
	public void add(String... msg) {
		SubItem sub_queue = m_write_item_names.next();
		JedisCommands jc = m_ju.getJedis();
		try {
			jc.lpush(sub_queue.getItemName(), msg);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			m_ju.returnResource(jc);
		}
	}
	public long remove(long arge1, String arge2) {
		int count = 0;
		SubItem sub_item = null;
		long ret = 0;
		JedisCommands jc = m_ju.getJedis();
		try {
			do {
				if (sub_item != null && ((last_update_time.get() - sub_item.getTime()) > (m_period * 1000 * 5))
						&& !m_write_item_names.contains(sub_item)) {
					m_read_item_names.remove(sub_item);
					jc.hdel(m_item_name + SPACE + READ_ITEMS, sub_item.getItemName());
				}
				sub_item = m_read_item_names.next();
				ret = jc.lrem(sub_item.getItemName(), arge1, arge2);
				count++;
			} while (ret <= 0 && (count < m_read_item_names.size()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			m_ju.returnResource(jc);
		}
		return ret;
	}
	public String poll() {
		int count = 0;
		SubItem sub_item = null;
		String ret = null;
		JedisCommands jc = m_ju.getJedis();
		try {
			do {
				if (sub_item != null && ((last_update_time.get() - sub_item.getTime()) > (m_period * 1000 * 5))
						&& !m_write_item_names.contains(sub_item)) {
					m_read_item_names.remove(sub_item);
					jc.hdel(m_item_name + SPACE + READ_ITEMS, sub_item.getItemName());
				}
				sub_item = m_read_item_names.next();
				ret = jc.rpop(sub_item.getItemName());
				count++;
				if ((ret == null || ret.isEmpty()) && count >= m_read_item_names.size()) {
					break;
				}
			} while (ret == null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			m_ju.returnResource(jc);
		}
		return ret;
	}
	public String take(int timeout) {
		int count = 0;
		SubItem sub_item = null;
		String ret = null;
		JedisCommands jc = m_ju.getJedis();
		try {
			do {
				if (sub_item != null && ((last_update_time.get() - sub_item.getTime()) > (m_period * 1000 * 5))
						&& !m_write_item_names.contains(sub_item)) {
					m_read_item_names.remove(sub_item);
					jc.hdel(m_item_name + SPACE + READ_ITEMS, sub_item.getItemName());
				}
				sub_item = m_read_item_names.next();
				ret = jc.rpop(sub_item.getItemName());
				count++;
				if ((ret == null || ret.isEmpty()) && count >= m_read_item_names.size()) {
					count = 0;
					sub_item = m_read_item_names.next();
					List<String> msg = jc.brpop(timeout, sub_item.getItemName());
					if (msg == null || msg.isEmpty()) {
						ret = null;
					} else {
						ret = msg.get(1);
					}
					break;
				}
			} while (ret == null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			m_ju.returnResource(jc);
		}
		return ret;
	}
	public void zadd(long next_scheduletime, String key) {
		SubItem sub_queue = m_write_item_names.next();
		JedisCommands jc = m_ju.getJedis();
		try {
			jc.zadd(sub_queue.getItemName(), next_scheduletime, key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			m_ju.returnResource(jc);
		}
	}
	public long zrem(String value) {
		int count = 0;
		SubItem sub_item = null;
		long ret = 0;
		JedisCommands jc = m_ju.getJedis();
		try {
			do {
				if (sub_item != null && ((last_update_time.get() - sub_item.getTime()) > (m_period * 1000 * 5))
						&& !m_write_item_names.contains(sub_item)) {
					m_read_item_names.remove(sub_item);
					jc.hdel(m_item_name + SPACE + READ_ITEMS, sub_item.getItemName());
				}
				sub_item = m_read_item_names.next();
				ret = jc.zrem(sub_item.getItemName(), value);
				count++;
			} while (ret <= 0 && count < m_read_item_names.size());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			m_ju.returnResource(jc);
		}
		return ret;
	}
	public Set<Tuple> zrangeByScoreWithScores(long arge1, long arge2) {
		int count = 0;
		SubItem sub_item = null;
		Set<Tuple> ret = null;
		JedisCommands jc = m_ju.getJedis();
		Set<Tuple> temp = null;
		try {
			do {
				if (sub_item != null && ((last_update_time.get() - sub_item.getTime()) > (m_period * 1000 * 5))
						&& !m_write_item_names.contains(sub_item)) {
					m_read_item_names.remove(sub_item);
					jc.hdel(m_item_name + SPACE + READ_ITEMS, sub_item.getItemName());
				}
				sub_item = m_read_item_names.next();
				temp = jc.zrangeByScoreWithScores(sub_item.getItemName(), arge1, arge2);
				if (temp != null && !temp.isEmpty()) {
					if (ret == null) {
						ret = temp;
					} else {
						ret.addAll(temp);
					}
				}
				count++;
			} while (count < m_read_item_names.size());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			m_ju.returnResource(jc);
		}
		return ret;
	}
	public void stop() {
		m_update_item_names.cancel();
	}
}
