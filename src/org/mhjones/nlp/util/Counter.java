package org.mhjones.nlp.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.mhjones.nlp.math.DoubleArrays;

public class Counter<E> implements Serializable {
    Map<E, Integer> entriesEncoder;
    E[] entriesDecoder;
    double[] values;

    protected int encode(E key) {
	return entriesEncoder.get(key);
    }

    protected E decode(int enc_key) {
	return entriesDecoder[enc_key];
    }

    public Set<E> keySet() {
	return entriesEncoder.keySet();
    }

    public int size() {
	return entriesEncoder.size();
    }

    public boolean isEmpty() {
	return (size() == 0);
    }
    
    public double getCount(E key) {
	if (encode(key) > values.length)
	    // FIXME: this will break with smoothing
	    return 0.0;
	return values[encode(key)];
    }
    
    public void incrementCount(E key, double val) {
	int enc_key = encode(key);
	values[enc_key] += val;
    }

    public void normalize() {
	double total = DoubleArrays.sum(values);
	DoubleArrays.inPlaceDivide(values, total);
    }

    E argMax() {
	return decode(DoubleArrays.argMax(values));
    }

    double totalCount() {
	return DoubleArrays.sum(values);
    }

    public String toString() {
	String ret = "[ ";
      
	for (int pos = 0; pos < values.length; pos++) ret += pos + " : " + values[pos] + ", ";
      
	ret += "]";
	return ret;
    }
}