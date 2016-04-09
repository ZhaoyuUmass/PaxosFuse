package exercise;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.gigapaxos.PaxosConfig;
import edu.umass.cs.gigapaxos.interfaces.Request;
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
	
	private final static int NUM_REPLICAS = 3;
	
	
	
	private Integer numReq = 0;
	private NumNoopProfile lastReconfiguredProfile = null;
	
	//private NumNoopFakeLatency latencyMap = new NumNoopFakeLatency();
	
	/**
	 * @param name
	 */
	public NumNoopProfile(String name) {
		super(name);
	}
	
	/**
	 * @param nnp
	 */
	public NumNoopProfile(NumNoopProfile nnp) {
		super(nnp.name);
		this.numReq = nnp.numReq;
	}

	@Override
	public NumNoopProfile clone() {
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
	}
	
	@Override
	public JSONObject getStats() {
		JSONObject json = new JSONObject();
		try {
			json.put(SERVICE_NAME, this.name);
			json.put(NUM_REQUEST, numReq);
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
		System.out.println("register"+request+" "+sender+" "+nodeConfig);
	}

	@Override
	public void reset() {
		numReq = 0;
	}
	
	@Override
	public ArrayList<InetAddress> shouldReconfigure(ArrayList<InetAddress> curActives, InterfaceGetActiveIPs nodeConfig) {
		ArrayList<InetAddress> reconfiguredAddresses = new ArrayList<InetAddress>();
		ArrayList<String> names = null;
		
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
		if(numReq % REPORT_EVERY_FEW_REQUEST == 0){
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
