package com.blueline.flowprocess.components.service.storage.api;

public interface IExceedStorage <T>{
	/**
	 * 
	 * @param interval ��ʱ��ʱ��������λΪ��
	 */
	public void setDefaultInterval(long interval);
	public void regHandler(IHandler<T> handler);
	public String add(T data);
	/**
	 * 
	 * @param data
	 * @param ouvertime ��ʱ��ʱ��������λΪ��
	 */
	public String add(T data,long ouvertime);
	
	public boolean remove(String expireKey, T data);
}



