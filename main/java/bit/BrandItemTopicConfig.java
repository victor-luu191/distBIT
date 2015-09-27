package bit;

import org.kohsuke.args4j.Option;
import org.petuum.jbosen.PsConfig;

import defs.Dimensions;
import defs.Pair;
import defs.Priors;

public class BrandItemTopicConfig extends PsConfig {
	
	@Option(name = "-staleness", required = false, usage = "Staleness of parameter tables. Default = 0")
	public int staleness = 0;

	@Option(name = "-dataDir", required = true, usage = "Path to data folder. Default is current folder.")
	public String dataDir = "";

//	@Option(name = "-numEpochs", required = false, usage = "Number of passes over data. Default = 10")
//	public int numEpochs = 10;

	@Option(name = "-burnIn", required = false, usage = "Number of iterations for burn-in period. Default = 100")
	public int burnIn = 100;
	
	@Option(name = "-numIter", required = false, usage = "Number of iterations for actual training period. Default = 500")
	public int numIter = 500;
	
	@Option(name = "-period", required = false, usage = "Length of each period. Default = 10")
	public int period = 25;
	
	@Option(name = "-numTopic", required = false, usage = "Number of topics for BIT. Default = 2")
	public int numTopic = 2;
	
	@Option(name = "-outputPrefix", required = false, usage = "Output to outputPrefix.")
	public String outputPrefix = "";
	
	@Option(name = "-alpha", required = false, usage = "topic-brand prior")
	public double alpha = 0.1;
	
	@Option(name = "-beta", required = false, usage = "brand-item prior")
	public double beta = 0.1; 
	
	@Option(name = "-gamma", required = false, usage = "user-decision prior")
	public double gamma = 0.1;	// correct default? 
	
	@Option(name = "-theta", required = false, usage = "user-topic prior")
	public double theta = 0.1; 
	
	@Option(name = "-phi", required = false, usage = "topic-item prior")
	public double phi = 0.1; 
	
	public Priors priors = new Priors(alpha, beta, gamma, theta, phi);
	
	public Pair[] allPairs;

	public Dimensions dims;
	
	@Option(name = "-numRestart", required = false, usage = "Number of restarts for training BIT. Default = 10")
	public int numRestart = 10;
	
//	@Option(name = "-seed", required = false, usage = "Seed for the sampler. Default = 123")
//	public int seed = 123;

//	@Option(name = "-numMiniBatchesPerEpoch", required = false, usage = "Equals to number of clock() calls per data sweep. "
//			+ "Default = 1")
//	public int numMiniBatchesPerEpoch = 1;

	
}
