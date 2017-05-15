package com.blueline.flowprocess.components.service.storage.api;


public interface IMapStorage<K,V>
{
	public V put(K key, V value);
	public V get(K key);
	public V remove(K key);
	public boolean containsKey(K key);
}
