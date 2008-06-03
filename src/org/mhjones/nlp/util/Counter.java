package org.mhjones.nlp.util;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;


import org.mhjones.nlp.math.DoubleArrays;

public class Counter<E> implements Serializable {
    Encoding<E> encoding;
    double[] values;

    boolean logCounter;
    double defaultValue;

    protected int encode(E key) {
	int eKey = encoding.encode(key);

	if (eKey >= values.length) {
		// resize values
		double[] newValues = DoubleArrays.constantArray(values.length*2, defaultValue);

		for (int i = 0; i < values.length; i++)
		    newValues[i] = values[i];

		values = newValues;
	}

	return eKey;
    }

    protected E decode(int eKey) {
	return encoding.decode(eKey);
    }

    public Set<E> keySet() {
	return encoding.keySet();
    }

    public int size() {
	return encoding.size();
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

    public Counter(int keySetSize, boolean logCounter, Encoding<E> encoding) {
	this.logCounter = logCounter;
	if (logCounter) this.defaultValue = Double.NEGATIVE_INFINITY;
	else this.defaultValue = 0.0;

	this.encoding = encoding;
	values = DoubleArrays.constantArray(keySetSize, defaultValue);
    }

    public Counter(int keySetSize, boolean logCounter) {
	this(keySetSize, logCounter, new Encoding<E>(keySetSize));
    }

    public Counter(int keySetSize) {
	this(keySetSize, false);
    }

    public Counter(boolean logCounter) {
	this(128, logCounter);
    }

    public Counter() {
	this(128, false);
    }
}
