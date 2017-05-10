package com.flowprocess.cedf.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.flowprocess.cedf.config.ConfigUtils;



public class CedfCommandTool
{
	private static final String COMMAND_EXIT = "exit";
	private static final String COMMAND_SET = "set";
	private static final String COMMAND_PN = CommandUtils.COMMAND_PROCESS_NODE;
	private static final String COMMAND_PU = CommandUtils.COMMAND_PROCESS_UNIT;
	
	private static final String PARAM_RANGE = "range";
	private static final String PARAM_NAME = "name";
	
	public static void main(String[] args) throws Exception
	{
		String range = CommandUtils.RANGE_ALL;
		String name = "";
		String command_word;
		String action;
		Map<String, Object> params = new HashMap<String, Object>();
		
		ConfigUtils.init(new HashMap<String, Object>());
		CommandUtils.init(new HashMap<String, Object>());
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true)
		{
			String line = in.readLine();
			if (COMMAND_EXIT.equalsIgnoreCase(line))
			{
				break;
			}
			
			String[] parts = line.split(" ");
			command_word = parts[0];
			
			if (COMMAND_SET.equals(command_word))
			{
				//set [range=(all|cluster|node)] [name=<name>]
				if (parts.length == 1)
				{
					System.out.println("range=" + range + "\tname=" + name);
				}
				else
				{
					for (int i = 1; i < parts.length; i ++)
					{
						String[] entry = parts[i].split("=");
						String key = entry[0];
						String value = entry[1];
						if (PARAM_RANGE.equalsIgnoreCase(key))
						{
							if ((CommandUtils.RANGE_ALL.equalsIgnoreCase(value)) || (CommandUtils.RANGE_CLUSTER.equalsIgnoreCase(value)) || (CommandUtils.RANGE_NODE.equalsIgnoreCase(value)))
							{
								range = value;
							}
							else
							{
								System.out.println("invalid param, usage: set [range=(all|cluster|node)] [name=<name>]");
							}
						}
						else if (PARAM_NAME.equalsIgnoreCase(key))
						{
							name = value;
						}
						else
						{
							System.out.println("invalid param, usage: set [range=(all|cluster|node)] [name=<name>]");
						}
					}
				}
			}
			else if (COMMAND_PN.equalsIgnoreCase(command_word))
			{
				//pn load <cluster> [<paramkey>=<paramvalue>]{0,n}
				//pn (start|stop|unload|exit)
				action = parts[1];
				params = new HashMap<String, Object>();
				if (CommandUtils.ACTION_LOAD.equals(action))
				{
					params.put(CommandUtils.PARAM_CLUSTER, parts[2]);
					for (int i = 3; i < parts.length; i ++)
					{
						String[] entry = parts[i].split("=");
						String key = entry[0];
						String value = entry[1];
						params.put(key, value);
					}
					CedfCommand command = new CedfCommand(range, name, command_word, action, params);
					CommandUtils.publish(command);
				}
				else if ((CommandUtils.ACTION_START.equals(action)) || (CommandUtils.ACTION_STOP.equals(action)) || (CommandUtils.ACTION_UNLOAD.equals(action)) || (CommandUtils.ACTION_EXIT.equals(action)))
				{
					CedfCommand command = new CedfCommand(range, name, command_word, action, params);
					CommandUtils.publish(command);
				}				
				else
				{
					System.out.println("invalid param, usage: pn load <cluster> [paramkey=paramvalue]{0,n}\n" +
									   "                  or: pn (start|stop|unload|exit)");
				}
			}
			else if (COMMAND_PU.equalsIgnoreCase(command_word))
			{
				//load <category> [id=<id>] [<paramkey>=<paramvalue>]{0,n}
				//start <id> <count> [<category>]
				//stop <id> <count> [<category>]
				//adjust <id> <count> [<category>]
				//unload <id>
				action = parts[1];
				params = new HashMap<String, Object>();
				if (CommandUtils.ACTION_LOAD.equals(action))
				{
					params.put(CommandUtils.PARAM_CATEGORY, parts[2]);
					for (int i = 3; i < parts.length; i ++)
					{
						String[] entry = parts[i].split("=");
						String key = entry[0];
						String value = entry[1];
						params.put(key, value);
					}
					CedfCommand command = new CedfCommand(range, name, command_word, action, params);
					CommandUtils.publish(command);
				}
				else if ((CommandUtils.ACTION_START.equals(action)) || (CommandUtils.ACTION_STOP.equals(action)) || (CommandUtils.ACTION_ADJUST.equals(action)))
				{
					params.put(CommandUtils.PARAM_ID, parts[2]);
					params.put(CommandUtils.PARAM_COUNT, parts[3]);
					if (parts.length > 4)
					{
						params.put(CommandUtils.PARAM_CATEGORY, parts[4]);
					}
					CedfCommand command = new CedfCommand(range, name, command_word, action, params);
					CommandUtils.publish(command);
				}
				else if (CommandUtils.ACTION_UNLOAD.equalsIgnoreCase(action))
				{
					params.put(CommandUtils.PARAM_ID, parts[2]);
					CedfCommand command = new CedfCommand(range, name, command_word, action, params);
					CommandUtils.publish(command);
				}
				else
				{
					System.out.println("invalid param, usage: pu load <category> [id=<id>] [<paramkey>=<paramvalue>]{0,n}\n" +
									   "                  or: pu (start|stop|adjust) <id> <count> [<category>]\n" +
									   "                  or: pu unload <id>");
				}
			}
		}
	}
}
