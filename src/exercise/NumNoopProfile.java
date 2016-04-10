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
	private final static int RECONFIGURATION_THRESHOLD = 20;
	private final static String SERVICE_NAME = "service_name";
	private final static String SHOULD_RECONFIGURE = "should_reconfigure";
	private final static String HOST = "host";
	
	private Integer numReq = 0;
	private boolean shouldReconfigue = false;
	private NumNoopProfile lastReconfiguredProfile = null;
	
	private NumNoopFakeLatency latMap = new NumNoopFakeLatency();
	private String mostActiveRegion = null;
	
	
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
		this.mostActiveRegion = nnp.mostActiveRegion;
	}

	@Override
	public NumNoopProfile clone() {
		return new NumNoopProfile(this);	
	}

	/**
	 * @param json
	 * @throws JSONException
	 */
	public NumNoopProfile(JSONObject json) throws JSONException {
		super(json.getString(SERVICE_NAME));
		this.mostActiveRegion = json.getString(HOST);
	}
	
	
	@Override
	public void combine(AbstractDemandProfile dp) {
		NumNoopProfile update = (NumNoopProfile) dp;
		if( !update.mostActiveRegion.equals(this.mostActiveRegion)){
			this.mostActiveRegion = update.mostActiveRegion;
			this.shouldReconfigue = true;
		}
		System.out.println("Coordinator combines update request "+update);	
	}


	
	@Override
	public JSONObject getStats() {
		JSONObject json = new JSONObject();
		try {
			json.put(SERVICE_NAME, this.name);
			json.put(SHOULD_RECONFIGURE, this.shouldReconfigue);
			json.put(HOST, this.mostActiveRegion);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println("getStats:"+json.toString());
		
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
		if(mostActiveRegion == null ){
			mostActiveRegion = host;
			numReq = 0;
		} else if(!mostActiveRegion.equals(host)){
			mostActiveRegion = host;
			numReq = 0;	
			
		}else{
			numReq++;
			System.out.println(this+" Recived "+numReq+" requests");
		}
		
		System.out.println("register"+request+" "+host);
		
	}

	@Override
	public void reset() {
		numReq = 0;
		mostActiveRegion = null;
	}
	
	@Override
	public ArrayList<InetAddress> shouldReconfigure(ArrayList<InetAddress> curActives, InterfaceGetActiveIPs nodeConfig) {
		if(this.shouldReconfigue){
			ArrayList<InetAddress> reconfiguredAddresses = new ArrayList<InetAddress>();
			System.out.println("The most active region is "+mostActiveRegion);
			
			ArrayList<String> names = latMap.getClosest(mostActiveRegion);
			
			System.out.println("Closest names are "+names);
			for (String name:names){
				reconfiguredAddresses.add(PaxosConfig.getActives().get(name).getAddress());
			}
			
			System.out.println("reconfigured address set is "+reconfiguredAddresses);
			
			this.shouldReconfigue = false;
			return reconfiguredAddresses;
		}else{
			return null;
		}
	}

	@Override
	public boolean shouldReport() {	
		/**
		 * Do not report on every request, report when
		 * a replica receives too many requests from a
		 * region. 
		 */
		if(numReq >= RECONFIGURATION_THRESHOLD ){
			this.shouldReconfigue = true;
			this.numReq = 0;
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
