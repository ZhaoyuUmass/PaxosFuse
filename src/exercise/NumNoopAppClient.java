package exercise;

import java.io.IOException;
import java.util.Set;

import org.json.JSONException;

import edu.umass.cs.gigapaxos.interfaces.Request;
import edu.umass.cs.gigapaxos.interfaces.RequestCallback;
import edu.umass.cs.nio.interfaces.IntegerPacketType;
import edu.umass.cs.reconfiguration.ReconfigurableAppClientAsync;
import edu.umass.cs.reconfiguration.examples.AppRequest;
import edu.umass.cs.reconfiguration.examples.noopsimple.NoopApp;
import edu.umass.cs.reconfiguration.reconfigurationpackets.CreateServiceName;
import edu.umass.cs.reconfiguration.reconfigurationutils.RequestParseException;

/**
 * @author gaozy
 *
 */
public class NumNoopAppClient extends ReconfigurableAppClientAsync {

	public NumNoopAppClient() throws IOException {
		super();
	}
	
	private class Callback implements RequestCallback{

		@Override
		public void handleResponse(Request request) {
			System.out.println(request);
		}
		
	}
	
	private static void sendTestReqeust(NumNoopAppClient client, String name) throws IOException{
		client.sendRequest(new AppRequest(name, "1",
				AppRequest.PacketType.DEFAULT_APP_REQUEST, false), 
				new RequestCallback(){
					@Override
					public void handleResponse(Request response) {
						// TODO Auto-generated method stub
						System.out.println("The response is " +response);
					}
			
				});
			
		//client.sendRequest(request, server, callback);
		
		//client.sendRequest(request, server, callback);
	}
	
	@Override
	public Request getRequest(String stringified) throws RequestParseException {
		try {
			return NoopApp.staticGetRequest(stringified);
		} catch (RequestParseException | JSONException e) {
			// do nothing by design
		}
		return null;
	}

	@Override
	public Set<IntegerPacketType> getRequestTypes() {
		return NoopApp.staticGetRequestTypes();
	}
	
	public static void main(String[] args) throws IOException{
		final NumNoopAppClient client = new NumNoopAppClient();
		String namePrefix = "some_name";
		final String name = namePrefix+0;
		
		client.sendRequest(new CreateServiceName(name, "0"),
				new RequestCallback() {

					@Override
					public void handleResponse(Request request) {
						// TODO Auto-generated method stub
						System.out.println("Received:"+request);
						try {
							sendTestReqeust(client, name);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
		
		System.out.println("Service "+name+" has been created ...");
		
		
		
	}
}
