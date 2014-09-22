import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;

/**
 * This class is used to connect to Google Task API.
 * 
 * To use this class, the user has to provide the
 * details of their Google account and sign in.
 * This class can create, read, update or delete tasks
 * for the given Google account.
 * 
 * @author Michelle Tan
 */
public class GoogleTaskConnector {

	private static final String CLIENT_ID = "1009064713944-qqeb136ojidkjv4usaog806gcafu5dmn.apps.googleusercontent.com";
	private static final String CLIENT_SECRET = "9ILpkbnlGwVMQiqh10za3exf";
	private static final String APPLICATION_NAME = "Task Commander";

	private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

	private Tasks service;

	/**
	 * Returns a GoogleTaskConnector after trying to 
	 * connect to Google.
	 * 
	 */
	public GoogleTaskConnector() {
		try {
			setUp();
		} catch (IOException e) {
			System.out.println("Unable to login.");
		}
	}

	/**
	 * Connects to Google and initialises Tasks service.
	 * 
	 * Makes an authorisation request to Google and prints
	 * out a URL. The user has to enter the given URL into 
	 * a browser and login to Google, then paste the returned
	 * authorisation code into command line. 
	 *
	 */
	public void setUp() throws IOException {
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(TasksScopes.TASKS))
		.setAccessType("online")
		.setApprovalPrompt("auto").build();

		String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
		System.out.println("Please open the following URL in your browser then type the authorization code:");
		System.out.println("  " + url);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = br.readLine();
		br.close();
		System.out.println("oops");
		GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
		System.out.println("oops2");
		GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);
		System.out.println("oops3");

		service = new Tasks.Builder(httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
		
		Tasks.TasksOperations.List request = service.tasks().list("@default");
		List<Task> tasks = request.execute().getItems();

		for (Task task : tasks) {
		  System.out.println(task.getTitle());
		}
		System.out.println("oops4");
	}
	
}
