package com.blueline.flowprocess.core.event;

import java.util.Map;

public interface IEventQueue
{
	public void init(Map<String, Object> config);
	public void start();
	public void stop();

	public void add(Event event);
	public Event take(int timeout) throws InterruptedException;
	public Event poll();
}
