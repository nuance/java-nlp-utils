package org.mhjones.nlp.util;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.mhjones.nlp.math.DoubleArrays;

public class Counter<E> implements Serializable {
    Map<E, Integer> entriesEncoder;
    E[] entriesDecoder;
    double[] values;

    protected int encode(E key) {
	if (!entriesEncoder.containsKey(key)) {
	    int enc_key = entriesEncoder.size();
	    entriesEncoder.put(key, enc_key);

	    if (values.length == enc_key) {
		// resize values and entriesDecoder
		double[] newValues = DoubleArrays.constantArray(values.length*2, 0.0);
		E[] newEntriesDecoder = (E[]) new Object[entriesDecoder.length*2];

		for (int i = 0; i < values.length; i++) {
		    newValues[i] = values[i];
		    newEntriesDecoder[i] = entriesDecoder[i];
		}

		values = newValues;
		entriesDecoder = newEntriesDecoder;
	    }

	    entriesDecoder[enc_key] = key;
	}

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
	return values[encode(key)];
    }
    
    public void incrementCount(E key, double val) {
	values[encode(key)] += val;
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

    public Counter(int keySetSize) {
	entriesEncoder = new HashMap<E, Integer>();
	entriesDecoder = (E[]) new Object[keySetSize];
	values = DoubleArrays.constantArray(keySetSize, 0.0);
    }

    public Counter() {
	entriesEncoder = new HashMap<E, Integer>();
	entriesDecoder = (E[]) new Object[128];
	values = DoubleArrays.constantArray(128, 0.0);
    }

}
