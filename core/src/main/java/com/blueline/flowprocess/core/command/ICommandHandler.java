package com.blueline.flowprocess.core.command;
public interface ICommandHandler
{
	public String getCommand();
	public boolean handle(CedfCommand command);
}
