package org.mhjones.nlp.util;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.mhjones.nlp.math.DoubleArrays;

public class Counter<E> implements Serializable {
    public Encoding<E> encoding;
    public double[] values;

    boolean logCounter;
    double defaultValue;

    protected int encode(E key) {
	int eKey = encoding.encode(key);

	// resize values
	if (eKey >= values.length)
	    values = DoubleArrays.resizeArray(values, values.length*2);

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
    
    public void incrementCount(E key) {
	values[encode(key)] += 1.0;
    }

    public void incrementCount(E key, double val) {
	values[encode(key)] += val;
    }

    // This is really transforming a set of counts into a distribution of counts,
    // so perhaps this should at least lock the values or somehow signify that it's not for counting anymore?
    public void normalize() {
	double total = totalCount();
	if (logCounter) {
	    DoubleArrays.inPlaceLog(values, 0, encoding.size());
	    DoubleArrays.inPlaceAdd(values, -Math.log(total), 0, encoding.size());
	}
	else DoubleArrays.inPlaceDivide(values, total, 0, encoding.size());
    }

    E argMax() {
	return decode(DoubleArrays.argMax(values, 0, encoding.size()));
    }

    double totalCount() {
	return DoubleArrays.sum(values, 0, encoding.size());
    }

    public String toString() {
	String ret = "[ ";
      
	for (int pos = 0; pos < values.length; pos++) ret += pos + " : " + values[pos] + ", ";
      
	ret += "]";
	return ret;
    }

    public Counter(double[] values, int[] index, int used, Encoding<E> encoding) {
	this.values = DoubleArrays.constantArray(encoding.size(), 0.0);
	this.encoding = encoding;

	for (int i = 0; i < used; i++)
	    this.values[index[i]] = values[i];
    }

    public Counter(int keySetSize, boolean logCounter, Encoding<E> encoding) {
	this.logCounter = logCounter;
	if (logCounter) this.defaultValue = Double.NEGATIVE_INFINITY;
	else this.defaultValue = 0.0;

	this.encoding = encoding;
	values = DoubleArrays.constantArray(keySetSize, defaultValue);
    }

    public Counter(Encoding<E> encoding) {
	this(encoding.size(), false, encoding);
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
