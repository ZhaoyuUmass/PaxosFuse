package exercise;

import java.net.InetAddress;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.gigapaxos.interfaces.Request;
import edu.umass.cs.reconfiguration.reconfigurationutils.AbstractDemandProfile;
import edu.umass.cs.reconfiguration.reconfigurationutils.InterfaceGetActiveIPs;

/**
 * @author gaozy
 *
 */
public class NumNoopPolicy extends AbstractDemandProfile{
	private final static int REPORT_EVERY_FEW_REQUEST = 1;
	private final static String SERVICE_NAME = "service_name";
	private final static String NUM_REQUEST = "num_request";
	
	private Integer numReq = 0;
	private NumNoopPolicy lastReconfiguredProfile = null;
	
	/**
	 * @param name
	 */
	public NumNoopPolicy(String name) {
		super(name);
	}
	
	/**
	 * @param nnp
	 */
	public NumNoopPolicy(NumNoopPolicy nnp) {
		super(nnp.name);
		this.numReq = nnp.numReq;
	}

	@Override
	public NumNoopPolicy clone() {
		return new NumNoopPolicy(this);
	}

	@Override
	public void combine(AbstractDemandProfile dp) {
		System.out.println(dp.getName());
		NumNoopPolicy update = (NumNoopPolicy) dp;
		System.out.println(update.getName()+" "+update.numReq);
		this.numReq = Math.max(update.numReq, this.numReq);
		System.out.println("The largest #reqs is "+this.numReq);
	}

	/**
	 * @param json
	 * @throws JSONException
	 */
	public NumNoopPolicy(JSONObject json) throws JSONException {
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
		System.out.println(request);
	}

	@Override
	public void reset() {
		numReq = 0;
	}

	@Override
	public ArrayList<InetAddress> shouldReconfigure(ArrayList<InetAddress> arg0, InterfaceGetActiveIPs arg1) {
		System.out.println("Ready to reconfigure!");
		return null;
	}

	@Override
	public boolean shouldReport() {
		
		numReq++;
		if(numReq % REPORT_EVERY_FEW_REQUEST == 0){
			System.out.println(">>>>>>>>>>>>>>>>> Ready to report");
			return true;
		}
		return false;		
		
	}

}
