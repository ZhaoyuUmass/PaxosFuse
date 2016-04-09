package exercise;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.gigapaxos.PaxosConfig;
import edu.umass.cs.gigapaxos.interfaces.Request;
import edu.umass.cs.reconfiguration.examples.AppRequest;
import edu.umass.cs.reconfiguration.reconfigurationutils.AbstractDemandProfile;
import edu.umass.cs.reconfiguration.reconfigurationutils.InterfaceGetActiveIPs;

/**
 * @author gaozy
 *
 */
public class NumNoopProfile extends AbstractDemandProfile{
	private final static int REPORT_EVERY_FEW_REQUEST = 20;
	private final static String SERVICE_NAME = "service_name";
	private final static String NUM_REQUEST = "num_request";
		
	private Integer numReq = 0;
	private NumNoopProfile lastReconfiguredProfile = null;
	
	private NumNoopFakeLatency latMap = new NumNoopFakeLatency();
	private static String mostAcitveRegion = null;
	
	
	/**
	 * @param name
	 */
	public NumNoopProfile(String name) {
		super(name);
		latMap = new NumNoopFakeLatency();
	}
	
	/**
	 * @param nnp
	 */
	public NumNoopProfile(NumNoopProfile nnp) {
		super(nnp.name);
		this.numReq = nnp.numReq;
		latMap = new NumNoopFakeLatency();
	}

	@Override
	public NumNoopProfile clone() {
		latMap = new NumNoopFakeLatency();
		return new NumNoopProfile(this);	
	}

	@Override
	public void combine(AbstractDemandProfile dp) {
		System.out.println(dp.getName());
		NumNoopProfile update = (NumNoopProfile) dp;
		System.out.println(update.getName()+" "+update.numReq);
		this.numReq = Math.max(update.numReq, this.numReq);
		System.out.println("The largest #reqs is "+this.numReq);
	}

	/**
	 * @param json
	 * @throws JSONException
	 */
	public NumNoopProfile(JSONObject json) throws JSONException {
		super(json.getString(SERVICE_NAME));
		this.numReq =json.getInt(NUM_REQUEST);
		latMap = new NumNoopFakeLatency();
	}
	
	@Override
	public JSONObject getStats() {
		JSONObject json = new JSONObject();
		try {
			json.put(SERVICE_NAME, this.name);
			json.put(NUM_REQUEST, REPORT_EVERY_FEW_REQUEST);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(json.toString());
		return json;
	}

	@Override
	public void justReconfigured() {
		this.lastReconfiguredProfile = this.clone();		
	}

	@Override
	public void register(Request request, InetAddress sender, InterfaceGetActiveIPs nodeConfig) {
		
		AppRequest req = (AppRequest) request;
		String host = req.getValue();
		if(!mostAcitveRegion.equals(name)){
			mostAcitveRegion = host;
			numReq = 0;
		} 
		System.out.println("register"+request+" "+host);
		
	}

	@Override
	public void reset() {
		numReq = 0;
	}
	
	@Override
	public ArrayList<InetAddress> shouldReconfigure(ArrayList<InetAddress> curActives, InterfaceGetActiveIPs nodeConfig) {
		ArrayList<InetAddress> reconfiguredAddresses = new ArrayList<InetAddress>();
		System.out.println("The most active region is "+mostAcitveRegion);
		
		ArrayList<String> names = latMap.getClosest(mostAcitveRegion);
		
		System.out.println("Closest names are "+names);
		for (String name:names){
			reconfiguredAddresses.add(PaxosConfig.getActives().get(name).getAddress());
		}
		
		System.out.println("reconfigured address set is "+reconfiguredAddresses);
		
		
		return reconfiguredAddresses;
	}

	@Override
	public boolean shouldReport() {		
		numReq++;
		System.out.println(this+"Recived "+numReq+" requests");
		if(numReq >= REPORT_EVERY_FEW_REQUEST ){			
			numReq = 0;
			return true;
		}
		return false;
	}
	
	protected static List<String> asSortedList() {
		Collection<String> c = PaxosConfig.getActives().keySet();
		List<String> list = new ArrayList<String>(c);
		java.util.Collections.sort(list);
		return list;
	}
}
