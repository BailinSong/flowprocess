package com.blueline.commons;

import java.io.Serializable;

public class SubItem implements Serializable{
	
	private static final long serialVersionUID = 1L;
	String item_name;
	long time;

	public SubItem(){};
	
	public SubItem(String item_name, long time) {
		this.item_name = item_name;
		this.time = time;
	}

	public String getItemName() {
		return item_name;
	}
	public void setItemName(String item_name) {
		this.item_name=item_name;
	}

	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time=time;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SubItem){
			SubItem temp=(SubItem)obj;
			if(temp.getItemName().equals(item_name)){
				return true;
				
			}else{
				return false;
			}
		}else{
			return false;
		}

	}
	
	@Override
	public String toString() {

		return "{item_name:\""+item_name+"\",time:"+time+"}";
	}
}
