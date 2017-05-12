package com.example.simple.flow.job;

import java.util.Map;

import com.example.simple.flow.service.Output;
import com.flowprocess.cedf.flow.IJob;
import com.flowprocess.cedf.service.ServiceUtils;

public class Five implements IJob {

	String serviceName;
	
	@Override
	public void init(Map<String, Object> config) {
		// TODO Auto-generated method stub
		serviceName=(String)config.get("serviceName");
	}

	@Override
	public String exec(Map<String, Object> data) {
		Output op=ServiceUtils.getService(serviceName);
		op.say(this.getClass().getSimpleName() +"\t"+ data.toString());
		return null;
	}

}
