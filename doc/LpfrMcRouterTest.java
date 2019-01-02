package doc;
import test.*;
import core.Coord;
import core.Message;
import core.NetworkInterface;
import routing.LpfrMcRouter;
import routing.MessageRouter;
import routing.SprayAndWaitRouter;


public class LpfrMcRouterTest  extends AbstractRouterTest  {
	private static int NR_OF_COPIES = 4;
	private LpfrMcRouter r0, r1, r2, r3, r4;
	private Message m1;
	
	@Override
	protected void setUp() throws Exception {
		ts.setNameSpace(null);
		ts.putSetting(MessageRouter.B_SIZE_S,""+ BUFFER_SIZE);
		ts.putSetting(NetworkInterface.TRANSMIT_RANGE_S, "100.0");
		ts.putSetting(NetworkInterface.TRANSMIT_SPEED_S, "1");
		setRouterProto(new LpfrMcRouter(ts));
		super.setUp();
		m1 = new Message(h0, h4, msgId1, 1);
		r0 = (LpfrMcRouter) h0.getRouter();
		r1 = (LpfrMcRouter) h1.getRouter();
		r2 = (LpfrMcRouter) h2.getRouter(); 
		r3 = (LpfrMcRouter) h3.getRouter();
		r4 = (LpfrMcRouter) h4.getRouter();
	}
	
	public void testDirectDelivery() {
		h0.createNewMessage(m1);
		checkCreates(1);
		h0.forceConnection(h3, h0.getInterfaces().get(0).getInterfaceType(), true);
		advanceWorld(1);
		//System.out.println(mc.next());
		assertTrue(mc.next());
		assertEquals(mc.getLastType(), mc.TYPE_START);
		assertEquals(mc.getLastFrom(), h0);
		assertEquals(mc.getLastTo(), h3);
		//this triggers our algorithm to start the transfer, where our algorithm has to do the work
	
		advanceWorld(1);		
		assertTrue(mc.next());
		assertEquals(mc.getLastType(), mc.TYPE_RELAY);
		assertEquals(mc.getLastTo(), h3);
		
	}
	
	private void advanceWorld(int seconds) {
		clock.advance(seconds);
		updateAllNodes();
	}
	
}
