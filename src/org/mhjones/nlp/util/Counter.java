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

    boolean logCounter;
    double defaultValue;

    protected int encode(E key) {
	if (!entriesEncoder.containsKey(key)) {
	    int encKey = entriesEncoder.size();
	    entriesEncoder.put(key, encKey);

	    if (values.length == encKey) {
		// resize values and entriesDecoder
		double[] newValues = DoubleArrays.constantArray(values.length*2, defaultValue);
		E[] newEntriesDecoder = (E[]) new Object[entriesDecoder.length*2];

		for (int i = 0; i < values.length; i++) {
		    newValues[i] = values[i];
		    newEntriesDecoder[i] = entriesDecoder[i];
		}

		values = newValues;
		entriesDecoder = newEntriesDecoder;
	    }

	    entriesDecoder[encKey] = key;
	}

	return entriesEncoder.get(key);
    }

    protected E decode(int encKey) {
	return entriesDecoder[encKey];
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

    // This is really transforming a set of counts into a distribution of counts,
    // so perhaps this should at least lock the values or somehow signify that it's not for counting anymore?
    public void normalize() {
	double total = totalCount();
	if (logCounter) {
	    DoubleArrays.inPlaceLog(values);
	    DoubleArrays.inPlaceAdd(values, -Math.log(total));
	}
	else DoubleArrays.inPlaceDivide(values, total);
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
	this.logCounter = false;
	this.defaultValue = 0.0;

	entriesEncoder = new HashMap<E, Integer>();
	entriesDecoder = (E[]) new Object[keySetSize];
	values = DoubleArrays.constantArray(keySetSize, defaultValue);
    }

    public Counter() {
	this.logCounter = false;
	this.defaultValue = 0.0;

	entriesEncoder = new HashMap<E, Integer>();
	entriesDecoder = (E[]) new Object[128];
	values = DoubleArrays.constantArray(128, defaultValue);
    }

    public Counter(int keySetSize, boolean logCounter) {
	this.logCounter = logCounter;
	if (logCounter) this.defaultValue = Double.NEGATIVE_INFINITY;
	else this.defaultValue = 0.0;

	entriesEncoder = new HashMap<E, Integer>();
	entriesDecoder = (E[]) new Object[keySetSize];
	values = DoubleArrays.constantArray(keySetSize, defaultValue);
    }

    public Counter(boolean logCounter) {
	this.logCounter = logCounter;
	if (logCounter) this.defaultValue = Double.NEGATIVE_INFINITY;
	else this.defaultValue = 0.0;

	entriesEncoder = new HashMap<E, Integer>();
	entriesDecoder = (E[]) new Object[128];
	values = DoubleArrays.constantArray(128, defaultValue);
    }
}
