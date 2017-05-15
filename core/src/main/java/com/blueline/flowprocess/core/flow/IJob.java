package com.blueline.flowprocess.core.flow;

import java.util.Map;


public interface IJob
{
	public void init(Map<String,Object> config);
	public String exec(Map<String,Object> data);
}
