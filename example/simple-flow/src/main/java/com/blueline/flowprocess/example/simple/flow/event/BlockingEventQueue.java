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
		// TODO Auto-generated method stub
		queue=new LinkedBlockingQueue<Event>();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void add(Event event) {
		// TODO Auto-generated method stub
		try {
			queue.put(event);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public Event take(int timeout) throws InterruptedException {
		// TODO Auto-generated method stub
		return queue.poll(timeout, TimeUnit.SECONDS);
	}

	@Override
	public Event poll() {
		// TODO Auto-generated method stub
		return queue.poll();
	}

}
