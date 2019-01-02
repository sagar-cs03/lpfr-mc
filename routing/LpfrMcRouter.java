
package routing ; 

import core.Settings;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import routing.util.RoutingInfo;
import util.Tuple;
import core.Connection;
import core.Coord;
import core.DTNHost;
import core.Message;

public class LpfrMcRouter extends ActiveRouter {

    
    public static final String LPFR_MC_NS = "LpfrMcRouter" ;                                                                                                                                                                                                                                                                           

    //defines initial probability vector for each node
    private double[] initialProbabilityVector = {0.95, 0.1, 0.1, 0.1, 0.1};
    private double[][] tpm = new double[5][5];
    private double[] tpmSNext = new double[5];
    
    
    private Map<DTNHost, double[]> ipvN;
    private Map<DTNHost, double[][]> tpmN;
    private Map<DTNHost, double[]> tpmNnext;
    private Map<DTNHost, Map<String, Double> > mdpN ;
    private Map<String, Double> mdp;
    private WeightMatrix w;
    

    /**
     * Constructor based on Settings s object
     */
    public  LpfrMcRouter(Settings s) {
        super(s);
        Settings lpfrsettings = new Settings(LPFR_MC_NS);
        //TODO: we need to use the setting object to do some initialization for the object, which we will see later
        initPreds();
    }

    /**
     * Copy Constructor 
     */
    protected LpfrMcRouter(LpfrMcRouter r) {
        super(r);
        initPreds();
    }
    

    /**
     * creates a copy of the router object during simulation
     */
    @Override
    public LpfrMcRouter replicate() {
        return new LpfrMcRouter(this);
    }

    /**
     *  Initializes predictability hash
     */
    private void initPreds() {
    	this.tpm = WeightMatrix.tpm ;
        this.tpmN = new HashMap<DTNHost, double[][]>();
        this.tpmNnext = new HashMap<DTNHost, double[]>();
        this.ipvN = new HashMap<DTNHost, double[]>();
        this.mdpN = new HashMap<DTNHost, Map<String,Double>>();
        this.mdp = new HashMap<String, Double>();
    }
    
    
	@Override
	public void changedConnection(Connection con) {
		// TODO Auto-generated method stub
		super.changedConnection(con);
		
		if(con.isUp()) {
			
			DTNHost otherNode = con.getOtherNode(getHost());
			if(!ipvN.containsKey(otherNode)) {
				System.out.println("didn't contain" + otherNode.getLocation());
				ipvN.put(otherNode, WeightMatrix.getDefaultipv());
				tpmN.put(otherNode, WeightMatrix.getDefaultTPM());
				tpmNnext.put(otherNode, new double[] {0});
				mdpN.put(otherNode, new HashMap<String, Double>());
                                mdpN.get(otherNode).put("Sagar", 4.0);
                                System.out.println(mdpN.get(otherNode).get("Sagar"));
			} 
			
		}
	}
	


    /**
     * implements step 7 of the algorithm 
     * @throws Exception 
     */
    public void updateTpm(DTNHost host, int state, double[] ps) throws Exception {
        if(tpmN.containsKey(host)) {
            double[][] tpmHost = tpmN.get(host);
            for(int i = 0; i < 5; ++i) {
                tpmHost[state][i] += ps[i];
                tpmHost[state][i] /= 2;
            }
            System.out.println("finished updatetpm");
        } else {
        	throw new Exception("can't find host in tpmn");
        }
    }


    public double[][] multiplyTwoTpms(double[][] tpm1, double[][] tpm2) {
    	
        double[][] productMatrix = new double[5][5];
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                double sum = 0;
                try {
	                for(int k = 0; k < 5; k++) {
	                    sum += tpm1[i][k] * tpm2[k][j];
	                }
                } catch(Exception e ) {
                	System.out.println("there is some problem");
                }
                productMatrix[i][j] = sum;
            }
        }
        return productMatrix;
    }


    public double[] multiplyVectorWithMatrix(double[] v, double[][] matrix) {
        double[] tpmHostNext = new double[5];
            for(int j = 0; j < 5; j++) {
                for(int k = 0; k < 5; k++) {
                    tpmHostNext[j] = v[k] * matrix[k][j];
                }
            }
        return tpmHostNext;
    }
