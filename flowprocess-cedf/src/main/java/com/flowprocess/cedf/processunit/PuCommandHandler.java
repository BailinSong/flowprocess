package com.flowprocess.cedf.processunit;

import java.util.Map;

import com.flowprocess.cedf.command.CedfCommand;
import com.flowprocess.cedf.command.CommandUtils;
import com.flowprocess.cedf.command.ICommandHandler;
import com.flowprocess.cedf.config.ConfigUtils;
import com.flowprocess.cedf.log.LogUtils;

public class PuCommandHandler implements ICommandHandler
{
	public String getCommand()
	{
		return CommandUtils.COMMAND_PROCESS_UNIT;
	}

	public boolean handle(CedfCommand command)
	{
		LogUtils.traceFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_COMMAND, command);
		
		String action = command.getAction();
		boolean ret = false;
		Map<String, Object> params = command.getParams();
		String id = (String) params.get(CommandUtils.PARAM_ID);

		try
		{
			if (CommandUtils.ACTION_LOAD.equalsIgnoreCase(action))
			{
				String category = (String) params.get(CommandUtils.PARAM_CATEGORY);
				ret = ProcessUnitUtils.load(category, id, params);
			}
			else if (CommandUtils.ACTION_UNLOAD.equalsIgnoreCase(action))
			{
				ret = ProcessUnitUtils.unload(id);
			}
			else if (CommandUtils.ACTION_START.equalsIgnoreCase(action))
			{
				int count = Integer.parseInt((String) params.get(CommandUtils.PARAM_COUNT));
//				System.out.println("----PuCommandHandler-handle-start");
//				System.out.println(id);
//				System.out.println(params);
//				System.out.println(count);
				ProcessUnitUtils.startProcessUnit(id, params, count);
				ret = true;
			}
			else if (CommandUtils.ACTION_STOP.equalsIgnoreCase(action))
			{
				int count = Integer.parseInt((String) params.get(CommandUtils.PARAM_COUNT));
				ProcessUnitUtils.stopProcessUnit(id, count);
				ret = true;
			}
			else if (CommandUtils.ACTION_ADJUST.equalsIgnoreCase(action))
			{
					int count = Integer.parseInt((String) params.get(CommandUtils.PARAM_COUNT));
					ProcessUnitUtils.adjustProcessUnit(id, params, count);
					ret = true;
			}
			else
			{
				;
			}
		}
		catch (Exception e)
		{
			LogUtils.warn(e);
			ret = false;
		}

		String ret_string = LogUtils.CONTENT_FAILED;
		if (ret)
		{
			ret_string = LogUtils.CONTENT_SUCCESSFUL;
		}
		LogUtils.traceFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_EXEC, ret_string);
		return ret;
	}

}
