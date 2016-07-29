package com.hqkang.DSMS.Server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDPServer {
	private byte[] buffer = new byte[1024];    
    private MulticastSocket socket = null;    
    private DatagramPacket packet = null;    
    private InetSocketAddress socketAddress = null;    
    private InetAddress clientAddress;
    private String clientIP;
    private int clientPort;
    private String clientData;
    
    private HashMap<String, String> memberList;
    private HashMap<String, String> reverseMemList;
    private myVictor seqList = new myVictor();;
    private ServerInterface srv;
    private TreeMap<Long, String> sendQueue = new TreeMap<Long, String>();
    private HashMap<String, TreeMap<Long, String>> receiveQueue = new HashMap<String, TreeMap<Long, String>>();
    private LinkedList<String> deliverQueue = new LinkedList<String>();
    private String serverName;
    private String myIP;
    private int myPort;
    private long lastNACKSeq;
    InetAddress multcastAdd = InetAddress.getByName("224.0.0.5"); 
    private UDPListener listener;
    private long count=1;
    
    public UDPServer(ServerInterface srv) throws IOException {
    	init(srv);
    	Bind(myIP, myPort);
    	new Thread(listener).start();

    	System.out.println(myIP+" "+ myPort +" "+"UDP Srv Started");
    }
    private void Bind(String ip, int port) throws IOException {    
       //create socket
        socketAddress = new InetSocketAddress(ip, port);  
        // bind to local address
        //socket = new MulticastSocket(socketAddress);
        socket = new MulticastSocket(port);

        socket.joinGroup(multcastAdd);
        packet = new DatagramPacket(buffer, buffer.length);
        
    }  
    
    public final String getOrgIp() {    
        return clientIP;    
    } 
    
    private String Receive() throws IOException {
        
        socket.receive(packet);
        // get client address
        clientAddress = packet.getAddress();
        clientIP = clientAddress.getHostAddress();
        // get clien port
        clientPort = packet.getPort();
        // get data
        clientData = new String(packet.getData(), 0, packet.getLength());     
        return clientData;    
    }    
    
    public void Send(String content) throws IOException {    
        packet.setAddress(clientAddress);
        packet.setPort(clientPort);
        packet.setData(content.getBytes()); 
        
     
        socket.send(packet);    
    } 
    
    public void Send(String content, String add, int port) throws IOException {    
        packet.setAddress(InetAddress.getByName(add));
        
        packet.setPort(port);
        packet.setData(content.getBytes()); 
        
     
        socket.send(packet);    
    } 
    

    
    private boolean init(ServerInterface _srv) throws IOException {
    	this.srv = _srv;
    	this.serverName = srv.getMyServerName();
    	
    	memberList = srv.getGroupMember();
    	this.myIP = memberList.get(serverName).split(":")[0];
    	this.myPort = Integer.parseInt(memberList.get(serverName).split(":")[1]);
    	reverseMemList = getReverseMap(memberList);
    	Iterator<Entry<String, String>> it = memberList.entrySet().iterator();
    	while(it.hasNext()) {
    		seqList.put(it.next().getKey(), (long) 0);
    	}
    	initReceiveQueue();
    	listener = new UDPListener(this);
    	
    	return true;
    }
    
    private static HashMap<String, String> getReverseMap(HashMap<String, String> src) {
    	
    	HashMap<String, String> result = new HashMap<String, String>();
    	Iterator<Entry<String, String>> it = src.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<String, String> e = (Entry<String, String>) it.next();
    		result.put(e.getValue(), e.getKey());
    	}
    	return result;
    }
    
    public void RMulticast(String content) throws IOException {
    	
        sendQueue.put(seqList.get(serverName), content);
        myVictor sendSeqList = (myVictor) seqList.clone();
        sendSeqList.put(serverName, count);
        content += sendSeqList.toString();
        DatagramPacket mPkt = new DatagramPacket(buffer, buffer.length);
        mPkt.setAddress(multcastAdd);
        mPkt.setPort(myPort);
        mPkt.setData(content.getBytes());
        socket.send(mPkt);
        count++;
    	//remove send sequence
    	sendQueue = new TreeMap<Long, String>(sendQueue.tailMap(lastNACKSeq, true));
    	
    }
    
    public void listen() throws IOException {
    	
    	parsePacket();
    	Iterator<Entry<String, TreeMap<Long, String>>> it = receiveQueue.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<String, TreeMap<Long, String>> e = (Entry<String, TreeMap<Long, String>>) it.next();
    		String remoteServerName = e.getKey();
    		TreeMap<Long, String> rList = e.getValue();
    		if(!rList.isEmpty()) {
	    		Entry<Long, String> packet = rList.firstEntry();
	    		Long recordedSeq = seqList.get(remoteServerName);
	    		synchronized(deliverQueue) {
		    		while(!rList.isEmpty()&&recordedSeq+1 == packet.getKey()) {
		    			Long packetSeq = packet.getKey();
		    			String packetCnt = packet.getValue();
		    			deliverQueue.add(packetCnt);
		    			rList.remove(packetSeq);
		    			recordedSeq++;
		    			seqList.put(remoteServerName, recordedSeq);
		    			packet = rList.firstEntry();
		    			deliverQueue.notify();
	    			
		    		}
	    		}
    	}

    	}
    	
    }
    
    public String deliver() throws InterruptedException {
    	synchronized(deliverQueue) {
	    	if(deliverQueue.isEmpty()) {deliverQueue.wait();}
	    	
	    	String cnt = deliverQueue.getFirst();
	   		deliverQueue.removeFirst();
	   		return cnt;
    	}
    }
    
    
    private void parsePacket() throws IOException {

    	Receive();
    	boolean resendFlag = true;
    	resendFlag = resend();
    	if(true == resendFlag) return;
    	String arr[] = clientData.split("@");
    	if(arr.length>=2) {
	    	String vic = arr[arr.length-1];
	    	Pattern pattern = Pattern.compile("\\[(.*?)\\]");
	        Matcher matcher = pattern.matcher(vic);
	        
	    	while(matcher.find()) {
	    		String ctx = matcher.group(1);
	    		String[] ent = ctx.split(":"); //ent[0] = remoteServerName, ent[1] = sequence from packet
	    		long packetSequence = Long.parseLong(ent[1]);
	    		String remoteServerName = ent[0];
	    		if(seqList.get(remoteServerName)+1 <  packetSequence) {
	    			requestResend(seqList.get(ent[0])+1);
	    			putReceiveQueue(arr[0], Long.parseLong(ent[1]), ent[0]);
	    		}
	    		if(seqList.get(remoteServerName)+1 ==  packetSequence) {
	    			//seqList.put(ent[0], packetSequence);
	    			putReceiveQueue(arr[0], Long.parseLong(ent[1]), ent[0]);
	    		}

	    		
	    	}
    	}
    	//select the longest queue,check the sequence if continuous.
    	
    	
    }
    
    private boolean resend() throws IOException {
    	if(clientData.contains("@NACK")) {
    		String[] res = clientData.split("#");
    		String sequence = res[res.length-2];
    		lastNACKSeq = Long.parseLong(sequence);
    		String content = sendQueue.get(Long.parseLong(sequence));
    		content += "@["+serverName+":"+sequence+"]";
    		Send(content);
    		return true;
    	} else return false;
    }
    

    private boolean initReceiveQueue() {
    	Iterator<Entry<String, String>> it = memberList.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<String, String> e = (Entry<String, String>) it.next();
    		receiveQueue.put(e.getKey(), new TreeMap<Long, String>());
    	}
    	return true;
    }
    private boolean putReceiveQueue(String content, long seq, String senderName) {
    	TreeMap<Long, String> queue = receiveQueue.get(senderName);
    	queue.put(seq, content);
    	return true;
    	
    	
    }
    
    private void requestResend(long l) throws IOException {
		// TODO Auto-generated method stub
    	Send("@NACK#"+l+"#");
    	
		
	}
	public void close() {    
        try 
        {    
            socket.close();    
        } 
        catch (Exception ex) 
        {    
            ex.printStackTrace();    
        }    
    }    
    
    
    
    

}
