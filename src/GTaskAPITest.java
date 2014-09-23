import java.io.IOException;

/**
 * This class is used to test the connector to Google Task API.
 * 
 * @author Michelle Tan
 */

public class GTaskAPITest {

	public static void main(String[] args) {
		GoogleTaskConnector gtc = new GoogleTaskConnector();

		//System.out.println(gtc.addTask("Task 1", null, null));
		System.out.println(gtc.getAllTasks());

	}
}
