package com.blueline.flowprocess.core.processunit;
import java.util.Map;
import com.blueline.flowprocess.core.command.CedfCommand;
import com.blueline.flowprocess.core.command.CommandUtils;
import com.blueline.flowprocess.core.command.ICommandHandler;
import com.blueline.flowprocess.core.config.ConfigUtils;
import com.blueline.flowprocess.core.log.LogUtils;
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
