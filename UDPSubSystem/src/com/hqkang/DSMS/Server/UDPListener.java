package com.hqkang.DSMS.Server;

import java.io.IOException;
import java.io.PipedInputStream;
import java.net.SocketException;

import com.hqkang.DSMS.Test.DuplicaManager;

public class UDPListener implements Runnable {
	private UDPServer uSrv;

	public UDPListener(UDPServer _uSrv) throws IOException {
		uSrv = _uSrv;
		

		
	}
	

	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			try {
				uSrv.listen();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
