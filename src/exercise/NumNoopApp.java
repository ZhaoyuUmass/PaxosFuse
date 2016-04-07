package exercise;

import java.util.HashMap;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.gigapaxos.interfaces.ClientMessenger;
import edu.umass.cs.gigapaxos.interfaces.Replicable;
import edu.umass.cs.gigapaxos.interfaces.Request;
import edu.umass.cs.nio.interfaces.IntegerPacketType;
import edu.umass.cs.nio.interfaces.SSLMessenger;
import edu.umass.cs.reconfiguration.examples.AbstractReconfigurablePaxosApp;
import edu.umass.cs.reconfiguration.examples.AppRequest;
import edu.umass.cs.reconfiguration.examples.AppRequest.ResponseCodes;
import edu.umass.cs.reconfiguration.examples.noopsimple.NoopApp;
import edu.umass.cs.reconfiguration.interfaces.Reconfigurable;
import edu.umass.cs.reconfiguration.reconfigurationutils.RequestParseException;

/**
 * @author gaozy
 *
 */
public class NumNoopApp extends AbstractReconfigurablePaxosApp<String> 
	implements Replicable, Reconfigurable, ClientMessenger {
	
	private final HashMap<String, Integer> appData = new HashMap<String, Integer>();	
	
	
	private boolean processRequest(AppRequest request,
			boolean doNotReplyToClient) {
		if (request.getServiceName() == null)
			return true; // no-op
		if (request.isStop())
			return true;
		
		String name = request.getServiceName();
		int value = Integer.parseInt(request.getValue());
		
		if (appData.containsKey(name)){
			appData.put(name, appData.get(name)+value);
			return true;
		} 
		
		this.sendResponse(request);
		return false;
	}

	
	@Override
	public String checkpoint(String name) {
		System.out.println("checkpoint: "+name);
		return this.appData.get(name).toString();
	}

	@Override
	public boolean restore(String name, String state) {
		System.out.println("restore: "+name);
		if(state == null){
			return false;
		}
		this.appData.put(name, Integer.parseInt(state));
		return true;
	}

	@Override
	public boolean execute(Request request) {		
		return execute(request, false);
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
	
	@Override
	public boolean execute(Request request, boolean doNotReplyToClient) {
		//AppRequest req = (AppRequest) request;
		//String name = req.getServiceName();
		if (request.toString().equals(Request.NO_OP)){
			return true;
		}
		
		
		switch ((AppRequest.PacketType) (request.getRequestType())) {
		case DEFAULT_APP_REQUEST:
			return processRequest((AppRequest) request, doNotReplyToClient);
		default:
			break;
		}		
		
		return false;
	}
	
	
	private String myID;
	
	@Override
	public void setClientMessenger(SSLMessenger<?, JSONObject> msgr) {
		this.myID = msgr.getMyID().toString();		
	}	
	
	public String toString(){
		return this.getClass().getSimpleName()+myID;
	}
	
	private void sendResponse(AppRequest request) {
		// set to whatever response value is appropriate
		request.setResponse(ResponseCodes.ACK.toString() );
	}
}
