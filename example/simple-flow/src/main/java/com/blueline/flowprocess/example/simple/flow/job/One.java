package com.blueline.flowprocess.example.simple.flow.job;
import java.util.Map;
import com.blueline.flowprocess.core.flow.IJob;
import com.blueline.flowprocess.core.service.ServiceUtils;
import com.blueline.flowprocess.example.simple.flow.service.Output;
public class One implements IJob {
	String serviceName;
	@Override
	public void init(Map<String, Object> config) {
		serviceName=(String)config.get("serviceName");
	}
	@Override
	public String exec(Map<String, Object> data) {
		Output op=ServiceUtils.getService(serviceName);
		op.say(this.getClass().getSimpleName() +"\t"+ data.toString());
		return null;
	}
}
