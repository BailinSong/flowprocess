package com.blueline.flowprocess.core.processunit;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.blueline.commons.XmlUtils;
import com.blueline.flowprocess.core.event.EventUtils;
import com.blueline.flowprocess.core.flow.Flow;
import com.blueline.flowprocess.core.log.LogUtils;
import com.blueline.flowprocess.core.service.ServiceUtils;

@SuppressWarnings("unchecked")
public class ProcessUnit
{
	private static final String PARAM_PROCESS_UNIT_CONFIG = "ProcessUnitConfig";
	private static final String PARAM_SERVICE_CONFIG = "ServiceConfig";
	private static final String PARAM_EVENT_CONFIG = "EventConfig";
	private static final String PARAM_FLOW_CONFIG = "FlowConfig";
	
	private static final String PARAM_FLOW = "Flow";
	private static final String PARAM_ID = "id";
	private static final String PARAM_FIELD="field";
	
	private Map<String,Flow> m_flow_map=new ConcurrentHashMap<String, Flow>();
	
	Map<String, Object> m_service_config;
	Map<String, Object> m_event_config;
	
	public ProcessUnit(String config_path) throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		Map<String, Object> config = (Map<String, Object>) XmlUtils.XML2MAP(config_path).get(PARAM_PROCESS_UNIT_CONFIG);
		init(config,null);
	}
	
	public ProcessUnit(String config_path,Map<String,String> params) throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		Map<String, Object> config = (Map<String, Object>) XmlUtils.XML2MAP(config_path).get(PARAM_PROCESS_UNIT_CONFIG);
		init(config,null);
	}
	public ProcessUnit(Map<String, Object> config ) throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		init(config,null);
	}
	public ProcessUnit(Map<String, Object> config ,Map<String,Object> params) throws InstantiationException, IllegalAccessException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		init(config, params);
	}

	public void init(Map<String, Object> config,Map<String,Object> params) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		
		Map<String, Object> event_config = (Map<String, Object>) config.get(PARAM_EVENT_CONFIG);
		EventUtils.init(event_config,params);
		Map<String, Object> service_config = (Map<String, Object>) config.get(PARAM_SERVICE_CONFIG);
		ServiceUtils.init(service_config,params);
		Map<String, Object> flow_config = (Map<String, Object>) config.get(PARAM_FLOW_CONFIG);
//		FlowUtils.init(flow_config,params);
		initFlows(flow_config,params);
		m_event_config=event_config;
		m_service_config=service_config;
		LogUtils.infoFormat("%s\t%s\t%s", this.getClass().getSimpleName(), LogUtils.TYPE_INIT, "successful");
	}

	public void start()
	{
		
		EventUtils.start(m_event_config);
		ServiceUtils.start(m_service_config);
//		FlowUtils.start();
		startFlows();
		
		
		LogUtils.infoFormat("%s\t%s\t%s", this.getClass().getSimpleName(), LogUtils.TYPE_START, "successful");
	}
	
	public void stop()
	{
		stopFlows();
//		FlowUtils.stop();
		ServiceUtils.stop(m_service_config);
		EventUtils.stop(m_event_config);
		LogUtils.infoFormat("%s\t%s\t%s", this.getClass().getSimpleName(), LogUtils.TYPE_STOP, "successful");
	}
	
	public void initFlows(Map<String, Object> config,
			Map<String, Object> params) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		if (config == null) {
			return;
		}

		Object flow_configs = config.get(PARAM_FLOW);
		if (flow_configs == null) {
			return;
		}

		if (flow_configs instanceof List) {
			List<Map<String, Object>> flow_config_list = (List<Map<String, Object>>) flow_configs;
			for (int i = 0; i < flow_config_list.size(); i++) {
				Map<String, Object> flow_config = flow_config_list.get(i);
				loadFlow(flow_config, params);
			}
		} else {
			Map<String, Object> flow_config = (Map<String, Object>) flow_configs;
			loadFlow(flow_config, params);
		}
		
//		LogUtils.debugFormat("%s\t%s\t%s", this.getClass().getSimpleName(),
//				LogUtils.TYPE_INIT, "successful");
	}

	private  void loadFlow(Map<String, Object> flow_config,
			Map<String, Object> params) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		if (params != null && !params.isEmpty()) {
			String field = (String) flow_config.get(PARAM_FIELD);
			if ((field == null || field.isEmpty())) {
				//flow_config.put(PARAM_FIELD, null);
				;
			} else {
				String field_value = (String) params.get(field);
				if(field_value != null && !field_value.isEmpty()){
					flow_config.put(PARAM_FIELD, field_value);
				}
			}
		}

		String id = (String) flow_config.get(PARAM_ID);
		Flow flow = new Flow();
		flow.init(flow_config);
		// TODO
//		putFlow(id, flow);
		m_flow_map.put(id, flow);
		LogUtils.infoFormat("%s\t%s\t%s", this.getClass().getSimpleName(),LogUtils.TYPE_INIT,id);
	}
	
	public  void startFlows() {
		for (Flow flow : m_flow_map.values()) {
			flow.start();
			LogUtils.infoFormat("%s\t%s\t%s\t%s", this.getClass().getSimpleName(),
					LogUtils.TYPE_START,flow.getId(), "successful");
		}
	}

	public  void stopFlows() {
		for (Flow flow : m_flow_map.values()) {
			flow.stop();
			LogUtils.infoFormat("%s\t%s\t%s\t%s", this.getClass().getSimpleName(),
					LogUtils.TYPE_STOP,flow.getId(), "successful");
		}

		
	}
}
