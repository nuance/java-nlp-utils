package org.mhjones.nlp.examples;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import org.mhjones.nlp.math.DoubleArrays;
import org.mhjones.nlp.util.Counter;
import org.mhjones.nlp.util.CounterMap;
import org.mhjones.nlp.util.Encoding;
import org.mhjones.nlp.util.FeatureExtractor;
import org.mhjones.nlp.util.Pair;

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

    CounterMap<Integer, String> featureDistribution;
    FeatureExtractor[] featureExtractors;
    protected Encoding<String> featureEncoder;

    public void train(Set<Pair<String, String>> labeledData) {
        for (Pair<String,String> datum : labeledData)
            for (FeatureExtractor extractor: featureExtractors)
                for (int feature : extractor.extractFeatures(datum.getFirst()))
                    featureDistribution.incrementCount(feature, datum.getSecond());

        featureDistribution.normalize();
    }

    public String label(String datum) {
        double uniform = 1.0 / (double)featureDistribution.secondaryEncoding.size();
        double[] labelDistribution = DoubleArrays.constantArray(featureDistribution.secondaryEncoding.size(), uniform);

        for (FeatureExtractor extractor: featureExtractors)
            for (int feature : extractor.extractFeatures(datum)) {
                Counter<String> features = featureDistribution.getCounter(feature);
		//                System.out.println("Feature " + featureEncoder.decode(feature) + ": " + features);
                DoubleArrays.inPlaceMultiply(labelDistribution, features.values, 0, features.encoding.size());
            }

	//	System.out.print("Labelling " + datum + ": [");
	//	for (int i = 0; i < labelDistribution.length; i++)
	//	    System.out.print(" " + featureDistribution.secondaryEncoding.decode(i) + " : " + labelDistribution[i] + ",");
	//	System.out.println("");

        return featureDistribution.secondaryEncoding.decode(DoubleArrays.argMax(labelDistribution));
    }

    public double score(String datum) {
        double uniform = 1.0 / (double)featureDistribution.secondaryEncoding.size();
        double[] labelDistribution = DoubleArrays.constantArray(featureDistribution.secondaryEncoding.size(), uniform);

        for (FeatureExtractor extractor: featureExtractors)
            for (int feature : extractor.extractFeatures(datum)) {
                Counter<String> features = featureDistribution.getCounter(feature);
                //              System.out.println("Feature " + featureEncoder.decode(feature) + ": " + features);
                DoubleArrays.inPlaceMultiply(labelDistribution, features.values, 0, features.encoding.size());
            }

	DoubleArrays.inPlaceNormalize(labelDistribution);

        return DoubleArrays.max(labelDistribution);
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

        featureExtractors = this.featureExtractors();
    }

    protected FeatureExtractor[] featureExtractors() {
	FeatureExtractor[] ret = new FeatureExtractor[2];
        //      featureExtractors[0] = new IdentityExtractor(featureEncoder);
        ret[0] = new CharacterExtractor(featureEncoder);
        ret[1] = new BiCharacterExtractor(featureEncoder);
	return ret;
    }

    public static Set<Pair<String, String>> readDelimitedData(String filename, String delimiter) throws IOException {
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);
        Set<Pair<String, String>> pairs = new HashSet<Pair<String, String>>();

        while (br.ready()) {
            String line = br.readLine();
            String[] split = line.split(delimiter);

            pairs.add(new Pair<String,String>(split[1], split[0]));
        }

        br.close();
        fr.close();

        return pairs;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("*** Naive Bayes Classifier ***");

        boolean verbose = false;
        NaiveBayesClassifier classifier = new NaiveBayesClassifier();

        /** Read in training and test data **/
        Set<Pair<String, String>> labeledTrainingData = readDelimitedData("data/pnp-train.txt", "\t");
        Set<Pair<String, String>> labeledTestData = readDelimitedData("data/pnp-test.txt", "\t");
	
	Set<String> testData = new HashSet<String>();
	for (Pair<String,String> datum : labeledTestData) testData.add(datum.getFirst());
	
        classifier.train(labeledTrainingData);

        Map<String, String> guessedLabels = classifier.label(testData);

        int correct = 0;
        for (Pair<String,String> datum : labeledTestData) {
            if (verbose) classifier.debugLabeling(datum.getFirst());
            if (guessedLabels.get(datum.getFirst()).equals(datum.getSecond()))
                correct++;
        }

        System.out.println("Correctly labeled " + correct + " of " + labeledTestData.size());
    }
}
