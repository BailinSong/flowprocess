package com.flowprocess.cedf.flow;

import java.util.Map;


public interface IJob
{
	public void init(Map<String,Object> config);
	public String exec(Map<String,Object> data);
}
