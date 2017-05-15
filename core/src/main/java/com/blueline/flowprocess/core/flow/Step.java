package com.blueline.flowprocess.core.flow;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.blueline.commons.ReflectUtils;
import com.blueline.flowprocess.core.event.Event;
import com.blueline.flowprocess.core.log.LogUtils;

public class Step
{
	public static final String TYPE_NORMAL = "0";
	public static final String TYPE_SPECIAL = "1";
	
	private String m_flow_id;
	private String m_id;
	private String m_type;
	private String m_special_type;
	private String m_job_class;
	
	private IJob m_job;
	
	public Step(String flow_id, String id, String type, String special_type, String job_class, Map<String,Object> config) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		setFlowId(flow_id);
		setId(id);
		setType(type);
		setSpecialType(special_type);
		setJobClass(job_class);
		
//		Event event = new Event();
//		event.setFlowId(flow_id);
//		event.setStepId(id);
		
		if (getJobClass() != null)
		{
			m_job = ReflectUtils.newObject(getJobClass());
			m_job.init(config);
		}
	}
	
	public String getFlowId()
	{
		return m_flow_id;
	}
	
	private void setFlowId(String value)
	{
		m_flow_id = value;
	}

	public String getId()
	{
		return m_id;
	}
	
	private void setId(String value)
	{
		m_id = value;
	}
	
	public String getType()
	{
		return m_type;
	}
	
	private void setType(String value)
	{
		m_type = value;
	}
	
	public String getSpecialType()
	{
		return m_special_type;
	}
	
	private void setSpecialType(String value)
	{
		m_special_type = value;
	}
	
	public String getJobClass()
	{
		return m_job_class;
	}
	
	private void setJobClass(String value)
	{
		m_job_class = value;
	}
	
	@Override
	public String toString()
	{
//		StringBuffer sb = new StringBuffer();
//		
//		sb.append("FlowId=");
//		sb.append(getFlowId());
//		sb.append("&Id=");
//		sb.append(getId());
//		sb.append("&Type=");
//		sb.append(getType());
//		sb.append("&SpecialType=");
//		sb.append(getSpecialType());
//		sb.append("&JobClass=");
//		sb.append(getJobClass());
//		
//		return sb.toString();
		
		return JSON.toJSONString(this);
	}
	
	public String exec(Event event)
	{
		String last_result = null;
		if (TYPE_SPECIAL.equals(getType()))
		{
			last_result = event.getLatestResult();
			LogUtils.traceFormat("%s[%s-%s]\t%s\t%s", this.getClass().getSimpleName(), getFlowId(), getId(), LogUtils.TYPE_EXEC, event, last_result);
			return last_result;
		}
		
		last_result = m_job.exec(event.getData());
		
		LogUtils.traceFormat("%s[%s-%s]\t%s\t%s", this.getClass().getSimpleName(), getFlowId(), getId(), LogUtils.TYPE_EXEC, event, last_result);
		return last_result;
	}
}
