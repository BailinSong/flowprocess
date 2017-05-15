package com.blueline.commons;

@SuppressWarnings("unchecked")
public class ReflectUtils
{
	private ReflectUtils()
	{
		
	}
	
	public static <V> V newObject(String class_name) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Class<?> event_class = Class.forName(class_name);
		return (V) event_class.newInstance();
	}
}
