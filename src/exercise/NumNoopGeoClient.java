package exercise;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
	private final static int NUM_THREAD = 10;
	private final static int TIMEOUT = 1000;
	
	private static NumNoopGeoClient client;	
	private static ThreadPoolExecutor executorPool = new ThreadPoolExecutor(NUM_THREAD, NUM_THREAD, 0, TimeUnit.SECONDS, 
    		new LinkedBlockingQueue<Runnable>(), new MyThreadFactory());
	
	static long totalLatency = 0;
	static int received = 0;
	static int timeout = 0;
	static synchronized void updateLatency(long latency){
		totalLatency += latency;
	}
	
	 static class MyThreadFactory implements ThreadFactory {
	    	
	    	public Thread newThread(Runnable r) {
	    		Thread t = new Thread(r);
	    	    return t;
	    	}
	    }
	
	protected static class Callback implements RequestCallback{
		
		private long initTime;
		private Object obj;
		
		Callback(long initTime, Object obj){
			this.initTime = initTime;
			this.obj = obj;
		}
		
		@Override
		public void handleResponse(Request request) {
			long eclapsed = System.currentTimeMillis() - this.initTime;
			updateLatency(eclapsed);
			System.out.println("Latency of request"+received +":"+eclapsed+"ms");
			if(request.getRequestType() != AppRequest.PacketType.DEFAULT_APP_REQUEST){
				System.out.println(request);
			}
			synchronized(obj){
				obj.notify();
			}
		}
		
	}
	
	private static class requestRunnable implements Callable<Boolean>{
		Object obj = new Object();
		
		@Override
		public Boolean call() throws Exception {
			try {
				System.out.println("Send request "+received);
				client.sendRequest(new AppRequest(serviceName, HOST_NAME,
						AppRequest.PacketType.DEFAULT_APP_REQUEST, false)
						, new Callback(System.currentTimeMillis(), obj));
			} catch (IOException e) {
				e.printStackTrace();
			}
			synchronized(obj){
				obj.wait();
			}
			return true;
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
		executorPool.prestartAllCoreThreads();
		
		while (received < NUM_REQ){
			Future<Boolean> future = executorPool.submit(new requestRunnable());
			try {
				boolean rcvd = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
				if(rcvd){
					received++;
				}
			} catch (ExecutionException | TimeoutException e) {
				received++;
				timeout++;
				e.printStackTrace();
			}
		} 
		
		System.out.println("Sent "+NUM_REQ+" requests, received "+(received-timeout)+" requests and "+ timeout + " requests timed out"
				+ ". The average latency is "+totalLatency/received+"ms");
		
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
