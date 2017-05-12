package com.example.simple.flow.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.flowprocess.cedf.event.EventUtils;
import com.flowprocess.cedf.log.LogUtils;
import com.flowprocess.cedf.log.logger.SystemOutLog;
import com.flowprocess.cedf.service.IService;

public class SayHi implements IService {

	Thread sayHiThread;
	final AtomicBoolean exitFlag= new AtomicBoolean(false);
	String eventQueueName;
	
	@Override
	public void init(Map<String, Object> config) {
		// TODO Auto-generated method stub
		eventQueueName=(String)config.get("EventQueueName");
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		sayHiThread=new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String,Object> event;
				while(!exitFlag.get()){
					event=new HashMap<String,Object>();
					event.put("msg", System.currentTimeMillis());
					EventUtils.add(eventQueueName,UUID.randomUUID().toString(), event);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LogUtils.warn("sayHiThread:interrupted");
					}
				}
			}
		});
		sayHiThread.setName("sayHiThread");
		sayHiThread.start();
	}

	@Override
	public void stop() {
		
		exitFlag.compareAndSet(true, false);
		sayHiThread.interrupt();
		
	}

}
