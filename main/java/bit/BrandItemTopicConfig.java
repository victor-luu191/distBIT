package bit;

import org.kohsuke.args4j.Option;
import org.petuum.jbosen.PsConfig;

public class BrandItemTopicConfig extends PsConfig {
	@Option(name = "-staleness", required = false, usage = "Staleness of parameter tables. Default = 0")
	public int staleness = 0;

	@Option(name = "-dataFile", required = true, usage = "Path to data file.")
	public String dataFile = "";

	@Option(name = "-numEpochs", required = false, usage = "Number of passes over data. Default = 10")
	public int numEpochs = 10;

	@Option(name = "-burnIn", required = false, usage = "Number of iterations for burn-in period. Default = 100")
	public int burnIn = 100;
	
	@Option(name = "-numIter", required = false, usage = "Number of iterations for actual training period. Default = 500")
	public int numIter = 500;
	
	@Option(name = "-snapshot", required = false, usage = "Length of each snapshot. Default = 50")
	public int snapshot = 50;
	
	@Option(name = "-seed", required = false, usage = "Seed for the sampler. Default = 123")
	public int seed = 123;

	@Option(name = "-numMiniBatchesPerEpoch", required = false, usage = "Equals to number of clock() calls per data sweep. "
			+ "Default = 1")
	public int numMiniBatchesPerEpoch = 1;

	@Option(name = "-outputPrefix", required = false, usage = "Output to outputPrefix.L, outputPrefix.W.")
	public String outputPrefix = "";
}
