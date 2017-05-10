package com.flowprocess.cedf.components.event.queue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.flowprocess.cedf.event.Event;
import com.flowprocess.cedf.event.EventUtils;
import com.flowprocess.cedf.event.IEventQueue;
import com.flowprocess.cedf.log.LogUtils;

public class FileCacheEventQueue implements IEventQueue
{
	private static final String PARAM_ID = "id";
	private static final String PARAM_BASE_PATH = "basepath";
	private static final String PARAM_CAPACITY = "capacity";
//	private static final String PARAM_INTERVAL = "interval";
	
	private static final String PARAM_WORKTIME = "worktime";
	private static final String PARAM_BEGIN = "begin";
	private static final String PARAM_END = "end";
	
	private static final long MILLISSECOND_OF_DAY = 86400000L;
	private static final long MILLISSECOND_ADJUST_TIME_ZONE = 28800000L;

	private String m_id;
	private String m_base_path;
	private int m_capacity = 100;
//	private int m_interval = 1000 * 60;
	
	private long m_worktime_begin = 0;
	private long m_worktime_end = MILLISSECOND_OF_DAY;

	private BlockingQueue<Event> m_realtime_queue;
	private BlockingQueue<Event> m_history_queue;
	private BlockingQueue<Event> m_empty_queue;

	private String getId()
	{
		return m_id;
	}

	private void setId(String value)
	{
		m_id = value;
	}

	private String getBasePath()
	{
		return m_base_path;
	}

	private void setBasePath(String value)
	{
		m_base_path = value;
	}

	private int getCapacity()
	{
		return m_capacity;
	}

	private void setCapacity(int value)
	{
		m_capacity = value;
	}
//
//	private int getInterval()
//	{
//		return m_interval;
//	}
//
//	private void setInterval(int value)
//	{
//		m_interval = value;
//	}

	private long getWorkTimeBegin()
	{
		return m_worktime_begin;
	}
	
	private void setWorkTimeBegin(long value)
	{
		m_worktime_begin = value;
	}

	private long getWorkTimeEnd()
	{
		return m_worktime_end;
	}
	
	private void setWorkTimeEnd(long value)
	{
		m_worktime_end = value;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(Map<String, Object> config)
	{
		String id = (String) config.get(PARAM_ID);
		setId(id);
		String base_path = (String) config.get(PARAM_BASE_PATH);
		setBasePath(base_path);
		String capacity = (String) config.get(PARAM_CAPACITY);
		setCapacity(Integer.parseInt(capacity));
//		String interval = (String) config.get(PARAM_INTERVAL);
//		setInterval(Integer.parseInt(interval));
		
		Map<String, Object> worktime_config = (Map<String, Object>) config.get(PARAM_WORKTIME);
		if (worktime_config != null)
		{
			String begin = (String) worktime_config.get(PARAM_BEGIN);
			String end = (String) worktime_config.get(PARAM_END);
			setWorkTimeBegin(Long.parseLong(begin));
			setWorkTimeEnd(Long.parseLong(end));
		}
		
		m_realtime_queue = new LinkedBlockingQueue<Event>(getCapacity());
		m_history_queue = new LinkedBlockingQueue<Event>();
		m_empty_queue = new LinkedBlockingQueue<Event>();
		
		loadCacheFiles();
	}

	@Override
	public void start()
	{
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), getId(), LogUtils.TYPE_START, LogUtils.CONTENT_SUCCESSFUL);
	}

	@Override
	public void stop()
	{
		if (!m_realtime_queue.isEmpty())
		{
			flushRealtimeQueue();
		}
		if (!m_history_queue.isEmpty())
		{
			flushHistoryQueue();
		}
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), getId(), LogUtils.TYPE_STOP, LogUtils.CONTENT_SUCCESSFUL);
	}

	@Override
	public synchronized void add(Event event)
//	public void add(Event event)
	{
		LogUtils.debugFormat("FileCacheEventQueue::add:{}:{}",getId(), event);
		boolean full = false;
		try
		{
			m_realtime_queue.add(event);
			LogUtils.debugFormat("FileCacheEventQueue::add:{}:not full {}",getId(),LogUtils.CONTENT_SUCCESSFUL);
		}
		catch (IllegalStateException e)
		{
			LogUtils.debugFormat("FileCacheEventQueue::add:{}:is full",getId());
			
			full = true;
		}
		if (full)
		{
			
			flushRealtimeQueue();
			LogUtils.debugFormat("FileCacheEventQueue::add:{}:flushRealtimeQueue",getId(),LogUtils.CONTENT_SUCCESSFUL);
			m_realtime_queue.add(event);
			LogUtils.debugFormat("FileCacheEventQueue::add:{}:queue add {}",getId(),LogUtils.CONTENT_SUCCESSFUL);
		}
		//System.out.println("add...");
		printQueues();
	}

	@Override
	public Event take(int timeout) throws InterruptedException
//	public Event take(int timeout) throws InterruptedException
	{
		//TODO interval check
		
		//worktime check
		long today_millis = (System.currentTimeMillis() + MILLISSECOND_ADJUST_TIME_ZONE) % MILLISSECOND_OF_DAY;
//		System.out.println(today_millis);
		if ((today_millis < getWorkTimeBegin()) || (today_millis > getWorkTimeEnd()))
		{
			return m_empty_queue.poll(timeout, TimeUnit.SECONDS);
		}
		
		Event event = m_history_queue.poll();
		
		
			
		
			if (event == null)
			{
				boolean loaded = load();
				//System.out.println("take loaded:" + loaded);
				if (!loaded)
				{
					//System.out.println("no files");
					//System.out.println("take...");
					printQueues();
//					return m_realtime_queue.poll(timeout, TimeUnit.SECONDS);
					event = m_realtime_queue.poll();
					if (event == null)
					{
						return m_empty_queue.poll(timeout, TimeUnit.SECONDS);
					}
					else
					{
						return event;
					}
				}
				event = m_history_queue.poll();
			}
		

		//System.out.println("history");
		//System.out.println("take...");
		printQueues();
		return event;
	}

	@Override
	public Event poll()
