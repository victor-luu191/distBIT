package defs;

public class Priors {
	public double alpha ;	// topic-brand prior
	public double beta ;	// brand-item prior
	public double gamma ;	// user-decision prior
	public double theta ;	// user-topic prior
	public double phi ;	// topic-item prior
	
	
	
	public Priors() {
		alpha = 0.1;
		beta = 0.1;
		gamma = 0.1;
		theta = 0.1;
		phi = 0.1;
	}



	public Priors(double alpha, double beta, double gamma, double theta,
			double phi) {
		super();
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.theta = theta;
		this.phi = phi;
	}
}
