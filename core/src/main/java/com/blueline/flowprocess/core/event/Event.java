package com.blueline.flowprocess.core.event;

import java.util.Map;

import com.alibaba.fastjson.JSON;

public class Event implements Cloneable
{
	private String m_id = "";
	private String m_task_id = "";
	private String m_flow_id = "";
//	private String m_step_id = "";
	
	private String m_latest_result = "";

//	private String m_data_factory = "";
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
	
//	public String getStepId()
//	{
//		return m_step_id;
//	}
//	
//	public void setStepId(String value)
//	{
//		m_step_id = value;
//	}
	
	public String getLatestResult()
	{
		return m_latest_result;
	}
	
	public void setLatestResult(String value)
	{
		m_latest_result = value;
	}
	
//	public String getDataFactory()
//	{
//		return m_data_factory;
//	}
	
//	private void setDataFactory(String value)
//	{
//		m_data_factory = value;
//	}
	
	public Map<String,Object> getData()
	{
		return m_data;
	}
	
	public void setData(Map<String,Object> value)
	{
		m_data = value;
//		if (value != null)
//		{
////			setDataFactory(value.getClass().getName());
//			setDataFactory(value.getFactoryClassString());
//		}
	}
	
	@Override
	public String toString()
	{
//		StringBuffer sb = new StringBuffer();
//		
//		sb.append("EventId=");
//		sb.append(getId());
//		sb.append("&FlowId=");
//		sb.append(getFlowId());
//		sb.append("&TaskId=");
//		sb.append(getTaskId());
////		sb.append("&StepId=");
////		sb.append(getStepId());
//		sb.append("&LatestResult=");
//		sb.append(getLatestResult());
//		sb.append("&DataFactory=");
////		sb.append(getDataFactory());
//		sb.append("&Data={");
//		getData();
//		if (data != null)
//		{
//			sb.append(data.toString());
//		}
//		sb.append("}");
		
		return JSON.toJSONString(this);
	}
	
	public Event clone()
	{
//		Event event = new Event();
//		
//		event.setId(getId());
//		event.setFlowId(getFlowId());
//		event.setTaskId(getTaskId());
////		event.setStepId(getStepId());
//		event.setLatestResult(getLatestResult());
////		event.setData(getData());
		
		return JSON.parseObject(JSON.toJSONString(this), Event.class);
	}
}
