package com.flowprocess.cedf.command;

public interface ICommandHandler
{
	public String getCommand();
	public boolean handle(CedfCommand command);
}
