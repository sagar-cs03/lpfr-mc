package test;
import core.Coord;
import core.Message;
import routing.MessageRouter;
import routing.SprayAndWaitRouter;

public class SprayAndWaitRouterTest extends AbstractRouterTest {
	private static int NR_OF_COPIES = 4;
	private static boolean BINARY_MODE = true;
	
	private SprayAndWaitRouter r0, r1, r2;
	private Message m1;
	
	private static final String MSG_COUNT_PROPERTY = SprayAndWaitRouter.MSG_COUNT_PROPERTY;

	@Override
	protected void setUp() throws Exception {
		ts.setNameSpace(null);
		ts.putSetting(MessageRouter.B_SIZE_S,""+ BUFFER_SIZE);
		ts.putSetting(SprayAndWaitRouter.SPRAYANDWAIT_NS + "." + SprayAndWaitRouter.NROF_COPIES, NR_OF_COPIES + "");
		ts.putSetting(SprayAndWaitRouter.SPRAYANDWAIT_NS + "." + SprayAndWaitRouter.BINARY_MODE, ""+ BINARY_MODE);
		setRouterProto(new SprayAndWaitRouter(ts));
		super.setUp();
		
		m1 = new Message(h0, h3, msgId1, 1);
		r0 = (SprayAndWaitRouter) h0.getRouter();
		r1 = (SprayAndWaitRouter) h1.getRouter();
		r2 = (SprayAndWaitRouter) h2.getRouter(); 
	}
	
	public void testDirectDelivery() {
		
		h0.createNewMessage(m1);
		
		checkCreates(1);
		
		h0.forceConnection(h3, h0.getInterfaces().get(0).getInterfaceType(), true);
		
		advanceWorld(1);
	
		
		assertTrue(mc.next());
		
		assertEquals(mc.getLastType(), mc.TYPE_START);
		assertEquals(mc.getLastFrom(), h0);
		assertEquals(mc.getLastTo(), h3);
		
		advanceWorld(1);
		
		assertTrue(mc.next());
		
		assertEquals(mc.getLastType(), mc.TYPE_RELAY);
		assertEquals(mc.getLastFrom(), h0);
		assertEquals(mc.getLastTo(), h3);	
		assertTrue(mc.getLastFirstDelivery());
		
		//assertTrue(mc.getLastFirstDelivery());
		
	}
	
	public void findAngles() {
		Coord c1 = h0.getLocation();
		Coord c2 = h1.getLocation();
		Coord c3 = h2.getLocation();
		double dist1 = c1.distance(c2); 
		double angle1 = c1.angle(c2) * (180 / Math.PI);
		double angle2 = c1.angle(c3) * (180 / Math.PI);
		double alpha = Math.abs(angle1 - angle2);
		double beta = ( 90 - (2 * alpha)) / 2 ;
	}
	
	public void testRouting() {
		h0.setLocation(new Coord(1.0, 0.0));
		h1.setLocation(new Coord(2.0, 0.5));
		h2.setLocation(new Coord(3.0, 0.0));
		h3.setLocation(new Coord(2.0, 1.0));
		findAngles();
		
//		System.out.println("The address is " + h0.getLocation() + " secondn address " + h1.getLocation());		
//		h0.createNewMessage(m1);
//		checkCreates(1);
//		updateAllNodes();
//		
//		// Initially h1 has NROF_COPIES copies of m1
//		assertEquals(m1.getProperty(MSG_COUNT_PROPERTY),NR_OF_COPIES);
//		
//		//t1
//		//contact between h0 and h1
//		h0.forceConnection(h1, h0.getInterfaces().get(0).getInterfaceType(), true);
//		
//		advanceWorld(1);
//		
//		assertTrue(mc.next());
//		assertEquals(mc.getLastType(), mc.TYPE_START);
//		advanceWorld(1);
//		
//		assertTrue(mc.next());
//		assertEquals(mc.getLastType(), mc.TYPE_RELAY);
//		assertEquals(mc.getLastFrom(), h0);
//		assertEquals(mc.getLastTo(), h1);
//		h0.forceConnection(h1, h0.getInterfaces().get(0).getInterfaceType(), false);
//		advanceWorld(1);
//		
//		Message m2 = (Message) r0.getMessageCollection().toArray()[0];
//		assertEquals(m2.getProperty(MSG_COUNT_PROPERTY), NR_OF_COPIES/2);
//		m2 = (Message) r1.getMessageCollection().toArray()[0];
//		assertEquals(m2.getProperty(MSG_COUNT_PROPERTY), NR_OF_COPIES/2);
//		
//		
//		//t2
//		//contact between h1 and h2
//		h1.forceConnection(h2, h1.getInterfaces().get(0).getInterfaceType(), true);
//		advanceWorld(1);
//		System.out.println(mc.getLastType() + "is the type");
//		//System.out.println(mc.next());
//		assertTrue(mc.next());
//		System.out.println(mc.getLastType() + "is the type");
//		assertEquals(mc.getLastType(), mc.TYPE_START);
//		advanceWorld(1);
//		
//		assertTrue(mc.next());
//		assertEquals(mc.getLastType(), mc.TYPE_RELAY);
//		assertEquals(mc.getLastFrom(), h1);
//		assertEquals(mc.getLastTo(), h2);
//		
//		//contact between h1 and h2 terminates
//		h1.forceConnection(h2, h1.getInterfaces().get(0).getInterfaceType(), false);
//		advanceWorld(1);
//		
//		//t3
//		//contact between h1 and h4
//		h1.forceConnection(h4, h1.getInterfaces().get(0).getInterfaceType(), true);
//		advanceWorld(1);
//		
//		//there is no message transfer event since h1 has only single copy of m1
//		assertFalse(mc.next());
//		h1.forceConnection(h4, h1.getInterfaces().get(0).getInterfaceType(), false);
//		advanceWorld(1);
//		
//		//t4
//		//contact between h1 and h3, final delivery happens here
//		h1.forceConnection(h3, h1.getInterfaces().get(0).getInterfaceType(), true);
//		advanceWorld(1);
//		
//		assertTrue(mc.next());
//		assertEquals(mc.getLastType(), mc.TYPE_START);
//		advanceWorld(1);
//		
//		assertTrue(mc.next());
//		assertEquals(mc.getLastType(), mc.TYPE_RELAY);
//		assertEquals(mc.getLastFrom(), h1);
//		assertEquals(mc.getLastTo(), h3);
//		assertTrue(mc.getLastFirstDelivery());
//		
//		//contact between h1 and h3 terminates
//		h1.forceConnection(h3, h1.getInterfaces().get(0).getInterfaceType(), false);
//		advanceWorld(1);
		
}

	
	public void testNonBinaryReplication() throws Exception {
		ts.setNameSpace(null);
		ts.putSetting(MessageRouter.B_SIZE_S,""+ BUFFER_SIZE);
		ts.putSetting(SprayAndWaitRouter.SPRAYANDWAIT_NS + "." + SprayAndWaitRouter.NROF_COPIES, NR_OF_COPIES + "");
		ts.putSetting(SprayAndWaitRouter.SPRAYANDWAIT_NS + "." + SprayAndWaitRouter.BINARY_MODE, "false");
		setRouterProto(new SprayAndWaitRouter(ts));
		super.setUp();
		//just establish connection->transfer-> make sure no_of_copies - 1 in source and 1 at destination
	}
	
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		//super.tearDown();
	}
	
	
	private void advanceWorld(int seconds) {
		clock.advance(seconds);
		updateAllNodes();
	}
	
}
