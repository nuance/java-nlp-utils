package org.mhjones.nlp.tests;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.mhjones.nlp.examples.NaiveBayesClassifier;
import org.mhjones.nlp.util.FeatureExtractor;
import org.mhjones.nlp.util.Pair;

public class NaiveBayesClassifierTest extends TestCase {
    private Map<String, String> testData(int spam, int ham) {
        Map<String, String> pairs = new HashMap<String, String>();
        for (int i = 0; i < spam; i++)
            pairs.put("porn", "spam");
        for (int i = 0; i < ham; i++)
            pairs.put("porn", "ham");
        return pairs;
    }

    private Map<String, String> testData(int pornSpam, int pornHam, int voteSpam, int voteHam) {
        Map<String, String> pairs = new HashMap<String, String>();
        for (int i = 0; i < pornSpam; i++)
            pairs.put("porn", "spam");
        for (int i = 0; i < pornHam; i++)
            pairs.put("porn", "ham");
        for (int i = 0; i < voteSpam; i++)
            pairs.put("vote", "spam");
        for (int i = 0; i < voteHam; i++)
            pairs.put("vote", "ham");
        return pairs;
    }

    private Set<Pair<String, String>> trainingData(int spam, int ham) {
        Set<Pair<String, String>> pairs = new HashSet<Pair<String, String>>();
        for (int i = 0; i < spam; i++)
            pairs.add(new Pair<String, String>("porn", "spam"));
        for (int i = 0; i < ham; i++)
            pairs.add(new Pair<String, String>("porn", "ham"));
        return pairs;
    }

    private Set<Pair<String, String>> trainingData(int pornSpam, int pornHam, int voteSpam, int voteHam) {
        Set<Pair<String, String>> pairs = new HashSet<Pair<String, String>>();
        for (int i = 0; i < pornSpam; i++)
            pairs.add(new Pair<String,String>("porn", "spam"));
        for (int i = 0; i < pornHam; i++)
            pairs.add(new Pair<String, String>("porn", "ham"));
        for (int i = 0; i < voteSpam; i++)
            pairs.add(new Pair<String, String>("vote", "spam"));
        for (int i = 0; i < voteHam; i++)
            pairs.add(new Pair<String, String>("vote", "ham"));
        return pairs;
    }

    protected class SimpleNaiveBayesClassifier extends NaiveBayesClassifier {
        protected FeatureExtractor[] featureExtractors() {
            FeatureExtractor[] ret = new FeatureExtractor[1];
            ret[0] = new IdentityExtractor(featureEncoder);
            return ret;
        }
    }

    @Test public void testSingleItem() {
        NaiveBayesClassifier classifier = new SimpleNaiveBayesClassifier();

        /** Read in training and test data **/
        Set<Pair<String, String>> labeledTrainingData = trainingData(1, 0);
        classifier.train(labeledTrainingData);

        Map<String, String> labeledTestData = testData(1, 0);
        Map<String, String> guessedLabels = classifier.label(labeledTestData.keySet());

        int correct = 0;
        for (String item : labeledTestData.keySet()) {
            if (labeledTestData.get(item).equals(guessedLabels.get(item)))
                correct++;
            assertEquals(1.0, classifier.score(item), 0.00001);
        }

        assertEquals(1, correct);
    }

    @Test public void testSplitTraining() {
        NaiveBayesClassifier classifier = new SimpleNaiveBayesClassifier();

        /** Read in training and test data **/
        Set<Pair<String, String>> labeledTrainingData = trainingData(1, 1);
        classifier.train(labeledTrainingData);

        Map<String, String> labeledTestData = testData(1, 0);
        Map<String, String> guessedLabels = classifier.label(labeledTestData.keySet());

        for (String item : labeledTestData.keySet()) {
            assertEquals(0.5, classifier.score(item), 0.000001);
        }
    }

    @Test public void testMultipleKeys() {
        NaiveBayesClassifier classifier = new SimpleNaiveBayesClassifier();

        /** Read in training and test data **/
        Set<Pair<String, String>> labeledTrainingData = trainingData(3, 2, 1, 5);
        classifier.train(labeledTrainingData);

        Map<String, String> labeledTestData = testData(1, 0, 0, 1);
        Map<String, String> guessedLabels = classifier.label(labeledTestData.keySet());

        int correct = 0;
        for (String item : labeledTestData.keySet()) {
            if (labeledTestData.get(item).equals(guessedLabels.get(item)))
                correct++;
	    //            assertEquals(1.0, classifier.score(item), 0.00001);
        }

        assertEquals(2, correct);
    }    
}
