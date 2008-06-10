package org.mhjones.nlp.examples;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.mhjones.nlp.math.DoubleArrays;
import org.mhjones.nlp.util.CounterMap;
import org.mhjones.nlp.util.Encoding;
import org.mhjones.nlp.util.FeatureExtractor;

public class NaiveBayesClassifier {

    protected class IdentityExtractor<E> implements FeatureExtractor<E> {
	Encoding<String> encoder;

	public int[] extractFeatures(E datum) {
	    int[] features = new int[1];

	    features[0] = encoder.encode("IDENTITY-" + datum);

	    return features;
	}

	public IdentityExtractor(Encoding<String> encoder) {
	    this.encoder = encoder;
	}
    }

    CounterMap<Integer, String> featureDistribution;
    FeatureExtractor<String>[] featureExtractors;
    Encoding<String> featureEncoder;

    public void train(Map<String, String> labeledData) {
	for (String datum : labeledData.keySet())
	    for (FeatureExtractor<String> extractor: featureExtractors)
		for (int feature : extractor.extractFeatures(datum))
		    featureDistribution.incrementCount(feature, labeledData.get(datum));

	featureDistribution.normalize();
    }

    public String label(String datum) {
	double[] labelDistribution = new double[featureDistribution.encoder.size()];

	for (FeatureExtractor<String> extractor: featureExtractors)
	    for (int feature : extractor.extractFeatures(datum))
		DoubleArrays.inPlaceAdd(labelDistribution, featureDistribution.getCounter(feature).values);

	return featureDistribution.encoder.decode(DoubleArrays.argMax(labelDistribution));
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
	featureDistribution = new CounterMap<String, String>(true);

	featureEncoder = new Encoding<String>();

	featureExtractors = (FeatureExtractor<String>[]) new Object[1];
	featureExtractors[0] = new IdentityExtractor<String>(featureEncoder);
    }

    public static void main() {
	System.out.println("*** Naive Bayes Classifier ***");

	boolean verbose = true;

	NaiveBayesClassifier classifier = new NaiveBayesClassifier();
	Map<String, String> labeledTrainingData = new HashMap<String, String>();
	Map<String, String> labeledTestData = new HashMap<String, String>();

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