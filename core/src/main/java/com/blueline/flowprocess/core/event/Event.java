package com.blueline.flowprocess.core.event;
import java.util.Map;
import com.alibaba.fastjson.JSON;
public class Event implements Cloneable
{
	private String m_id = "";
	private String m_task_id = "";
	private String m_flow_id = "";
	private String m_latest_result = "";
	private Map<String,Object> m_data;
	public String getId()
	{
		return m_id;
	}
	public void setId(String value)
	{
		m_id = value;
	}
	public String getTaskId()
	{
		return m_task_id;
	}
	public void setTaskId(String value)
	{
		m_task_id = value;
	}
	public String getFlowId()
	{
		return m_flow_id;
	}
	public void setFlowId(String value)
	{
		m_flow_id = value;
	}
	public String getLatestResult()
	{
		return m_latest_result;
	}
	public void setLatestResult(String value)
	{
		m_latest_result = value;
	}
	public Map<String,Object> getData()
	{
		return m_data;
	}
	public void setData(Map<String,Object> value)
	{
		m_data = value;
	}
	@Override
	public String toString()
	{
		return JSON.toJSONString(this);
	}
	public Event clone()
	{
		return JSON.parseObject(JSON.toJSONString(this), Event.class);
	}
}
