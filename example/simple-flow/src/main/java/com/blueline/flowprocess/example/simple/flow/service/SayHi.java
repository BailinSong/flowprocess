package com.blueline.flowprocess.example.simple.flow.service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import com.blueline.flowprocess.core.event.EventUtils;
import com.blueline.flowprocess.core.log.LogUtils;
import com.blueline.flowprocess.core.log.logger.SystemOutLog;
import com.blueline.flowprocess.core.service.IService;
public class SayHi implements IService {
	Thread sayHiThread;
	final AtomicBoolean exitFlag= new AtomicBoolean(false);
	String eventQueueName;
	@Override
	public void init(Map<String, Object> config) {
		eventQueueName=(String)config.get("EventQueueName");
	}
	@Override
	public void start() {
		sayHiThread=new Thread(new Runnable() {
			@Override
			public void run() {
				Map<String,Object> event=null;
				while(!exitFlag.get()){
					try{
					event=new HashMap<String,Object>();
					event.put("msg", System.currentTimeMillis());
					EventUtils.add(eventQueueName,UUID.randomUUID().toString(), event);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LogUtils.warn("sayHiThread:interrupted");
					}
					}catch (Exception e) {
						LogUtils.warnFormat("Event {} {}", event,e);
					}
				}
			}
		});
		sayHiThread.setName("sayHiThread");
		sayHiThread.start();
	}
	@Override
	public void stop() {
		exitFlag.compareAndSet(false, true);
		sayHiThread.interrupt();
	}
}
