package com.blueline.flowprocess.components.service.storage.api;
public interface IMapAndExceedStorage<K,V> {
	public void setDefaultInterval(long interval);
	public V put(K key, V value);
	public V put(K key, V value, long overtime);
	public V get(K key);
	public V remove(K key);
	public boolean containsKey(K key);
}
