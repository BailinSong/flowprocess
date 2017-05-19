package com.blueline.flowprocess.components.service.storage.api;
public interface IExceedStorage <T>{
	public void setDefaultInterval(long interval);
	public void regHandler(IHandler<T> handler);
	public String add(T data);
	public String add(T data,long ouvertime);
	public boolean remove(String expireKey, T data);
}
