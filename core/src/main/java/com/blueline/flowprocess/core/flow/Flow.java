package com.blueline.flowprocess.core.flow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blueline.flowprocess.core.event.Event;
import com.blueline.flowprocess.core.event.EventUtils;
import com.blueline.flowprocess.core.event.IEventQueue;
import com.blueline.flowprocess.core.log.LogUtils;

@SuppressWarnings("unchecked")
public class Flow
{
	private static final String PARAM_EVENT_QUEUE= "eventqueue";
	protected static final String PARAM_FIELD = "field";

	private static final String PARAM_ID = "id";
	private static final String PARAM_STEPS = "Steps";
	private static final String PARAM_STEP = "Step";
	private static final String PARAM_TYPE = "type";
	private static final String PARAM_SPECIAL_TYPE = "specialtype";
	private static final String PARAM_JOB_CLASS = "jobclass";
	private static final String PARAM_RELATIONS = "Relations";
	private static final String PARAM_RELATION = "Relation";
	private static final String PARAM_NEXT_ID = "nextid";
	private static final String PARAM_CONDITION = "condition";
	
	private static final String FIRST_STEP = "0";
	
	private TakeThread m_take_thread;
	private IEventQueue m_event_queue;
	
	private String m_id;
	private String m_event_queue_id;
	private Map<String, Step> m_step_map = new ConcurrentHashMap<String, Step>();
	private Map<String, Relation> m_relation_map = new ConcurrentHashMap<String, Relation>();
	
	public String getId()
	{
		return m_id;
	}
	
	private void setId(String value)
	{
		m_id = value;
	}
	
	public String getEventQueueId()
	{
		return m_event_queue_id;
	}
	
	private void setEventQueueId(String value)
	{
		m_event_queue_id = value;
	}
	
	private void addStep(Step step)
	{
		m_step_map.put(step.getId(), step);
	}
	
	private Step getStep(String id)
	{
		return m_step_map.get(id);
	}
	
	private Step getNextStep(String step_id, String condition)
	{
		Step step = getStep(step_id);
		String key = null;
		if (Step.TYPE_NORMAL.equals(step.getType()))
		{
			key = Relation.buildKey(step_id, null);
		}
		else
		{
			key = Relation.buildKey(step_id, condition);
		}
		
		Relation relation = m_relation_map.get(key);
		if (relation == null)
		{
			return null;
		}
		else
		{
			return getStep(relation.getNextId());
		}
	}
	
	private void addRelation(Relation relation)
	{
		m_relation_map.put(relation.getKey(), relation);
	}

	private Step newStep(Map<String, Object> step_config) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		String step_id = (String) step_config.get(PARAM_ID);
		String type = (String) step_config.get(PARAM_TYPE);
		String special_type = (String) step_config.get(PARAM_SPECIAL_TYPE);
		String job_class = (String) step_config.get(PARAM_JOB_CLASS);
		
		Step step = new Step(getId(), step_id, type, special_type, job_class, step_config);
		return step;
	}

	private Relation newRelation(Map<String, Object> relation_config)
	{
		String step_id = (String) relation_config.get(PARAM_ID);
		String next_id = (String) relation_config.get(PARAM_NEXT_ID);
		String condition = (String) relation_config.get(PARAM_CONDITION);
		
		Relation relation = new Relation(step_id, next_id, condition);
		return relation;
	}
	
	public void init(Map<String, Object> config) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		
		
		String id = (String) config.get(PARAM_ID);
//		Map<String,Object> config=(Map<String,Object>)ConfigUtils.getConfig("Service",id);
		setId(id);
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), getId(), LogUtils.TYPE_CONFIG, config);
		String event_queue_id = (String) config.get(PARAM_EVENT_QUEUE);
		setEventQueueId(event_queue_id);
		String field = (String) config.get(PARAM_FIELD);
//		setEventQueueId(field);
		m_event_queue = EventUtils.getEventQueue(event_queue_id, field);
		
		m_take_thread = null;
		
		Map<String, Object> step_configs = (Map<String, Object>) config.get(PARAM_STEPS);
		Object step_config_object = step_configs.get(PARAM_STEP);
		if (step_config_object == null)
		{
			throw new RuntimeException("no steps");
		}
		else if (step_config_object instanceof List)
		{
			List<Map<String, Object>> step_config_list = (List<Map<String, Object>>) step_config_object;
			for (Map<String, Object> step_config : step_config_list)
			{
				Step step = newStep(step_config);
				addStep(step);
			}
		}
		else
		{
			Map<String, Object> step_config = (Map<String, Object>) step_config_object;
			Step step = newStep(step_config);
			addStep(step);
		}
		
		Map<String, Object> relation_configs = (Map<String, Object>) config.get(PARAM_RELATIONS);
		Object relation_config_object = relation_configs.get(PARAM_RELATION);
		if (relation_config_object == null)
		{
			throw new RuntimeException("no relations");
		}
		else if (relation_config_object instanceof List)
		{
			List<Map<String, Object>> relation_config_list = (List<Map<String, Object>>) relation_config_object;
			for (Map<String, Object> relation_config : relation_config_list)
			{
				Relation relation = newRelation(relation_config);
				addRelation(relation);
			}
		}
		else
		{
			Map<String, Object> relation_config = (Map<String, Object>) relation_config_object;
			Relation relation = newRelation(relation_config);
			addRelation(relation);
		}

		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), getId(), LogUtils.TYPE_INIT, LogUtils.CONTENT_SUCCESSFUL);
	}

	public void start()
	{
		m_take_thread = new TakeThread(m_event_queue, this);
		m_take_thread.setName(m_id);
		m_take_thread.start();
		
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), getId(), LogUtils.TYPE_START, LogUtils.CONTENT_SUCCESSFUL);
	}
	
	public void stop()
	{
		if (m_take_thread != null)
		{
			if (m_take_thread.isAlive())
			{
				m_take_thread.exit();
			}
			m_take_thread = null;
		}
		
		LogUtils.debugFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), getId(), LogUtils.TYPE_STOP, LogUtils.CONTENT_SUCCESSFUL);
	}

	public void exec(Event event)
	{
		Step step = getStep(FIRST_STEP);
		String last_result = null;
		while (step != null)
		{
			last_result = step.exec(event);
			event.setLatestResult(last_result);
			step = getNextStep(step.getId(), last_result);
		}

		LogUtils.traceFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), getId(), LogUtils.TYPE_EXEC, event, last_result);
	}
}
