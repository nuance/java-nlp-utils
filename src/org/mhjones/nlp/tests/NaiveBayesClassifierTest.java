package org.mhjones.nlp.tests;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;
import org.mhjones.nlp.examples.NaiveBayesClassifier;
import org.mhjones.nlp.util.FeatureExtractor;

public class NaiveBayesClassifierTest extends TestCase {
    private Map<String, String> testData(int spam, int ham) {
        Map<String, String> pairs = new HashMap<String, String>();
        for (int i = 0; i < spam; i++)
            pairs.put("porn", "spam");
        for (int i = 0; i < ham; i++)
            pairs.put("porn", "ham");
        return pairs;
    }

    private Map<String, String> trainingData(int spam, int ham) {
        Map<String, String> pairs = new HashMap<String, String>();
        for (int i = 0; i < spam; i++)
            pairs.put("porn", "spam");
        for (int i = 0; i < ham; i++)
            pairs.put("porn", "ham");
        return pairs;
    }

    protected class SimpleNaiveBayesClassifier extends NaiveBayesClassifier {
        protected FeatureExtractor[] featureExtractors() {
            FeatureExtractor[] ret = new FeatureExtractor[1];
            ret[0] = new IdentityExtractor(featureEncoder);
            return ret;
        }
    }

    private NaiveBayesClassifier classifier;

    @Test public void testSingleItem() {
        classifier = new SimpleNaiveBayesClassifier();

        /** Read in training and test data **/
        Map<String, String> labeledTrainingData = trainingData(1, 0);
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
        classifier = new SimpleNaiveBayesClassifier();

        /** Read in training and test data **/
        Map<String, String> labeledTrainingData = trainingData(1, 1);
        classifier.train(labeledTrainingData);

        Map<String, String> labeledTestData = testData(1, 0);
        Map<String, String> guessedLabels = classifier.label(labeledTestData.keySet());

        int correct = 0;
        for (String item : labeledTestData.keySet()) {
            if (labeledTestData.get(item).equals(guessedLabels.get(item)))
                correct++;
            assertEquals(0.5, classifier.score(item), 0.000001);
        }
    }
}
