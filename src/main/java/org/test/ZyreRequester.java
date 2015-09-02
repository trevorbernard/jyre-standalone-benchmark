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

		// thread for requester to receive responses 
		Thread listenerThread = new Thread(new Listener());
		listenerThread.start();
		
		// Wait for all expected peers to join before sending
		while(numPeers < numResponders) {
			log.info("waiting for peers to join. so far we have: " + numPeers + " out of " + numResponders);
			try { Thread.sleep(200); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		log.info("all " + numPeers + " peers joined");
		
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
		log.info("done sending");
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
			while(true) {
				ZMsg incoming = zre.recv();
			
				if (incoming == null) {// Interrupted
					log.error("Interrupted during recv()");
					break;
				}
							
				String eventType = incoming.popString();			
				
				// responder messages are received here
				if (eventType.equals("WHISPER")) {
					received++;
				} 
				// A device joins a group
				else if (eventType.equals("JOIN")) {
					numPeers++;
				} 
				else {
					//not handling other events
				}
			}
		}
	}
}
