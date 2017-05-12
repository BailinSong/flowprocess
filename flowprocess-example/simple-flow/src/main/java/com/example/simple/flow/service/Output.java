package com.example.simple.flow.service;

import java.util.Map;

import com.flowprocess.cedf.config.ConfigUtils;
import com.flowprocess.cedf.service.IService;

public class Output implements IService {

	@Override
	public void init(Map<String, Object> config) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}
	
	public void say(String msg){
		System.out.println(ConfigUtils.getCluster()+"=>"+ConfigUtils.getId()+":\t"+msg);
	}

}
