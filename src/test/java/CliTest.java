import org.test.Cli;

public class CliTest {

	public static void main(String[] args) throws Exception {
		
		String line = "--numResponders 10 --numMsgs 100 --interval 100";
		Cli.main(line.split("\\s+"));

	}

}
