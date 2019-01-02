


package routing.lpfr ; 

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
import routing.*;

public class LpfrMcRouter extends ActiveRouter {

    
    public static final String LPFR_MC_NS = "LpfrMcRouter" ;                                                                                                                                                                                                                                                                           

    //defines initial probability vector for each node
    private double[] initialProbabilityVector = {0.95, 0.1, 0.1, 0.1, 0.1};
    private double[][] tpm = new double[5][5];
    private double[] tpmSNext = new double[5];
    
    
    private Map<DTNHost, double[]> ipvN;
    private Map<DTNHost, double[][]> tpmN;
    private Map<DTNHost, double[]> tpmNnext;
    private Map<DTNHost, Double> mdpN;
    private double mdp;
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
        this.tpmN = new HashMap<DTNHost, double[][]>();
        this.tpmNnext = new HashMap<DTNHost, double[]>();
        this.ipvN = new HashMap<DTNHost, double[]>();
        this.mdpN = new HashMap<DTNHost, Double>();
    }
    
    
	@Override
	public void changedConnection(Connection con) {
		// TODO Auto-generated method stub
		super.changedConnection(con);
		
		if(con.isUp()) {
			
			//TODO: call methods that implement the algorithm
			DTNHost otherNode = con.getOtherNode(getHost());
			if(!ipvN.containsKey(otherNode)) {
				ipvN.put(otherNode, WeightMatrix.getDefaultipv());
				tpmN.put(otherNode, WeightMatrix.getDefaultTPM());
				tpmNnext.put(otherNode, new double[] {0});
				mdpN.put(otherNode, 0.0);
			} 
			
		}
	}
	
	public void algorithmDriver(DTNHost source, DTNHost neighbour, DTNHost destination) {
		double[] ps;
		System.out.println("called function");
//		try {
//			System.out.println("hey mate");
//			NeighbourInfo neighbourInfo = findAngles(source, neighbour, destination);
//			System.out.println("here");
////			findAreas(neighbourInfo);
////			Area area = neighbourInfo.getArea();	
////			ps = calculatePredictionToNextStates(source, area.getArea1(), area.getArea2(), area.getTotalArea(), neighbourInfo.getAngleBeta()/2, neighbourInfo.getState());
////			updateTpm(source, neighbourInfo.getState(), ps);
////			calculateTpmNnext(source);
////			calculateTpmSnext();
////			calculateMDP(WeightMatrix.w0);
////			calculateMDPHost(source, WeightMatrix.getWeightMatrix(neighbourInfo.getState()));
//		} catch(Exception e) {
//			System.out.println("error in algorithm driver");
//		}
	}


    /**
     * implements step 7 of the algorithm 
     */
    public void updateTpm(DTNHost host, int state, double[] ps) {
        if(tpmN.containsKey(host)) {
            double[][] tpmHost = tpmN.get(host);
            for(int i = 0; i < 5; ++i) {
                tpmHost[state][i] += ps[i];
                tpmHost[state][i] /= 2;
            }
        }
    }


    public double[][] multiplyTwoTpms(double[][] tpm1, double[][] tpm2) {
        double[][] productMatrix = new double[5][5];
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                double sum = 0;
                for(int k = 0; k < 5; k++) {
                    sum += tpm1[i][k] * tpm2[k][j];
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

    public double calculateMDPHost(DTNHost host, double[] weight) {
        double sum = 0;
        double[] tpmHostNext = tpmNnext.get(host);
        for(int i = 0; i < 5; i++) {
            sum += weight[i] * tpmHostNext[i];
        }
        return sum;
    }

    public void calculateMDP(double[] weight) {
        this.mdp = 0;
        for(int i = 0; i < 5; i++) this.mdp += weight[i] * tpmSNext[i];
    }

    public void findAreas(NeighbourInfo n) {
    	
    	//range radius of the source which will be constant irrespective of anything
    	double rangeRadius = WeightMatrix.TRANSMISSION_RANGE;
    	double angleBeta = n.getAngleBeta();
    	/************** CALCULATE AREA1 AND AREA4 ******************/
    	
    	//alpha1 and alpha2 = (90 - (2 * Beta)) / 2
    	double alpha1 =(90 - angleBeta * 2) / 2;
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
    	double largerArea = PIR2 * ( (angleBeta * 2) / 360.0);
    	
    	//calculate the length of hypotenuse between source and neighbour coordinates;
    	//using pythagoras theorem, a^2 = b^2 + c^2, 
    	//b and c are just the coordinates of the neighbour in the transformed space
    	double hypotenuse = Math.sqrt((n.getTranCord().getX() * n.getTranCord().getX()) + (n.getTranCord().getY() * n.getTranCord().getY()));
    	
    	//find out area2
    	//area2 is proportional to beta1 + beta2
    	//area2 = (angleBeta/360.0) * pi * r * r ; 
    	//substitute r with hypotenuse which happens to be the radius for the  circle inside of the transmission range circle  	
    	double area2 = (angleBeta/360.0) * Math.PI * hypotenuse * hypotenuse;
  
    	//area3 is portion left after finding area2
    	//in short, area2 + area3 = largerArea
    	double area3 = largerArea - area2;
    	
    	/*************** END OF CALCULATING AREA2 and AREA3 ********/
    	

    	//modify the neighbour reference to store the areas
    	Area area = new Area(area1, area2, area3, area4);
    	n.setArea(area);	
    	
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
            double[] ps = null;
            switch(state) {
                case 1:     ps = new double[5];
                            ps[state] = (area2 / totalArea) * (90 - 2 * beta) / 90;
                            ps[2] = ((totalArea/2) - area1) / totalArea;
                            ps[3] = area1/totalArea;
                            double pleft = 1 - (ps[1] + ps[2] + ps[3]);
                            ps[0] = (3 / 4.0) * pleft;
                            ps[4] = (1 / 4.0) * pleft;
                            break;
                            
                //TODO: do it for neighbours in other states i.e for state = i, for i in 2,3,4
                case 2:
                case 3:
                case 4:
            }
            return ps;
        }
        
        return null;
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
    
    NeighbourInfo  findAngles(DTNHost source, DTNHost neighbour, DTNHost destination) throws Exception {
    	Coord sourceCord = source.getLocation();
    	Coord neighbourCord = neighbour.getLocation();
    	Coord destinationCord = destination.getLocation();
    	
		Coord tranSourceCord = new Coord(sourceCord.getX() - sourceCord.getX(), sourceCord.getY() - sourceCord.getY());
		Coord tranNeighbourCord= new Coord(neighbourCord.getX() - sourceCord.getX(), neighbourCord.getY() - sourceCord.getY());
		Coord tranDestinationCord = new Coord(destinationCord.getX() - sourceCord.getX(), destinationCord.getY() - sourceCord.getY());
		

		int stateOfNeighbour = findState(tranNeighbourCord);
		int stateOfDestination = findState(tranDestinationCord);
		if(stateOfNeighbour == -1 || stateOfDestination == -1) {
			throw new Exception("state couldn't be found");
		}
		
		double angle1 = tranSourceCord.angle(tranNeighbourCord) * (180 / Math.PI);
		double angle2 = tranSourceCord.angle(tranDestinationCord) * (180 / Math.PI);
		
		double angleBeta;
		if(stateOfNeighbour == stateOfDestination) {
			angleBeta = Math.abs(angle1 - angle2);
		} else {
			angleBeta = findAngleBeta(stateOfNeighbour, stateOfDestination, angle1, angle2);
		}
		if(angleBeta == 360) {
			throw new Exception("source and destination in different coordinates ");
		}
		return new NeighbourInfo(angleBeta, stateOfNeighbour, neighbour, tranNeighbourCord);
	}
    
        
    /**
     * returns MDP for the host
     */
    public double getPredFor(DTNHost host) {
        if(mdpN.containsKey(host)) {
            return mdpN.get(host);
        } else {
            return 0;
        }
    }

    /**
     * returns a hash map containing the delivery predictions fo
     */
    private Map<DTNHost, Double> getDeliveryPreds() {
        return this.mdpN;
    }

    
    @Override 
    public void update() {
    	System.out.println("updated called");
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
    	System.out.println("try other messages called");
        List<Tuple<Message, Connection>> messages = new ArrayList<Tuple<Message, Connection>>();
        Collection<Message> msgCollection = getMessageCollection();

        for(Connection con : getConnections()) {
            DTNHost other = con.getOtherNode(getHost());
            ProphetRouter othRouter = (ProphetRouter)other.getRouter();

            if(othRouter.isTransferring()) {
                continue;
            }

            for(Message m: msgCollection) {
            	
                if(othRouter.hasMessage(m.getId())) {
                    continue; // skip messages that the other one has
                }
                System.out.println("here");
//                algorithmDriver(this.getHost(), other, m.getTo());

//                if(othRouter.getPredFor(m.getTo()) > getPredFor(m.getTo())) {
//                    // the other node has higher probability of delivery
//                    messages.add(new Tuple<Message, Connection>(m, con));
//                }
                messages.add(new Tuple<Message, Connection>(m, con));
            }
        }
        if(messages.size() == 0) {
            return null;
        }

      //  Collection.sort(messages); // we can sort it, but that's for later
        return tryMessagesForConnected(messages); // try to send messages from this router

    }
  
}