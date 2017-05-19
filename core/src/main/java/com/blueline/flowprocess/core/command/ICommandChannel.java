package com.blueline.flowprocess.core.command;
import java.util.Map;
public interface ICommandChannel
{
	public void init(Map<String,Object> config);
	public void subscribe();
	public void publish(CedfCommand command);
}
