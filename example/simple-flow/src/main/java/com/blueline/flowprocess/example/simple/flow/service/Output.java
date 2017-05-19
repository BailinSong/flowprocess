package com.blueline.flowprocess.example.simple.flow.service;
import java.util.Map;
import com.blueline.flowprocess.core.config.ConfigUtils;
import com.blueline.flowprocess.core.service.IService;
public class Output implements IService {
	@Override
	public void init(Map<String, Object> config) {
	}
	@Override
	public void start() {
	}
	@Override
	public void stop() {
	}
	public void say(String msg){
		System.out.println(ConfigUtils.getCluster()+"=>"+ConfigUtils.getId()+":\t"+msg);
	}
}
