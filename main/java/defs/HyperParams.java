package defs;

public class HyperParams {
	double alpha ;	// topic-brand prior
	double beta ;	// brand-item prior
	double gamma ;	// user-decision prior
	public double theta ;	// user-topic prior
	double phi ;	// topic-item prior
	
	public HyperParams(double alpha, double beta, double gamma, double theta,
			double phi) {
		super();
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.theta = theta;
		this.phi = phi;
	}
}
