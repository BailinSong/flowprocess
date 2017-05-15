package com.blueline.flowprocess.core.config.client;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.blueline.commons.XmlUtils;
import com.blueline.flowprocess.core.config.IConfigClient;

@SuppressWarnings("unchecked")
public class XmlFileConfigClient implements IConfigClient
{
	public static final String PARAM_RESOURCE_PATH = "ResourcePath";
	public static final String PARAM_FILE_PATH = "FilePath";
	
	String m_base_path;
	String m_resource_path;
	
	private AtomicInteger m_counter = new AtomicInteger(0);
	
	public String generateId()
	{
		return "ProcessNode_" + getHostIp(getInetAddress()) + "_" + String.format("%04d", m_counter.incrementAndGet());
	}
	
    public static InetAddress getInetAddress()
    {
		try
		{
			return InetAddress.getLocalHost();
		}
		catch (UnknownHostException e)
		{
		}  
		return null;  
	}  

	public static String getHostIp(InetAddress netAddress)
	{  
		if (null == netAddress)
		{  
			return null;  
		}  
		String ip = netAddress.getHostAddress(); //get the ip address  
		return ip;  
	}  

	
	public <V> V getConfig(String type, String name)
	{
		String xml_path = m_base_path + type + File.separatorChar + name + ".xml";
		try
		{
			return (V) XmlUtils.XML2MAP(xml_path).get(type);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void init(Map<String, Object> param)
	{
		m_base_path=(String)param.get(PARAM_FILE_PATH);
		m_resource_path=(String)param.get(PARAM_RESOURCE_PATH);
	}

	private String getConfigString(String type, String name)
	{
		return XmlUtils.FileToXMLString(String.format("%s/%s/%s.xml", m_base_path,type,name));
	}

	private <V> V string2Map(String config_str)
	{
		try
		{
			return (V) XmlUtils.XMLString2MAP(config_str);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		} 
	}

	public <V> V getConfig(String type, String name, String format, Map<String, Object> param_map)
	{
		if (param_map == null)
		{
			return getConfig(type, name);
		}
		else
		{
			String string = getConfigString(type, name);
			for (Entry<String, Object> entry : param_map.entrySet())
			{
				string = string.replaceAll(String.format(format, entry.getKey()), (String) entry.getValue());
			}
			return (V) ((Map<String, Object>) string2Map(string)).get(type);
		}
	}
}
