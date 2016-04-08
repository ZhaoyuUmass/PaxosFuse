package exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import edu.umass.cs.gigapaxos.PaxosConfig;

/**
 * @author gaozy
 *
 */
public class NumNoopFakeLatency {
	private static HashMap<String, Integer> latencies = new HashMap<String, Integer>();
	private Random rand = new Random();
	
	private final static int MAX_LATENCY = 100;
	
	protected NumNoopFakeLatency(){
		reset();
	}
	
	protected ArrayList<String> getTopK(int k){
		ArrayList<String> names = new ArrayList<String>();
		int thres = MAX_LATENCY;
		for(String name:NumNoopFakeLatency.latencies.keySet()){
			if(names.size()<k){
				int pos = 0;
				for (String m:names){
					if(latencies.get(m) > latencies.get(name)){
						break;
					}
					pos++;
				}
				names.add(pos, name);
				int lat = latencies.get(name);
				if(lat < thres){
					thres = lat;
				}
			}else{
				int lat = latencies.get(name);
				if(lat >= thres){
					continue;
				} else{
					// pop one name out, and insert this one
					int pos = 0;
					for(String m:names){						
						if (latencies.get(m) > latencies.get(name) ){
							break;
						}
						pos++;
					}
					names.remove(names.size()-1);					
					names.add(pos, name);
					
				}
			}
			System.out.println("getTopK:"+names+" "+name);
		}
		return names;
	}
	
	
	protected void reset(){
		latencies.clear();
		for (String name:PaxosConfig.getActives().keySet()){
			latencies.put(name, rand.nextInt(MAX_LATENCY));
		}
		System.out.println(""+latencies);
	}
}
