package com.blueline.flowprocess.example.simple.flow.event;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.blueline.flowprocess.core.event.Event;
import com.blueline.flowprocess.core.event.IEventQueue;
public class BlockingEventQueue implements IEventQueue {
	LinkedBlockingQueue<Event> queue;
	@Override
	public void init(Map<String, Object> config) {
		queue=new LinkedBlockingQueue<Event>();
	}
	@Override
	public void start() {
	}
	@Override
	public void stop() {
	}
	@Override
	public void add(Event event) {
		try {
			queue.put(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Override
	public Event take(int timeout) throws InterruptedException {
		return queue.poll(timeout, TimeUnit.SECONDS);
	}
	@Override
	public Event poll() {
		return queue.poll();
	}
}