//calculates mdp for the neighbour
//documented names are misleading
public double calculateMDPHost(DTNHost host, double[] weight, String messageId) {
        double sum = 0;
        double[] tpmHostNext = tpmNnext.get(host);
        for(int i = 0; i < 5; i++) {
            sum += weight[i] * tpmHostNext[i];
        }  
        mdpN.get(host).put(messageId, Math.max(sum, 0.0));
        System.out.println("mdp for neighbour" + sum);
        return sum ;
        
    }

    public void calculateMDP(double[] weight, String messageId) {
        System.out.println("hey called calculatemdp");
        double tempMdp = 0.0 ; 
        for(int i = 0; i < 5; i++) tempMdp += weight[i] * tpmSNext[i];
        System.out.println(tempMdp + "is mdp");
        this.mdp.put(messageId, Math.max(0, tempMdp));
        System.out.println("yes," + this.mdp.get(messageId));
    }

    public void findAreas(NeighbourInfo n) {
    	
    	//range radius of the source which will be constant irrespective of anything
    	double rangeRadius = WeightMatrix.TRANSMISSION_RANGE;
  
    	/************** CALCULATE AREA1 AND AREA4 ******************/
    	
    	//alpha1 and alpha2 = (90 - (2 * Beta)) / 2
    	double alpha1 =(90 - n.angleBeta * 2) / 2;
    	double alpha2 = alpha1;
  
    	//calculates pi*r*r; radius is transmissionRange of the source 
    	double PIR2 = Math.PI * (rangeRadius * rangeRadius);
    	
    	//calculates area1 and area4
    	//using arc formula, based on proportionality of the angle to the area
    	double area1 = PIR2 * (alpha1/360.0);
    	double area4 = area1;
    	
    	/************** ENDING CALCULATING AREA1 and AREA4 ************/
    	
    	
    	
    	/*************** CALCULATE AREA2 and AREA3 **************/
    	
    	//get the area of the circle subtended by beta1 + beta2;
    	double largerArea = PIR2 * ( (n.angleBeta * 2) / 360.0);
    	
    	//calculate the length of hypotenuse between source and neighbour coordinates;
    	//using pythagoras theorem, a^2 = b^2 + c^2, 
    	//b and c are just the coordinates of the neighbour in the transformed space
    	double hypotenuse = Math.sqrt((n.tranCord.getX() * n.tranCord.getX()) + (n.tranCord.getY() * n.tranCord.getY()));
    	
    	//find out area2
    	//area2 is proportional to beta1 + beta2
    	//area2 = (angleBeta/360.0) * pi * r * r ; 
    	//substitute r with hypotenuse which happens to be the radius for the  circle inside of the transmission range circle  	
    	double area2 = (n.angleBeta/360.0) * Math.PI * hypotenuse * hypotenuse;
  
    	//area3 is portion left after finding area2
    	//in short, area2 + area3 = largerArea
    	double area3 = largerArea - area2;
    	
    	/*************** END OF CALCULATING AREA2 and AREA3 ********/
    	
    	
    	//modify the neighbour reference to store the areas
    	Area area = new Area(area1, area2, area3, area4);
    	n.area = area;
    	
    }

    /**
     * implements step 8,9 of the algorithm
     * Prediction of the next region/location of node n with markov chain
     */
    public void calculateTpmNnext(DTNHost host) {

            double[][] tpmHost = tpmN.get(host);
            double[][] tpmNew = multiplyTwoTpms(this.tpm, tpmHost);
            double[] tpmHostNext = tpmNnext.get(host);
            tpmHostNext = multiplyVectorWithMatrix(ipvN.get(host), tpmNew);
            tpmNnext.put(host, tpmHostNext);
            
    }
  
    /**
     * implements step 11 of the algorithm
     * Prediction of the next region/location of source node S with markov chain
     */
    public void calculateTpmSnext() {
        tpmSNext = multiplyVectorWithMatrix(initialProbabilityVector, tpm);
    }

    public double[] calculatePredictionToNextStates(DTNHost host, double area1, 
                                                double area2, double totalArea,
                                                double beta,
                                                int state) {

        if(tpmN.containsKey(host)) {
            double[] ps = new double[5];
            double pleft;
            ps[state] = (area2 / totalArea) * (90 - 2 * beta) / 90 ;
            switch(state) {
                case 1: ps[2] = ((totalArea/2) - area1) / totalArea;
                        ps[3] = area1/totalArea;
                        pleft = 1 - (ps[1] + ps[2] + ps[3]);
                        ps[0] = (3 / 4.0) * pleft;
                        ps[4] = (1 / 4.0) * pleft;
                        break;
                            
                //TODO: do it for neighbours in other states i.e for state = i, for i in 2,3,4
                case 2: ps[4] = ((totalArea/2) - area1) / totalArea; 
                		ps[1] = area1/totalArea;
                		pleft = 1 - (ps[1] + ps[2] + ps[4]);
                		ps[0] = 0.75 * pleft;
                		ps[3] = 0.25 * pleft; 
                		break;
                		
                case 3: ps[1] = ((totalArea/2) - area1) / totalArea;
                		ps[4] = area1/totalArea;
                		pleft = 1 - (ps[1] + ps[4] + ps[state]);
                		ps[0] = 0.75 * pleft;
                		ps[2] = 0.25 * pleft;
                		break;
                		
                case 4: ps[3] = ((totalArea/2) - area1) / totalArea;
                		ps[2] = area1/totalArea;
                		pleft = 1 - (ps[state] + ps[3] + ps[2]);
                		ps[0] = 0.75 * pleft;
                		ps[1] = 0.25 * pleft;
                		break;
            }
            return ps;
        }
        return null;
    }
    
    
	public void algorithmDriver(DTNHost source, DTNHost neighbour, DTNHost destination, String messageId) {
		double[] ps;
		try {
			NeighbourInfo neighbourInfo = findAngles(source, neighbour, destination);
			findAreas(neighbourInfo);
			Area area = neighbourInfo.area;	
			ps = calculatePredictionToNextStates(neighbour, area.getArea1(), area.getArea2(), area.getTotalArea(), neighbourInfo.angleBeta / 2.0, neighbourInfo.state);
			updateTpm(neighbour, neighbourInfo.state, ps);
			calculateTpmNnext(neighbour);
			calculateTpmSnext();
                        

			calculateMDP(WeightMatrix.w0, messageId);
                        System.out.println("hey what's going");
			calculateMDPHost(neighbour, WeightMatrix.getWeightMatrix(neighbourInfo.state), messageId);
		} catch(Exception e) {
			System.out.println("error in algorithm driver");
		}
	}


    
    int findState(Coord x) {
    	if(x.getX() >= 0 && x.getY() >= 0) return 1;
    	if(x.getX() < 0 && x.getY() >= 0) return 2;
    	if(x.getX() < 0 && x.getY() < 0) return 3;
    	if(x.getX() >= 0 && x.getY() < 0) return 4;
    	return -1;
    }
    
    double findAngleBeta(int stateNeighbour, int stateDestination, double angleNeighbour, double angleDestination) {
    	switch(stateNeighbour) {
    	case 1: if(stateDestination  == 2) {
    				return (90 - angleNeighbour) + angleDestination;
    			} else if(stateDestination == 4) {
    				return (angleNeighbour + angleDestination);
    			} 
    			break;
    	
    	case 2: if(stateDestination == 3) {
    				return angleNeighbour + (90 - angleDestination);
    			} else if(stateDestination == 1) {
    				return (90 - angleNeighbour) + (90 - angleDestination);
    			}
    			break;
    	
    	case 3: if(stateDestination == 2) {
    				return (90 - angleNeighbour) + angleDestination;
    			} else if(stateDestination == 4) {
    				return angleNeighbour + (90 - angleDestination);
    			}
    			break;
    			
    	case 4: if(stateDestination == 1) {
    				return angleNeighbour + angleDestination;
    			} else if(stateDestination == 3) {
    				return (90 - angleNeighbour) + angleDestination;
    			}
    			break;
    	}
    	
    	return 360;
    }
    
    public double findAngleBetaWithVector(Coord v1, Coord v2) {
    	
    	double x1 = v1.getX();
    	double x2 = v2.getX();
    	double y1 = v1.getY();
    	double y2 = v2.getY();
    	
    	double magnitudeV1 = Math.sqrt( (x1 * x1) + (y1 * y1));
    	double magnitudeV2 = Math.sqrt((x2 * x2) + (y2 * y2));
   
    	return Math.toDegrees(Math.acos(((x1 * x2) + (y1 * y2)) / (magnitudeV1 * magnitudeV2))); 	
    }
    
    
    NeighbourInfo  findAngles(DTNHost source, DTNHost neighbour, DTNHost destination) throws Exception {
    	Coord sourceCord = source.getLocation();
    	Coord neighbourCord = neighbour.getLocation();
    	Coord destinationCord = destination.getLocation();
    	
		Coord tranNeighbourCord= new Coord(neighbourCord.getX() - sourceCord.getX(), neighbourCord.getY() - sourceCord.getY());
		Coord tranDestinationCord = new Coord(destinationCord.getX() - sourceCord.getX(), destinationCord.getY() - sourceCord.getY());
		

		int stateOfNeighbour = findState(tranNeighbourCord);
		int stateOfDestination = findState(tranDestinationCord);
		
		System.out.println("Neighbour state" + stateOfNeighbour);
		System.out.println("Destination state" + stateOfDestination);
	//	System.out.println("Destination Coord" + stateOfDestination);
		System.out.println("neighbour cord" + tranNeighbourCord);
		System.out.println("destination coord" + tranDestinationCord);
		System.out.println("source cord" + sourceCord);
		
		
		
		if(stateOfNeighbour == -1 || stateOfDestination == -1) {
			throw new Exception("state couldn't be found");
		}
		
		double angleBeta = findAngleBetaWithVector(tranNeighbourCord, tranDestinationCord);
	
		System.out.println("angle beta" + angleBeta);
		//commented because the process is lengthy. uncomment below lines and comment above line to run this method
		//double angle1 = tranSourceCord.angle(tranNeighbourCord) * (180 / Math.PI);
		//double angle2 = tranSourceCord.angle(tranDestinationCord) * (180 / Math.PI);
		//angleBeta = findAngleBeta(stateOfNeighbour, stateOfDestination, angle1, angle2);
	
		return new NeighbourInfo(angleBeta, stateOfNeighbour, neighbour, tranNeighbourCord);
	}
    
        


    
    @Override 
    public void update() {
        super.update();
        if(isTransferring() || !canStartTransfer()) {
            return;
        }

        //try messages that could be delivered to final recipient
        if(exchangeDeliverableMessages() != null) {
            return;
        }

        tryOtherMessages();
    }
    

    /**
     * Tries to send all others messages to all connected hosts ordered by their delivery probability
     */
    private Tuple<Message, Connection> tryOtherMessages() {
        List<Tuple<Message, Connection>> messages = new ArrayList<Tuple<Message, Connection>>();
        Collection<Message> msgCollection = getMessageCollection();

        for(Connection con : getConnections()) {
            DTNHost other = con.getOtherNode(getHost());
            LpfrMcRouter othRouter = (LpfrMcRouter)other.getRouter();
            System.out.println("conection between " + this.getHost().getLocation() + "and" + other.getLocation());
            if(othRouter.isTransferring()) {
                continue;
            }

            for(Message m: msgCollection) {
            	
                if(othRouter.hasMessage(m.getId())) {
                    continue; // skip messages that the other one has
                }
                
                algorithmDriver(this.getHost(), other, m.getTo(), m.getId());
                
                System.out.println(m.getTo() + "is dest");
                System.out.println("other router prediction" + mdpN.get(othRouter.getHost()).get(m.getId()));
                System.out.println("\n my prediction" + this.mdp.get(m.getId()));
                if(this.mdp.get(m.getId()) < this.mdpN.get(othRouter.getHost()).get(m.getId())) {
                    // the other node has higher probability of delivery
                    messages.add(new Tuple<Message, Connection>(m, con));  
                }
            }
        }
        if(messages.size() == 0) {
        	System.out.println("no messages were selected for transmission" + this.getHost().getLocation());
            return null;
        }

      //  Collection.sort(messages); // we can sort it, but that's for later
        return tryMessagesForConnected(messages); // try to send messages from this router

    }
    
   private class NeighbourInfo {
    	public double angleBeta;
    	public int state;
    	public DTNHost neighbour;
    	public Coord tranCord;
    	public Area area;
    	
    	NeighbourInfo(double angle, int state, DTNHost neighbour, Coord tranCord) {
    		this.angleBeta = angle;
    		this.state = state;
    		this.neighbour = neighbour;
    		this.tranCord = tranCord;
    		this.area = null; 
    	}
    	
    	NeighbourInfo() {
    		this.angleBeta = -360;
    		this.state = -1;
    		this.neighbour = null;
    		this.area = null ;
    		this.tranCord = null; 
    	}
    	
    }
    
}