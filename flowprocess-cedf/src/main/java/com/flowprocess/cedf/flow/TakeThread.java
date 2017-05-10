package com.flowprocess.cedf.flow;

import java.util.concurrent.atomic.AtomicBoolean;

import com.flowprocess.cedf.log.LogUtils;
import com.flowprocess.cedf.event.Event;
import com.flowprocess.cedf.event.IEventQueue;

public class TakeThread extends Thread
{
	private static final int TIMEOUT_INTERVAL = 1;
	
	private final IEventQueue m_event_queue;
	private final Flow m_flow;
	
	public TakeThread(IEventQueue event_queue, Flow flow)
	{
		m_event_queue = event_queue;
		m_flow = flow;
	}
	
	private AtomicBoolean m_exit_flag = new AtomicBoolean(false);
	
	public void exit()
	{
		m_exit_flag.set(true);
		this.interrupt();
	}
	
	@Override
	public void run()
	{
		while (!m_exit_flag.get())
		{
			try
			{
				Event event = m_event_queue.take(TIMEOUT_INTERVAL);
				if (event != null)
				{
					m_flow.exec(event);
//					FlowUtils.exec(event);
					LogUtils.traceFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), m_flow.getId(), LogUtils.TYPE_EXEC, event);
				}
				else
				{
					//LogUtils.traceFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), m_flow.getId(), LogUtils.TYPE_TAKE, null);
				}
			}
			catch (InterruptedException ie)
			{
				break;
			}
			catch (Exception e)
			{
				LogUtils.warn(e);
				LogUtils.warnFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), m_flow.getId(), LogUtils.TYPE_EXCEPTION, e);
				//e.printStackTrace();
			}
		}
	}
}
