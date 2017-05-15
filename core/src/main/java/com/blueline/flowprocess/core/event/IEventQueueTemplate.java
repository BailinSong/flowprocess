package com.blueline.flowprocess.core.event;

import java.util.Map;


public interface IEventQueueTemplate
{
	public void init(Map<String, Object> config);
	public void start();
	public void stop();

	public void add(String field, Event event);
	public IEventQueue getEventQueue(String field);
//	public Event take(int timeout) throws InterruptedException;
}
