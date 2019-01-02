package routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeightMatrix {

	   public static final double w0[] = {0.5, 0.15, 0.20, 0.05, 0.1};
	   public static final double w1[] = {0.1, 0.4, 0.15, 0.20, 0.15};
	   public static final double w2[] = {0.1, 0.15, 0.45, 0.20, 0.15};
	   public static final double w3[] = {0.1, 0.15, 0.20, 0.45, 0.15};
	   public static final double w4[] = {0.1, 0.15, 0.20, 0.15, 0.45};  
	   public static final double ipv[] = {0.2, 0.2, 0.2, 0.2, 0.2};
	   public static final double tpm[][] = {{0.2, 0.2, 0.2, 0.2, 0.2}, {0.2, 0.2, 0.2, 0.2, 0.2}, {0.2, 0.2, 0.2, 0.2, 0.2}, {0.2, 0.2, 0.2, 0.2, 0.2}, {0.2, 0.2, 0.2, 0.2, 0.2} };
	   public static double wm[][] = new double[][] { w0, w1, w2, w3, w4};
	   public static final double TRANSMISSION_RANGE  = 100; 
	   
	   
	   public WeightMatrix() {
		   
	   }
	   
	   public static double[] getDefaultipv() {
		   return ipv;
	   }
	   
	   public static double[][] getDefaultTPM() {
		   return tpm;
	   }
	   
	   public static double[] getWeightMatrix(int state) throws Exception {
		  if(state >= 0 && state < 5) {
			  return wm[state];
		  } else {
			  throw new Exception("Value incorrect");
		  }
	   }
}
