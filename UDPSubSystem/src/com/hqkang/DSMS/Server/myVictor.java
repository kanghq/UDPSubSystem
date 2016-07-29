package com.hqkang.DSMS.Server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class myVictor extends HashMap<String, Long>{
	public myVictor() {
		super();
	}
	public String toString() {
		Iterator<Entry<String, Long>> it = this.entrySet().iterator();
		String val = "@";
		while(it.hasNext()) {
		Entry e = it.next();
		val += "["+e.getKey()+":"+e.getValue()+"]";
		}
		return val;
	}

}
