package com.blueline.flowprocess.core;
import java.util.Map;
import com.blueline.flowprocess.core.command.CedfCommand;
import com.blueline.flowprocess.core.command.CommandUtils;
import com.blueline.flowprocess.core.command.ICommandHandler;
import com.blueline.flowprocess.core.config.ConfigUtils;
import com.blueline.flowprocess.core.log.LogUtils;
public class PnCommandHandler implements ICommandHandler
{
	public String getCommand()
	{
		return CommandUtils.COMMAND_PROCESS_NODE;
	}
	public boolean handle(CedfCommand command)
	{
		LogUtils.traceFormat("%s[%s]\t%s\t%s", this.getClass().getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_COMMAND, command);
		String action = command.getAction();
		boolean ret = false;
		try
		{
			if (CommandUtils.ACTION_LOAD.equalsIgnoreCase(action))
			{
				Map<String, Object> params = command.getParams();
				String cluster = (String) params.get(CommandUtils.PARAM_CLUSTER);
				ret = ProcessNode.load(cluster, params);
			}
			else if (CommandUtils.ACTION_UNLOAD.equalsIgnoreCase(action))
			{
				ret = ProcessNode.unload();
			}
			else if (CommandUtils.ACTION_START.equalsIgnoreCase(action))
			{
				ret = ProcessNode.start();
			}
			else if (CommandUtils.ACTION_STOP.equalsIgnoreCase(action))
			{
					ret = ProcessNode.stop();
			}
			else if (CommandUtils.ACTION_EXIT.equalsIgnoreCase(action))
			{
					ret = ProcessNode.exit();
			}
			else
			{
				;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			LogUtils.warn(e);
			ret = false;
		}
		String ret_string = LogUtils.CONTENT_FAILED;
		if (ret)
		{
			ret_string = LogUtils.CONTENT_SUCCESSFUL;
		}
		LogUtils.traceFormat("%s[%s]\t%s\t%s\t%s", this.getClass().getSimpleName(), ConfigUtils.getId(), LogUtils.TYPE_EXEC, action, ret_string);
		return ret;
	}
}
