package org.test;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;
import org.zyre.ZreInterface;

public class ZyreRequester {
	
	private static final Logger log = LoggerFactory.getLogger(ZyreRequester.class);
	
	private String group = "local";
	
	private ZreInterface zre = null;
	
	private long sent = 0;
	private long received = 0;
	
	private int interval;
	private int numMsgs;
	private int numResponders;
	
	private boolean done = false;
	
	private Timer timer = new Timer();
	
	private int numPeers = 0;

	public ZyreRequester(int interval, int numMsgs, int numResponders) {
		this.interval = interval;
		this.numMsgs  = numMsgs;
		this.numResponders = numResponders;
	}

	public void start() {
		zre = new ZreInterface();
		zre.join(group);		

		log.info("starting requester listener thread");
		Thread listenerThread = new Thread(new Listener());
		listenerThread.start();
		

		while(numPeers < numResponders) {
			log.info("waiting for peers to join. so far we have: " + numPeers + " out of " + numResponders);
			try { Thread.sleep(200); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		// Timer reports on number of messages sent/received so far
		timer.scheduleAtFixedRate(new TimerTask() {
			  @Override
			  public void run() {
				  log.info("sent: " + sent + " received: " + received);
			  }
		}, 2000, 2000);
		
		send();
	}
	
	/**
	 * Send numMsgs shouts
	 */
	private void send() {
		received = 0;
		
		log.info("starting send to " + numPeers + " peers");
		
		for (sent=0; sent < numMsgs; sent++) {
			String text = "request-payload-" + sent;
			ZMsg outgoing = new ZMsg();
			outgoing.add(group);
			outgoing.add(text);
			zre.shout(outgoing);
			
			if (interval > 0) {
				try { Thread.sleep(interval); } 
				catch (InterruptedException e) { e.printStackTrace(); }
			}
		}
		
		// Done sending. Now wait for a while for responders to finish replying
		
		int expected = numMsgs * numResponders;
		timer.cancel();
		
		int waited = 0;
		while (received < expected) {
			log.info("waiting for remaining responses.  received: " + received + " expected: " + expected);
			try { Thread.sleep(2000); } 
			catch (InterruptedException e) { e.printStackTrace();}

			if (++waited >= 3) { 
				log.info("timed out waiting for responses");
				break; 
			}
		}
		
		long percentage = Math.round( (double)received/(double)expected * 100 );
		log.info("sent: " + sent + " expected: " + expected + " received: " + received + " (" + percentage + "%)");
		System.exit(0);
	}
	
	private class Listener implements Runnable {

		@Override
		public void run() {
			while(!done) {
				ZMsg incoming = zre.recv();
			
				if (incoming == null) {// Interrupted
					log.error("Interrupted during recv()");
					break;
				}
							
				String eventType = incoming.popString();			
				
				// A Zyre-enabled device enters the network
				if (eventType.equals("ENTER")) {
					String zyreDeviceId = incoming.popString();
					log.debug("peer (" + zyreDeviceId + ") entered network");
				} 
				// responder messages are received here
				else if (eventType.equals("WHISPER")) {
					String zyreDeviceId = incoming.popString();
					String serializedMsg = incoming.popString();
					log.debug("peer (" + zyreDeviceId + ") responded: " + serializedMsg);
					received++;
				} 
				// A device joins a group
				else if (eventType.equals("JOIN")) {
					String zyreDeviceId = incoming.popString();
					String group = incoming.popString();
					numPeers++;
					log.debug("peer (" + zyreDeviceId + ") joined: " + group);
				} 
				// A device explicitly leaves a group
				else if (eventType.equals("LEAVE")) {
					String zyreDeviceId = incoming.popString();
					String group = incoming.popString();
					log.debug("peer (" + zyreDeviceId + ") left " + group);					
				}
				// A device exits the network
				else if (eventType.equals("EXIT")) {
					String zyreDeviceId = incoming.popString();
					log.debug("peer (" + zyreDeviceId + ") exited");
				}
				else {
					log.warn("unexpected event: " + eventType);
				}
				
			}
			log.info("listener thread stopped");
		}
		
	}
}
