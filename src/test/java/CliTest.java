import org.test.Cli;

public class CliTest {

	public static void main(String[] args) throws Exception {
		
		String line = "--numResponders 1 --numMsgs 10000 --interval 2";

		Cli.main(line.split("\\s+"));

	}

}
