package com.hqkang.DSMS.Test;

import com.hqkang.DSMS.Server.UDPServer;

public class TestReceiver implements Runnable {
	private UDPServer srv;
	
	TestReceiver(UDPServer _srv) {
	 srv = _srv;
	}
	@Override
	public void run() {
		while(true) {
			String content = "";
			try {
				content = srv.deliver();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(content);
		}
		
		// TODO Auto-generated method stub

	}

}
