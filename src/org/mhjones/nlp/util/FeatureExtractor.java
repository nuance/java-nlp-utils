package org.mhjones.nlp.util;

public interface FeatureExtractor<E> {
    public int[] extractFeatures(E datum);
}
