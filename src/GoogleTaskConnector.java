import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
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

	private static final String DATA_STORE_DIR = "credentials";
	private static final String DATA_STORE_NAME = "credentialDataStore";
	private static final String MESSAGE_EXCEPTION_IO = "Unable to read the data retrieved.";
	private static final String MESSAGE_ARGUMENTS_NULL = "Null arguments given.";

	// Option to request access type for application. Can be "online" or "offline".
	private static final String FLOW_ACCESS_TYPE = "offline";
	// Option to request approval prompt type for application. Can be "force" or "auto".
	private static final String FLOW_APPROVAL_PROMPT = "auto";
	
	private static final String USERNAME = "User";

	// Global instances
	private static Tasks service;
	private static FileDataStoreFactory dataStoreFactory;
	
	private HttpTransport httpTransport;
	private JsonFactory jsonFactory;
	private GoogleAuthorizationCodeFlow flow;
	private DataStore<StoredCredential> dataStore;

	/**
	 * Returns a GoogleTaskConnector after trying to 
	 * connect to Google.
	 * 
	 */
	public GoogleTaskConnector() {
		httpTransport = new NetHttpTransport();
		jsonFactory = new JacksonFactory();

		try {
			File dataStoreFile = new File(DATA_STORE_DIR);
			dataStoreFactory = new FileDataStoreFactory(dataStoreFile);
			dataStore = dataStoreFactory.getDataStore(DATA_STORE_NAME);
		} catch (IOException e) {
			System.out.println(MESSAGE_EXCEPTION_IO);
		}
		setUp();
	}

	/**
	 * Connects to Google and initialises Tasks service.
	 * Requests can be sent once this method is successfully
	 * executed.
	 */
	public void setUp(){
		GoogleCredential credential = getCredential();
		service = new Tasks.Builder(httpTransport, jsonFactory, credential)
		.setApplicationName(APPLICATION_NAME).build();
	}

	/**
	 * Gets a GoogleCredential for use in Google API requests,
	 * either from storage or by sending a request to Google.
	 * @return           Credential
	 */
	private GoogleCredential getCredential() {
		GoogleCredential credential = new GoogleCredential.Builder()
		.setJsonFactory(jsonFactory)
		.setTransport(httpTransport)
		.setClientSecrets(CLIENT_ID, CLIENT_SECRET)
		.addRefreshListener(new DataStoreCredentialRefreshListener(USERNAME, dataStore))
		.build();
		
		try {
			if(dataStore.containsKey(USERNAME)){
			    StoredCredential storedCredential = dataStore.get(USERNAME);
			    credential.setAccessToken(storedCredential.getAccessToken());
			    credential.setRefreshToken(storedCredential.getRefreshToken());
			}else{
			    credential.setFromTokenResponse(requestAuthorisation());
			}
			saveCredential(credential);
		} catch (IOException e) {
			System.out.println(MESSAGE_EXCEPTION_IO);
		}
		return credential;
	}

	/**
	 * Saves given credential in the datastore.
	 */
	public void saveCredential(GoogleCredential credential){
		StoredCredential storedCredential = new StoredCredential();
		storedCredential.setAccessToken(credential.getAccessToken());
		storedCredential.setRefreshToken(credential.getRefreshToken());
		try {
			dataStore.set(USERNAME, storedCredential);
		} catch (IOException e) {
			System.out.println(MESSAGE_EXCEPTION_IO);
		}
	}
	
	/**
	 * Returns a token response after requesting user
	 * login and authorisation.
	 * 
	 * Makes an authorisation request to Google and prints
	 * out a URL. The user has to enter the given URL into 
	 * a browser and login to Google, then paste the returned
	 * authorisation code into command line. 
	 */
	private GoogleTokenResponse requestAuthorisation() {
		try {
			flow = buildAuthorisationCodeFlow(httpTransport, jsonFactory, dataStoreFactory);
		} catch (IOException e) {
			System.out.println(MESSAGE_EXCEPTION_IO);
		}
		
		askUserForAuthorisationCode(flow);
		String code = getUserInput();

		return getTokenResponse(flow, code);
	}

	/**
	 * Sends a token request to get a GoogleTokenResponse.
	 * If an IOException occurs, returns null.
	 * 
	 * @param flow
	 * @param code
	 * @return      Token response
	 */
	private GoogleTokenResponse getTokenResponse(GoogleAuthorizationCodeFlow flow, String code) {
		try {
			GoogleTokenResponse response = flow.newTokenRequest(code)
					.setRedirectUri(REDIRECT_URI).execute();
			return response;
		} catch (IOException e) {
			System.out.println(MESSAGE_EXCEPTION_IO);
		}
		return null;
	}

	/**
	 * Creates the authorisation code flow needed for the authorisation URL.
	 * 
	 * @param httpTransport
	 * @param jsonFactory
	 * @param fdsf           FileDataStoreFactory
	 * @return               GoogleAuthorizationCodeFlow object
	 * @throws IOException
	 */
	private GoogleAuthorizationCodeFlow buildAuthorisationCodeFlow(
			HttpTransport httpTransport, 
			JsonFactory jsonFactory,
			FileDataStoreFactory fdsf) throws IOException {
		return new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(TasksScopes.TASKS))
		.setAccessType(FLOW_ACCESS_TYPE)
		.setApprovalPrompt(FLOW_APPROVAL_PROMPT)
		.setDataStoreFactory(fdsf).build();
	}

	/**
	 * Creates the authorisation URL, asks the user to open the URL and sign in, then type in the
	 * authorisation code from Google.
	 * @param flow
	 */
	private void askUserForAuthorisationCode(GoogleAuthorizationCodeFlow flow) {
		String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
		System.out.println("Please open the following URL in your browser then type the authorization code:");
		System.out.println("  " + url);
	}

	/**
	 * Reads user input and returns it. 
	 * @return      String of user input.
	 */
	private String getUserInput() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		try {
			input = br.readLine();
			br.close();
		} catch (IOException e) {
			System.out.println(MESSAGE_EXCEPTION_IO);
		}
		return input;
	}

	/**
	 * Prints out all tasks.
	 * @return       Feedback for user.
	 */
	public String getAllTasks() {
		try {
			Tasks.TasksOperations.List request = service.tasks().list("@default");
			List<Task> tasks = request.execute().getItems();

			String result = "";
			for (Task task : tasks) {
				result += task.getTitle() + "\n";
			}
			return result;
		} catch (IOException e) {
			return MESSAGE_EXCEPTION_IO;
		}
	}

	/**
	 * Adds task with given title, notes and date object.
	 * 
	 * @param title  Title of task.
	 * @param notes  Notes for task.
	 * @param date   DateTime object describing due date of task.
	 * @return       Feedback for user.
	 */
	public String addTask(String title, String notes, DateTime date) {
		if (title == null) {
			return MESSAGE_ARGUMENTS_NULL;
		} else {
			Task task = new Task();
			task.setTitle(title);
			if (notes != null) {
				task.setNotes(notes);
			}
			if (date != null) {
				task.setDue(date);
			}

			try {
				Tasks.TasksOperations.Insert request = service.tasks().insert("@default", task);
				Task result = request.execute();
				return result.getTitle();
			} catch (IOException e) {
				return MESSAGE_EXCEPTION_IO;
			}
		}
	}

}
