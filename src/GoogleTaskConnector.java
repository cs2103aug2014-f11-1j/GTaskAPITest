import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.*;

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

	  private static final String[] TASKS_SCOPES = {"https://www.googleapis.com/auth/tasks"};
	  
	  private Tasks _service;

	public GoogleTaskConnector() {
		try {
			setUp();
		} catch (IOException e) {
			System.out.println("Unable to login.");
		}
	}

	public void setUp() throws IOException {
	    HttpTransport httpTransport = new NetHttpTransport();
	    JsonFactory jsonFactory = new JacksonFactory();
	   
	    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	        httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(TASKS_SCOPES))
	        .setAccessType("online")
	        .setApprovalPrompt("auto").build();
	    
	    String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
	    System.out.println("Please open the following URL in your browser then type the authorization code:");
	    System.out.println("  " + url);
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    String code = br.readLine();
	    
	    GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
	    GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);
	    
	    _service = new Tasks.Builder(httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
	}
}
