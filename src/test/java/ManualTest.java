import org.test.ZyreRequester;
import org.test.ZyreResponder;

public class ManualTest {
	private static int numResponders = 10;
	private static int numMsgs = 1000;
	private static int intervalMs = 100;

	public static void main(String[] args) throws Exception {
		
		for (int i=0; i < numResponders; i++) {
			ZyreResponder responder = new ZyreResponder();
			new Thread(responder).start();
		}
		
		ZyreRequester requester = new ZyreRequester(intervalMs, numMsgs, numResponders);
		requester.start();

	}
}
