package org.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;
import org.zyre.ZreInterface;

public class ZyreResponder implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(ZyreResponder.class);
	
	private ZreInterface zre = null;
	private String group = "local";
	
	private int received = 0;
	private int sent = 0;
	
	@Override
	public void run() {
		log.info("responder thread starting");
		zre = new ZreInterface();
		zre.join(group);

		while(true) {
			ZMsg incoming = zre.recv();
		
			if (incoming == null) {// Interrupted
				log.error("Interrupted during recv()");
				break;
			}
						
			String eventType = incoming.popString();			
			
			if (eventType.equals("SHOUT")) {
				String requesterId = incoming.popString();
				String group = incoming.popString();
				String payload = incoming.popString();
				log.debug("peer (" + requesterId + ") shouted to group: " + group + ": " + payload);
				received++;
				
				ZMsg response = new ZMsg();
				response.add(requesterId);
				response.add("response-to-" + payload);
				
				zre.whisper(response);
				sent++;
			} 
			else {
				log.trace("not handling event: " + eventType);
			}
		}
		
	}

}
