package com.flowprocess.cedf.service;

import java.util.Map;

public interface IService
{
	public void init(Map<String, Object> config);
	public void start();
	public void stop();
}
