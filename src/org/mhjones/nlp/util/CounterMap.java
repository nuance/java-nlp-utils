package org.mhjones.nlp.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.System;
import java.util.IdentityHashMap;

import org.mhjones.nlp.math.DoubleArrays;
import org.mhjones.nlp.math.IntegerArrays;
import org.mhjones.nlp.math.DoubleArrays2D;
import org.mhjones.nlp.math.IntegerArrays2D;

import java.util.Set;

public class CounterMap<E,F> implements Serializable {
    public Encoding<E> primaryEncoding;
    public Encoding<F> secondaryEncoding;

    public double[][] values;

    public int[][] secondaryIdx; // Contains the encoded secondary for each location (tells you what is in position 1)
    public IdentityHashMap<Integer,Integer>[] secondaryRevIdx; // Contains the location of the specified encoded secondary (tells you where item 1 is)

    protected int encodePrimary(E key) {
	int eKey = primaryEncoding.encode(key);

	// resize values
	if (eKey >= values.length) {
	    int newSize = values.length*2;
	    values = DoubleArrays2D.resizeArray(values, newSize, 1, 0.0);
	    secondaryIdx = IntegerArrays2D.resizeArray(secondaryIdx, newSize, 1, 1);

	    IdentityHashMap<Integer,Integer>[] temp = (IdentityHashMap<Integer,Integer>[]) Array.newInstance(IdentityHashMap.class, newSize);
	    System.arraycopy(secondaryRevIdx, 0, temp, 0, newSize/2);
	    secondaryRevIdx = temp;
	    for (int i = newSize/2; i < newSize; i++) secondaryRevIdx[i] = new IdentityHashMap<Integer,Integer>();
	}

	return eKey;
    }

    protected E decodePrimary(int eKey) {
	return primaryEncoding.decode(eKey);
    }

    protected int encodeSecondary(F key) {
	int eKey = secondaryEncoding.encode(key);

	// resizing occurs on access to exploit sparsity

	return eKey;
    }

    protected F decodeSecondary(int eKey) {
	return secondaryEncoding.decode(eKey);
    }

    public Set<E> keySet() {
	return primaryEncoding.keySet();
    }

    public int size() {
	return primaryEncoding.size();
    }

    public boolean isEmpty() {
	return (size() == 0);
    }

    public Counter<F> getCounter(E primary) {
	int ePrimary = encodePrimary(primary);
	
	if (DoubleArrays.sum(values[ePrimary]) == 0) {
	    Counter<F> counter = new Counter(secondaryEncoding);
	    counter.normalize();
	    return counter;
	}

	return new Counter<F>(values[ePrimary], secondaryIdx[ePrimary], secondaryRevIdx[ePrimary].size(), secondaryEncoding);
    }

    public double getCount(E primary, F secondary) {
	int ePrimary = encodePrimary(primary);
	int eSecondary = encodeSecondary(secondary);

        if (secondaryRevIdx[ePrimary].get(eSecondary) == null)
	    return 0.0;

	return values[ePrimary][secondaryRevIdx[ePrimary].get(eSecondary)];
    }

    public void incrementCount(E primary, F secondary) {
	this.incrementCount(primary, secondary, 1.0);
    }

    public void incrementCount(E primary, F secondary, double val) {
	int ePrimary = encodePrimary(primary);
	int eSecondary = encodeSecondary(secondary);

        Integer idx = secondaryRevIdx[ePrimary].get(eSecondary);

	// Is it not in the sparse array?
	if (idx == null) {
	    idx = secondaryRevIdx[ePrimary].size();
	    
	    // Do we need to resize the sparse array?
	    if (idx == values[ePrimary].length) {
		// Resize sparse array & indexes
		values[ePrimary] = DoubleArrays.resizeArray(values[ePrimary], values[ePrimary].length*2);
		secondaryIdx[ePrimary] = IntegerArrays.resizeArray(secondaryIdx[ePrimary], secondaryIdx[ePrimary].length*2);
	    }

	    secondaryRevIdx[ePrimary].put(eSecondary, idx);
	    secondaryIdx[ePrimary][idx] = eSecondary;
	}

	values[ePrimary][idx] += val;
    }

    public void normalize() {
	for (int primary = 0; primary < primaryEncoding.size(); primary++) {
	    double totalCount = DoubleArrays.sum(values[primary], 0, secondaryRevIdx[primary].size());
	    DoubleArrays.inPlaceDivide(values[primary], totalCount, 0, secondaryRevIdx[primary].size());
	}
    }

    public F argMax(E primary) {
	int ePrimary = encodePrimary(primary);
	int argMaxIdx = DoubleArrays.argMax(values[ePrimary], 0, secondaryRevIdx[ePrimary].size());

	return decodeSecondary(secondaryIdx[ePrimary][argMaxIdx]);
    }

    public String toString() {
	String ret = "";

	for (int primary = 0; primary < primaryEncoding.size(); primary++) {
	    ret += decodePrimary(primary) + " : ";
	    ret += "[ ";
	    for (int pos = 0; pos < secondaryRevIdx[primary].size(); pos++)
		ret += decodeSecondary(secondaryRevIdx[primary].get(pos)) + " : " + values[primary][pos] + ", ";
	    ret += "]\n";
	}

	return ret;
    }

    public CounterMap(int primaryKeySetSize, int[] secondaryDistribution, Encoding<E> primaryEncoding, Encoding<F> secondaryEncoding) {
	values = new double[primaryKeySetSize][];
	secondaryIdx = new int[primaryKeySetSize][];
	secondaryRevIdx = (IdentityHashMap<Integer, Integer>[]) Array.newInstance(IdentityHashMap.class,primaryKeySetSize);

	for (int i = 0; i < primaryKeySetSize; i++) {
	    values[i] = new double[secondaryDistribution[i]];
	    secondaryIdx[i] = new int[secondaryDistribution[i]];
	    secondaryRevIdx[i] = new IdentityHashMap<Integer, Integer>();
	}

	this.primaryEncoding = primaryEncoding;
	this.secondaryEncoding = secondaryEncoding;
    }

    public CounterMap(int primaryKeySetSize, int secondaryKeySetSize, int[] secondaryDistribution) {
	this(primaryKeySetSize, secondaryDistribution, new Encoding<E>(primaryKeySetSize), new Encoding<F>(secondaryKeySetSize));
    }

    public CounterMap(int primaryKeySetSize, int secondaryKeySetSize) {
	this(primaryKeySetSize, secondaryKeySetSize, IntegerArrays.constantArray(primaryKeySetSize, secondaryKeySetSize));
    }

    public CounterMap(Encoding<E> primaryEncoding, Encoding<F> secondaryEncoding) {
	this(primaryEncoding.size(), IntegerArrays.constantArray(primaryEncoding.size(), secondaryEncoding.size()), primaryEncoding, secondaryEncoding);
    }

    public CounterMap() {
	this(64, 64);
    }

    public CounterMap(CounterMap<E,F> other) {
	this.primaryEncoding = new Encoding<E>(other.primaryEncoding);
	this.secondaryEncoding = new Encoding<F>(other.secondaryEncoding);

	this.values = DoubleArrays2D.copy(other.values);
	this.secondaryIdx = IntegerArrays2D.copy(other.secondaryIdx);
	this.secondaryRevIdx = (IdentityHashMap<Integer,Integer>[]) new Object[other.secondaryRevIdx.length];
	for (int i = 0; i < other.secondaryRevIdx.length; i++)
	    this.secondaryRevIdx[i] = new IdentityHashMap<Integer, Integer>(other.secondaryRevIdx[i]);
    }

}