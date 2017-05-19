package com.blueline.flowprocess.core.monitor;
import java.util.Map;
public interface ICollect {
	public void collect(String cpType, Map<String, Object> dataMap);
	public boolean start();
	public boolean stop();
	public boolean init(Map<String, Object> config);
}
