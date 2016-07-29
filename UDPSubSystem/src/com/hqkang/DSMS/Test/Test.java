package com.hqkang.DSMS.Test;

import java.io.IOException;

import com.hqkang.DSMS.Server.UDPServer;

public class Test {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		DuplicaManager mtl = new DuplicaManager("mtl");
		UDPServer uMTL = new UDPServer(mtl);
		uMTL.RMulticast("BBBR");
		
		//String t = uMTL.deliver();
		
		//System.out.println(t);
		
		//new Thread(new TestReceiver(uMTL)).start();

		
		uMTL.RMulticast("NACK#1#");
		String t = uMTL.deliver();
		
		System.out.println(t);
		String w = uMTL.deliver();
		
	}

}