//	public Event poll()
	{
		//worktime check
		long today_millis = System.currentTimeMillis() % MILLISSECOND_OF_DAY;
		if ((today_millis < getWorkTimeBegin()) || (today_millis > getWorkTimeEnd()))
		{
			return null;
		}
		
		Event event = m_history_queue.poll();
		if (event == null)
		{
			boolean loaded = load();
			//System.out.println("poll loaded:" + loaded);
			if (!loaded)
			{
				//System.out.println("no files");
				//System.out.println("poll...");
				printQueues();
				return m_realtime_queue.poll();
			}
			event = m_history_queue.poll();
		}

		//System.out.println("history");
		//System.out.println("poll...");
		printQueues();
		return event;
	}

	private static final String m_extension = ".cache";
	
	private BlockingQueue<String> m_filename_queue = new LinkedBlockingQueue<String>();
	private String m_history_file_name;

	private void loadCacheFiles()
	{
		File path = new File(getBasePath());
		if (!path.exists())
		{
			path.mkdirs();
		}
		FilenameFilter filter = new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				if (name.lastIndexOf(m_extension) > 0)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		};
		String[] filenames = path.list(filter);
		if (filenames == null)
		{
			return;
		}
		int count = filenames.length;
		if (count == 0)
		{
			return;
		}
		if (count > 1)
		for (int i = 0; i < count - 1; i ++)
		{
			for (int j = i+1; j < count; j ++)
			{
				if (filenames[i].compareTo(filenames[j]) > 0)
				{
					String temp = filenames[i];
					filenames[i] = filenames[j];
					filenames[j] = temp;
				}
			}
		}
		for (int i = 0; i < count; i ++)
		{
			m_filename_queue.add(fullPath(filenames[i]));
		}
		//System.out.println("loadCacheFiles " + count);
		//System.out.println(Arrays.toString(m_filename_queue.toArray(new String[0])));
		//System.out.println("loadCacheFiles...");
		printQueues();
	}
	
	private String fullPath(String filename)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getBasePath());
		sb.append(File.separatorChar);
		sb.append(filename);
//		System.out.println(sb.toString());
		return sb.toString();
	}

	private AtomicLong m_counter = new AtomicLong(0);
	
	private String nextFileName()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getBasePath());
		sb.append(File.separatorChar);
		sb.append(System.currentTimeMillis());
		sb.append(String.format("%04d", m_counter.incrementAndGet() % 1000));
		sb.append(m_extension);
//		System.out.println(sb.toString());
		return sb.toString();
	}

	private void flushRealtimeQueue()
	{
		//System.out.println("flush");
		//System.out.println(Arrays.toString(m_realtime_queue.toArray(new Event[0])));
		String file_name = nextFileName();
		//System.out.println(file_name);
		
		
		writeToFile(file_name, m_realtime_queue);
		m_filename_queue.add(file_name);
//		m_realtime_queue.clear();
	}
	
	private void flushHistoryQueue()
	{
		writeToFile(m_history_file_name, m_history_queue);
	}

	private void writeToFile(String file_name, BlockingQueue<Event> queue)
	{
		File f = new File(file_name);
		try
		{
			f.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			Event event = m_realtime_queue.poll();
			while (event != null)
			{
				String str = event.toString();
				writer.write(str);
				writer.write("\n");

				event = m_realtime_queue.poll();
			}
//			Event[] events = queue.toArray(new Event[0]);
//			for (int i = 0; i < events.length; i ++)
//			{
//				String str = events[i].toString();
//				writer.write(str);
//				writer.write("\n");
//			}
			writer.flush();
			writer.close();
			//System.out.println("flush...");
			printQueues();
		}
		catch (IOException e)
		{
			//System.out.println("flush exception");
			e.printStackTrace();
		}
	}

	
	private boolean load()
	{
		//System.out.println("load");
		String loading_filename = m_filename_queue.poll();
		//System.out.println(filename);
		if (loading_filename == null)
		{
			return false;
		}
		
		m_history_file_name = loading_filename;
		try
		{
//			m_history_queue.clear();
			File f = new File(loading_filename);
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();
			int count = 0;
			while (line != null)
			{
				try{
				Event event = EventUtils.fromString(line);
				//System.out.println("...." + event);
				if (event != null)
				{
					m_history_queue.add(event);
					count ++;
				}
				}catch (Exception e) {
					LogUtils.warn(loading_filename, e);
				}
				line = reader.readLine();
			}
			reader.close();
			f.delete();
			//System.out.println("load...");
			printQueues();
			return count > 0;
		}
		catch (Exception e)
		{
			//System.out.println("load exception");
			e.printStackTrace();
			return false;
		}
		
	}
	
	private void printQueues()
	{
		//System.out.println(String.format("----realtime queue:\t%s", Arrays.toString(m_realtime_queue.toArray(new Event[0]))));
		//System.out.println(String.format("----history queue:\t%s", Arrays.toString(m_history_queue.toArray(new Event[0]))));
	}
}
