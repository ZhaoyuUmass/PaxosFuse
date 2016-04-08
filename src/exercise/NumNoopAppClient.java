package exercise;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

import org.json.JSONException;

import edu.umass.cs.gigapaxos.PaxosConfig;
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
	
	static int TOTAL_REQUEST = 0;
	synchronized static void updateTotalRequest(){
		TOTAL_REQUEST++;
	}
	synchronized static int getTotalRequest(){
		return TOTAL_REQUEST;
	}
	
	/**
	 * @throws IOException
	 */
	public NumNoopAppClient() throws IOException {
		super();
	}
	
	private static class Callback implements RequestCallback{

		@Override
		public void handleResponse(Request request) {
			System.out.println(request);
			NumNoopAppClient.updateTotalRequest();
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
		
		/*
		for (String activeName:PaxosConfig.getActives().keySet()) {
			System.out.println("Active: "+activeName);
			client.sendRequest(new AppRequest(name, "1",
				AppRequest.PacketType.DEFAULT_APP_REQUEST, false), 
				PaxosConfig.getActives().get(activeName), 
				new Callback());
		}
		*/
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
