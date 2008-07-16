package org.mhjones.nlp.examples;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mhjones.nlp.math.DoubleArrays;
import org.mhjones.nlp.math.Function;
import org.mhjones.nlp.math.FunctionMinimizer;
import org.mhjones.nlp.util.Counter;
import org.mhjones.nlp.util.CounterMap;
import org.mhjones.nlp.util.Encoding;
import org.mhjones.nlp.util.FeatureExtractor;
import org.mhjones.nlp.util.Pair;

public class MaximumEntropyClassifier {

    protected class IdentityExtractor implements FeatureExtractor {
	Encoding<String> encoder;

	public int[] extractFeatures(String datum) {
	    int[] features = new int[1];

	    features[0] = encoder.encode("IDENTITY-" + datum);

	    return features;
	}

	public IdentityExtractor(Encoding<String> encoder) {
	    this.encoder = encoder;
	}
    }

    protected class CharacterExtractor implements FeatureExtractor {
	Encoding<String> encoder;

	public int[] extractFeatures(String datum) {
	    int[] features = new int[datum.length()];

	    for (int i = 0; i < datum.length(); i++)
		features[i] = encoder.encode("CHAR-" + datum.charAt(i));

	    return features;
	}

	public CharacterExtractor(Encoding<String> encoding) {
	    this.encoder = encoding;
	}
    }

    protected class BiCharacterExtractor implements FeatureExtractor {
	Encoding<String> encoder;

	public int[] extractFeatures(String datum) {
	    int[] features = new int[datum.length()];
	    char last = '_';

	    for (int i = 0; i < datum.length(); i++) {
		features[i] = encoder.encode("CHAR-" + last + datum.charAt(i));
		last = datum.charAt(i);
	    }

	    return features;
	}

	public BiCharacterExtractor(Encoding<String> encoding) {
	    this.encoder = encoding;
	}
    }

    private class MaxEntWeightFunction extends Function {

	private CounterMap<Integer, String> featureDistribution;
	
	protected double calculateValue(double[] point) {
	    return 0.0;
	}

	protected double[] calculateGradient(double[] point) {
	    return new double[1];
	}

	protected Pair<Double, double[]> calculateValueAndGradient(double[] point) {
	    return new Pair<Double, double[]>(calculateValue(point), calculateGradient(point));
	}

	public int dimension() {
	    return 1;
	}

	public Pair<Double, Double> range() {
	    return new Pair<Double, Double>(0.0, 1.0);
	}
	
	public MaxEntWeightFunction(CounterMap<Integer, String> featureDistribution) {
	    this.featureDistribution = featureDistribution;
	}
    }
    
    CounterMap<Integer, String> featureDistribution;
    CounterMap<Integer, String> featureCounts;
    double[] featureWeights;
    FeatureExtractor[] featureExtractors;
    Encoding<String> featureEncoder;

    public void train(Pair<String, String>[] labeledData) {
	System.out.println("*** Extracting features ***");
	for (Pair<String, String> datum : labeledData)
	    for (FeatureExtractor extractor: featureExtractors)
		for (int feature : extractor.extractFeatures(datum.getFirst()))
		    featureCounts.incrementCount(feature, datum.getSecond());

	System.out.println("*** Normalizing feature distribution ***");
	featureDistribution = new CounterMap<Integer, String>(featureCounts);
	featureDistribution.normalize();

	// Create a minimizer and a function to minimize
	MaxEntWeightFunction func = new MaxEntWeightFunction(featureDistribution);
	FunctionMinimizer minimizer = new FunctionMinimizer(func, 10);

	System.out.println("*** Optimizing feature weights ***");
	featureWeights = minimizer.minimize();
    }

    public String label(String datum) {
	double[] labelDistribution = DoubleArrays.constantArray(featureDistribution.secondaryEncoding.size(), 0.0);

	for (FeatureExtractor extractor: featureExtractors) {
	    for (int feature : extractor.extractFeatures(datum)) {
		Counter<String> features = featureDistribution.getCounter(feature);
		
		DoubleArrays.inPlaceMultiply(features.values, featureWeights[feature], 0, features.encoding.size());
		DoubleArrays.inPlaceAdd(labelDistribution, features.values, 0, features.encoding.size());
	    }
	}

	// The following code produces the normalized log probabilities
	//	double[] expProbs = DoubleArrays.exp(labelDistribution);
	//	double normalization = Math.log(DoubleArrays.sum(expProbs));
	//	DoubleArrays.inPlaceAdd(labelDistribution, -normalization);


	//	System.out.print("Labelling " + datum + ": [");
	//	for (int i = 0; i < labelDistribution.length; i++)
	//	    System.out.print(" " + featureDistribution.secondaryEncoding.decode(i) + " : " + labelDistribution[i] + ",");
	//	System.out.println("");

	return featureDistribution.secondaryEncoding.decode(DoubleArrays.argMax(labelDistribution));
    }

    public Map<String, String> label(Set<String> data) {
	HashMap<String, String> labels = new HashMap<String, String>();
	
	for (String datum : data)
	    labels.put(datum, label(datum));
	
	return labels;
    }

    public void debugLabeling(String datum) {
	System.out.println(datum + ": Chose label " + label(datum));
    }

    public MaximumEntropyClassifier() {
	featureDistribution = new CounterMap<Integer, String>();

	featureEncoder = new Encoding<String>();
	featureExtractors = new FeatureExtractor[2];
	//	featureExtractors[0] = new IdentityExtractor(featureEncoder);
	featureExtractors[0] = new CharacterExtractor(featureEncoder);
	featureExtractors[1] = new BiCharacterExtractor(featureEncoder);
    }

    public static Pair<String, String>[] readDelimitedData(String filename, String delimiter) throws IOException {
	FileReader fr = new FileReader(filename);
	BufferedReader br = new BufferedReader(fr);
	ArrayList<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();

	while (br.ready()) {
	    String line = br.readLine();
	    String[] split = line.split(delimiter);
	    
	    pairs.add(new Pair<String,String>(split[1], split[0]));
	}

	br.close();
	fr.close();

	return (Pair<String, String>[]) pairs.toArray();
    }

    public static void main(String[] args) throws IOException {
	System.out.println("*** Naive Bayes Classifier ***");

	boolean verbose = false;
	MaximumEntropyClassifier classifier = new MaximumEntropyClassifier();

	/** Read in training and test data **/
	Pair<String, String>[] labeledTrainingData = readDelimitedData("data/pnp-train.txt", "\t");
	Pair<String, String>[] labeledTestData = readDelimitedData("data/pnp-test.txt", "\t");

	classifier.train(labeledTrainingData);

	HashSet<String> testData = new HashSet<String>();
	for (Pair<String, String> data : labeledTestData) {
	    testData.add(data.getFirst());
	}
	
	Map<String, String> guessedLabels = classifier.label(testData);
	
	int correct = 0;
	for (Pair<String, String> datum : labeledTestData) {
	    if (verbose) classifier.debugLabeling(datum.getFirst());
	    if (datum.getSecond().equals(guessedLabels.get(datum.getFirst())))
		correct++;
	}

	System.out.println("Correctly labeled " + correct + " of " + labeledTestData.length);
    }

}