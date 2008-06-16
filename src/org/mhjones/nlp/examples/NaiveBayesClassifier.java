package org.mhjones.nlp.examples;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.mhjones.nlp.math.DoubleArrays;
import org.mhjones.nlp.util.CounterMap;
import org.mhjones.nlp.util.Encoding;
import org.mhjones.nlp.util.FeatureExtractor;

public class NaiveBayesClassifier {

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

    CounterMap<Integer, String> featureDistribution;
    FeatureExtractor[] featureExtractors;
    Encoding<String> featureEncoder;

    public void train(Map<String, String> labeledData) {
	for (String datum : labeledData.keySet())
	    for (FeatureExtractor extractor: featureExtractors)
		for (int feature : extractor.extractFeatures(datum))
		    featureDistribution.incrementCount(feature, labeledData.get(datum));

	featureDistribution.normalize();
    }

    public String label(String datum) {
	double[] labelDistribution = new double[featureDistribution.primaryEncoding.size()];

	for (FeatureExtractor extractor: featureExtractors)
	    for (int feature : extractor.extractFeatures(datum)) {
		double[] featureDist = featureDistribution.getCounter(feature).values;
		DoubleArrays.inPlaceAdd(labelDistribution, featureDist, 0, featureDist.length);
	    }

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

    public NaiveBayesClassifier() {
	featureDistribution = new CounterMap<Integer, String>();

	featureEncoder = new Encoding<String>();

	featureExtractors = new FeatureExtractor[1];
	featureExtractors[0] = new IdentityExtractor(featureEncoder);
    }

    public static Map<String, String> readDelimitedData(String filename, String delimiter) throws IOException {
	FileReader fr = new FileReader(filename);
	BufferedReader br = new BufferedReader(fr);
	Map<String, String> pairs = new HashMap<String, String>();

	while (br.ready()) {
	    String line = br.readLine();
	    String[] split = line.split(delimiter);
	    
	    pairs.put(split[1], split[0]);
	}

	br.close();
	fr.close();

	return pairs;
    }

    public static void main(String[] args) throws IOException {
	System.out.println("*** Naive Bayes Classifier ***");

	boolean verbose = true;
	NaiveBayesClassifier classifier = new NaiveBayesClassifier();

	/** Read in training and test data **/
	Map<String, String> labeledTrainingData = readDelimitedData("data/pnp-train.txt", "\t");
	Map<String, String> labeledTestData = readDelimitedData("data/pnp-test.txt", "\t");

	classifier.train(labeledTrainingData);

	Map<String, String> guessedLabels = classifier.label(labeledTestData.keySet());
	
	int correct = 0;
	for (String item : labeledTestData.keySet()) {
	    if (verbose) classifier.debugLabeling(item);
	    if (labeledTestData.get(item).equals(guessedLabels.get(item)))
		correct++;
	}

	System.out.println("Correctly labeled " + correct + " of " + labeledTestData.size());
    }

}