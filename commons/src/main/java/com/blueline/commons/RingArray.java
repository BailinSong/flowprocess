package com.blueline.commons;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
public class RingArray<E> implements Serializable {
	AtomicInteger index = new AtomicInteger();
	CopyOnWriteArrayList<E> list = new CopyOnWriteArrayList<E>();
	ReadWriteLock rwl = new ReentrantReadWriteLock();
	private final int incrementAndGet(int upperBound) {
		if (index.compareAndSet(upperBound - 1, 0)) {
			return 0;
		} else {
			return index.incrementAndGet();
		}
	}
	public boolean add(E e) {
		if (list.contains(e)) {
			return true;
		} else {
			rwl.writeLock().lock();
			boolean ret = list.add(e);
			rwl.writeLock().unlock();
			return ret;
		}
	}
	public E next() {
		E item = null;
		do {
			rwl.readLock().lock();
			try {
				int item_index = incrementAndGet(list.size());
				item = list.get(item_index);
			} catch (ArrayIndexOutOfBoundsException e) {
				index.set(0);
			} finally {
				rwl.readLock().unlock();
			}
		} while (item == null);
		return item;
	}
	public boolean remove(E e) {
		rwl.writeLock().lock();
		boolean ret = list.remove(e);
		rwl.writeLock().unlock();
		return ret;
	}
	public boolean contains(E e) {
		rwl.readLock().lock();
		boolean ret = list.contains(e);
		rwl.readLock().unlock();
		return ret;
	}
	public void fill(Collection<E> e) {
		CopyOnWriteArrayList<E> temp = new CopyOnWriteArrayList<E>(e);
		rwl.writeLock().lock();
		list = temp;
		rwl.writeLock().unlock();
	}
	public int size() {
		int size = list.size();
		return size;
	}
	public static void main(String[] args) {
		final RingArray<String> array = new RingArray<String>();
		Thread t = new Thread(new Runnable() {
			int i = 0;
			public void run() {
				while (true) {
					array.add((i++) + " item");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.setDaemon(true);
		t.start();
		array.add("begin");
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			System.out.println(array.next());
		}
	}
}
