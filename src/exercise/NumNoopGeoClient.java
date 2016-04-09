package exercise;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONException;

import edu.umass.cs.gigapaxos.PaxosConfig;
import edu.umass.cs.gigapaxos.interfaces.Request;
import edu.umass.cs.gigapaxos.interfaces.RequestCallback;
import edu.umass.cs.nio.interfaces.IntegerPacketType;
import edu.umass.cs.reconfiguration.ReconfigurableAppClientAsync;
import edu.umass.cs.reconfiguration.examples.AppRequest;
import edu.umass.cs.reconfiguration.examples.noopsimple.NoopApp;
import edu.umass.cs.reconfiguration.reconfigurationutils.RequestParseException;

/**
 * @author gaozy
 *
 */
public class NumNoopGeoClient extends ReconfigurableAppClientAsync{
	private static int NUM_REQ = 0;
	private static String HOST_REGION = null;
	private static String HOST_NAME = null;
	private final static String namePrefix = "some_name";
	private final static String serviceName = namePrefix+0;
	private static NumNoopGeoClient client;
	
	static long totalLatency = 0;
	static int received = 0;
	static synchronized void updateLatency(long latency){
		totalLatency += latency;
		received++;
	}
	
	private static class Callback implements RequestCallback{
		
		private long initTime;
		
		Callback(long initTime){
			this.initTime = initTime;
		}
		
		@Override
		public void handleResponse(Request request) {
			long eclapsed = System.currentTimeMillis() - this.initTime;
			updateLatency(eclapsed);
			System.out.println("Latency of request"+received +":"+eclapsed+"ms");
			if(received < NUM_REQ){
				sendRequest();
			}
		}
		
	}
	
	private static void sendRequest(){
		try {
			System.out.println("Send request "+received);
			client.sendRequest(new AppRequest(serviceName, HOST_NAME,
					AppRequest.PacketType.DEFAULT_APP_REQUEST, false)
					, new Callback(System.currentTimeMillis()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @throws IOException
	 */
	public NumNoopGeoClient() throws IOException {
		super();
	}

	public static void main(String[] args) throws IOException, InterruptedException{
		//FIXME: remove this hard-coded part
		String[] hosts = {"california", "virginia", "ireland", "sydney", "tokyo"};
		HashMap<String, String> hostToName = new HashMap<String, String>();
		
		assert(hosts.length == PaxosConfig.getActives().keySet().size());
		
		int i = 0;
		for(String name:PaxosConfig.getActives().keySet()){
			hostToName.put(hosts[i], name);
			i++;
		}
		
		if(args.length != 2){
			System.out.println("Please enter the parameters as: #req host_region");
			System.exit(0);
		}else{
			try{
				NUM_REQ = Integer.parseInt(args[0]);
			}catch(Exception e){
				System.out.println("Please enter the parameter number of request as an integer.");
				System.exit(0);
			}
			HOST_REGION = args[1].toLowerCase();
			if(!hostToName.containsKey(HOST_REGION)){
				System.out.println("Please make sure the parameter host region is one of "+hosts);
				System.exit(0);
			}
		}
		
		HOST_NAME = hostToName.get(HOST_REGION);
		assert(HOST_NAME != null);
		
		client = new NumNoopGeoClient();
		
		if (NUM_REQ > 0){
			sendRequest();
		} else{
			System.out.println("You send no requests.");
			System.exit(0);
		}
		
		while(received < NUM_REQ){
			Thread.sleep(1000);
		}
		
		System.out.println("Sent "+NUM_REQ+" requests, and received "+received+" requests. The average latency is "+totalLatency/received+"ms");
		System.exit(0);
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
	
}
