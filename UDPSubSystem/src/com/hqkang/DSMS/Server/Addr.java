package com.hqkang.DSMS.Server;

public class Addr {
	String IP;
	int port;
	public String getIP() {
		return IP;
	}
	public boolean setIP(String _ip) {
		this.IP = _ip;
		return true;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean setPort(int _port) {
		this.port = _port;
		return true;
	}

}
