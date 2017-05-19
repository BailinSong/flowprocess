package com.blueline.flowprocess.core.flow;
public class Relation
{
	private static final Object CONNECTOR_SYMBOL = "@";
	private String m_step_id;
	private String m_next_id;
	private String m_condition;
	public Relation(String step_id, String next_id, String condition)
	{
		setStepId(step_id);
		setNextId(next_id);
		setCondition(condition);
	}
	public String getStepId()
	{
		return m_step_id;
	}
	private void setStepId(String value)
	{
		m_step_id = value;
	}
	public String getNextId()
	{
		return m_next_id;
	}
	private void setNextId(String value)
	{
		m_next_id = value;
	}
	public String getCondition()
	{
		return m_condition;
	}
	private void setCondition(String value)
	{
		m_condition = value;
	}
	public String getKey()
	{
		return buildKey(getStepId(), getCondition());
	}
	public static String buildKey(String step_id, String condition)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(step_id);
		sb.append(CONNECTOR_SYMBOL);
		if (condition != null)
		{
			sb.append(condition);
		}
		return sb.toString();
	}
}
