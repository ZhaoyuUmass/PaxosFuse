package paxosfuse;

import java.util.Set;

import edu.umass.cs.gigapaxos.interfaces.Replicable;
import edu.umass.cs.gigapaxos.interfaces.Request;
import edu.umass.cs.nio.interfaces.IntegerPacketType;
import edu.umass.cs.reconfiguration.reconfigurationutils.RequestParseException;

/**
 * @author gaozy
 *
 */
public class PaxosFuse implements Replicable {

	@Override
	public boolean execute(Request arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Request getRequest(String arg0) throws RequestParseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<IntegerPacketType> getRequestTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String checkpoint(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean execute(Request arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean restore(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
