package com.flowprocess.cedf.components.command.channel;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.flowprocess.cedf.command.CedfCommand;
import com.flowprocess.cedf.command.CommandUtils;
import com.flowprocess.cedf.command.ICommandChannel;
import com.flowprocess.cedf.components.manager.JedisUtilManager;
import com.flowprocess.cedf.log.LogUtils;
import com.flowprocess.commons.JedisUtil;

import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPubSub;

public class RedisCommandChannel implements ICommandChannel
{
	public static final String PARAM_CHANNEL_NAME = "ChannelName";
	public static final String PARAM_STORAGE = "Storage";

	String m_command_channel_name;

	JedisCommands m_jedis_command;
	JedisPubSub m_pub_sub;
	JedisUtil m_jedis_util;


	@SuppressWarnings("unchecked")
	public void init(Map<String, Object> config)
	{

		//String storage=(String)config.get(PARAM_STORAGE);
		
		//Map<String,Object> storage_config = ConfigUtils.getConfig(PARAM_STORAGE, storage);
		//m_jedis_util = new JedisUtil(storage_config);
		m_jedis_util = JedisUtilManager.getJedisUtilInstance((String)config.get(PARAM_STORAGE));
		
		m_command_channel_name = (String) config.get(PARAM_CHANNEL_NAME);

		m_pub_sub = new JedisPubSub()
		{
			@Override
			public void onMessage(String channel, String message)
			{
				LogUtils.debugFormat("Receive: channel[%s], message[%s] ", channel,
						message);
				Map<String, Object> data = (Map<String, Object>) JSON.parse(message);

				CedfCommand command = new CedfCommand(data);
				CommandUtils.execCommand(command);
			}

			@Override
			public void onSubscribe(String channel, int subscribedChannels)
			{
				LogUtils.infoFormat("Subscribe: channel[%s], subscribedChannels[%d] success.", channel, subscribedChannels);
			}

		};

	}

	/*
	 * (�� Javadoc) <p>Title: sub</p> <p>Description: </p>
	 * 
	 * @param channel
	 * 
	 * @see com.wisdom.csmp.command.ICommandChannel#sub(java.lang.String)
	 */
	public void subscribe() {
		try {
			m_jedis_util.sub(m_pub_sub, m_command_channel_name);
		} catch (Exception e) {
			LogUtils.error(e);
			throw new RuntimeException(e);
		}
	}

	/*
	 * (�� Javadoc) <p>Title: pub</p> <p>Description: </p>
	 * 
	 * @param message
	 * 
	 * @see com.wisdom.csmp.command.ICommandChannel#pub(java.util.Map)
	 */
	public void publish(CedfCommand command)
	{
		try {
			LogUtils.debugFormat("publish: channel[{}], message[{}]", m_command_channel_name,command.toJsonString());
			m_jedis_util.pub(command.toJsonString(), m_command_channel_name);
		} catch (Exception e) {
			LogUtils.error(e);
			throw new RuntimeException(e);
		}
	}
}
