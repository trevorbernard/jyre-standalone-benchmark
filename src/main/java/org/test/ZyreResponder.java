package org.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;
import org.zyre.ZreInterface;

public class ZyreResponder implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(ZyreResponder.class);
	
	private ZreInterface zre;
	private String group = "local";
	
	@Override
	public void run() {
		log.debug("responder thread starting");
		zre = new ZreInterface();
		zre.join(group);
		
		while(true) {
			ZMsg incoming = zre.recv();
		
			if (incoming == null) {// Interrupted
				log.error("Interrupted during recv()");
				break;
			}
						
			String eventType = incoming.popString();			
			
			// Respond to a shout with a whisper to sender
			if (eventType.equals("SHOUT")) {
				String requesterId = incoming.popString();
				String group = incoming.popString();
				String payload = incoming.popString();
				
				ZMsg response = new ZMsg();
				response.add(requesterId);
				response.add("response-to-" + payload);
				
				zre.whisper(response);
			} 
			else {
				//not handling other events
			}
		}
	}
}
