package com.flowprocess.cedf.command;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.flowprocess.cedf.config.ConfigUtils;
import com.flowprocess.cedf.log.LogUtils;
import com.flowprocess.commons.ReflectUtils;

@SuppressWarnings("unchecked")
public class CommandUtils
{
	private static ICommandChannel m_command_channel;
	private static String[] DEFAULT_COMMAND_HANDLER_CLASSES = new String[] {
		"com.flowprocess.cedf.PnCommandHandler",
		"com.flowprocess.cedf.processunit.PuCommandHandler",
//		"com.flowprocess.cedf.event.EqCommandHandler",
//		"com.flowprocess.cedf.event.EqtCommandHandler",
//		"com.flowprocess.cedf.service.ServiceCommandHandler"
	};

	private CommandUtils() {}
	
	public static final String RANGE_ALL = "all";
	public static final String RANGE_CLUSTER = "cluster";
	public static final String RANGE_NODE = "node";
	
	public static final String COMMAND_PROCESS_NODE = "pn";
	public static final String COMMAND_PROCESS_UNIT = "pu";
	public static final String COMMAND_SERVICE = "service";
	public static final String COMMAND_EVENT_QUEUE = "eq";
	public static final String COMMAND_EVENT_QUEUE_TEMPLATE = "eqt";
	
	public static final String ACTION_LOAD = "load";
	public static final String ACTION_UNLOAD = "unload";
	public static final String ACTION_EXIT = "exit";
	public static final String ACTION_START = "start";
	public static final String ACTION_STOP = "stop";
	public static final String ACTION_ADJUST = "adjust";
	
	public static final String PARAM_CLUSTER = "cluster";
	public static final String PARAM_ID = "id";
	public static final String PARAM_CLASS = "class";
	public static final String PARAM_COUNT = "count";
	public static final String PARAM_CATEGORY = "category";
	public static final String PARAM_FIELD = "field";
	
	public static final String CHANNEL_PARAM_COMMAND_CHANNEL = "CommandChannel";
	public static final String CHANNEL_PARAM_STORAGE = "Storage";
	public static final String CHANNEL_PARAM_CHANNEL_NAME = "ChannelName";
	
	private static final Map<String, Object> DEFAULT_COMMAND_CHANNEL_CONFIG = new HashMap<String, Object>();


	private static final Map<String, ICommandHandler> m_command_handler_map = new ConcurrentHashMap<String, ICommandHandler>();

	public static void init(Map<String, Object> config) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		LogUtils.debugFormat("%s[%s]\t%s\t%s", CommandUtils.class.getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_CONFIG, config);
		
		//command channel
		Map<String, Object> command_channel_config = (Map<String, Object>) config.get(CHANNEL_PARAM_COMMAND_CHANNEL);
		if (command_channel_config == null)
		{
			command_channel_config = DEFAULT_COMMAND_CHANNEL_CONFIG;
		}
		String command_channel_class = (String) command_channel_config.get(PARAM_CLASS);
		m_command_channel = ReflectUtils.newObject(command_channel_class);
		m_command_channel.init(command_channel_config);
		
		//command handler
		for (int i = 0; i < DEFAULT_COMMAND_HANDLER_CLASSES.length; i ++)
		{
			loadCommandHandler(DEFAULT_COMMAND_HANDLER_CLASSES[i]);
		}
		
		LogUtils.infoFormat("%s[%s]\t%s\t%s", CommandUtils.class.getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_INIT, LogUtils.CONTENT_SUCCESSFUL);
	}
	
	private static void loadCommandHandler(String handler_class) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		ICommandHandler command_handler = ReflectUtils.newObject(handler_class);
		m_command_handler_map.put(command_handler.getCommand(), command_handler);

		LogUtils.debugFormat("%s[%s]\t%s\t%s", CommandUtils.class.getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_LOAD, handler_class);
	}

	public static CedfCommand newCommand(String range, String name, String command, String action, String... params)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		if (params != null)
		{
			for (int i = 0; i < params.length; i +=2)
			{
				map.put(params[i], params[i + 1]);
			}
		}
		return new CedfCommand(range, name, command, action, map);
	}

	public static void subscribe()
	{
		m_command_channel.subscribe();
	}
	public static void publish(CedfCommand command)
	{
		m_command_channel.publish(command);
	}

	public static void execCommand(CedfCommand command)
	{
		String range = command.getRange();
		String name = command.getName();
		boolean execute = false;
		if (CommandUtils.RANGE_ALL.equalsIgnoreCase(range))
		{
			execute = true;
		}
		else if (CommandUtils.RANGE_CLUSTER.equalsIgnoreCase(range))
		{
			if ((name != null) && (name.equalsIgnoreCase(ConfigUtils.getCluster())))
			{
				execute = true;
			}
		}
		else if (CommandUtils.RANGE_NODE.equalsIgnoreCase(range))
		{
			if ((name != null) && (name.equalsIgnoreCase(ConfigUtils.getId())))
			{
				execute = true;
			}
		}
		else
		{
			;
		}
		
		boolean ret = false;
		if (execute)
		{
			String command_word = command.getCommandWord();
			ICommandHandler command_handler = m_command_handler_map.get(command_word);
			ret = command_handler.handle(command);
		}
		
		String ret_string = LogUtils.CONTENT_FAILED;
		if (ret)
		{
			ret_string = LogUtils.CONTENT_SUCCESSFUL;
		}
		LogUtils.traceFormat("%s[%s]\t%s\t%s", CommandUtils.class.getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_EXEC, ret_string);
	}
}
