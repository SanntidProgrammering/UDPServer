package udp.server;

public class main_1 {

    /**
     * @param args
     */
    public static void main(String[] args) {

        float number = 100f;

        AUTOMODES result = setState(number);

        System.out.println(result);

        /*
		// TODO Auto-generated method stub
                double maxSpeed = 1.0;
		MiniPID miniPID; 
		
		miniPID=new MiniPID( .25,0.01,.4);
		miniPID.setOutputLimits(maxSpeed);
		miniPID.setMaxIOutput(2);
		//miniPID.setOutputRampRate(3);
		miniPID.setOutputFilter(.3);
		//miniPID.setSetpointRange(40);

		double target=0;
		
		double actual=60;
		double output=0;
		
		miniPID.setSetpoint(0);
		miniPID.setSetpoint(target);
		
		System.err.printf("Target,Actual\tOutput\tError\tLeft\tRight\n");
		//System.err.printf("Output\tP\tI\tD\n");
                int counter = 0;
		//* Position based test code
		for (int i=0;i<200 ;i++){
			
			//if(i==50)miniPID.setI(.05);
			
			//if(i==75)target=(100);
			//if(i>50 && i%4==0)target=target+(Math.random()-.5)*50;
			
			output=limit(miniPID.getOutput(actual,target));
			actual=actual+output;
			double left = 100.0*Math.abs(1-(output/maxSpeed));
                        double right = 100.0*Math.abs(1+(output/maxSpeed));
			//System.out.println("=========================="); 
			//System.out.printf("Current: %3.2f , Actual: %3.2f, Error: %3.2f\n",actual, output, (target-actual));
			System.err.printf("%3.2f\t%3.2f\t%3.2f\t%3.2f\t%3.2f\t%3.2f\n",target,actual, output, (target-actual),left,right);
			
			
			//if(i>80 && i%5==0)actual+=(Math.random()-.5)*20;
		}
         */
    }

    public static double limit(double a) {
        double MAX = 10.0;
        double MIN = -10.0;
        return (a > MAX) ? MAX : (a < MIN ? MIN : a);
    }

    public static AUTOMODES setState(float value) {
        AUTOMODES result = AUTOMODES.FWD;
        int intValue = Math.round(value);
        if (intValue == AUTOMODES.SEARCH_LEFT.value) {
            result = AUTOMODES.SEARCH_LEFT;

        } else if (intValue == AUTOMODES.SEARCH_RIGHT.value) {
            result = AUTOMODES.SEARCH_RIGHT;
        }
        return result;
    }

    public static enum AUTOMODES {
        FWD(1),
        SEARCH_LEFT(-1000),
        SEARCH_RIGHT(1000);

        private int value;

        private AUTOMODES(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

}
