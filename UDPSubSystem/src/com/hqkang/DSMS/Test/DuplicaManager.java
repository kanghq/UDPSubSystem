package com.hqkang.DSMS.Test;

import java.util.HashMap;

import com.hqkang.DSMS.Server.ServerInterface;

public class DuplicaManager implements ServerInterface {
	String name;
	public DuplicaManager (String _name) {
		this.name = _name;
	}
	@Override
	public HashMap<String, String> getGroupMember() {
		// TODO Auto-generated method stub
		HashMap<String, String> gm = new HashMap<String, String>();
		gm.put("mtl", "127.0.0.1:9991");
		gm.put("lvl", "127.0.0.1:9992");
		gm.put("ddo", "127.0.0.1:9993");
		return gm;
	}

	@Override
	public String getMyServerName() {
		// TODO Auto-generated method stub
		return name;
	}

}
