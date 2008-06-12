package org.mhjones.nlp.util;

import java.io.Serializable;

import org.mhjones.nlp.math.DoubleArrays;
import org.mhjones.nlp.math.IntegerArrays;
import org.mhjones.nlp.math.DoubleArrays2D;
import org.mhjones.nlp.math.IntegerArrays2D;

import java.util.Set;

public class CounterMap<E,F> implements Serializable {
    public Encoding<E> primaryEncoding;
    public Encoding<F> secondaryEncoding;

    public double[][] values;

    public int[][] secondaryIdx;    // Contains the encoded secondary for each location (tells you what is in position 1)
    public int[][] secondaryRevIdx; // Contains the location of the specified encoded secondary (tells you where item 1 is)
    public int[] used;              // The next position to add items to

    public final int NOT_PRESENT = -1;

    protected int encodePrimary(E key) {
	int eKey = primaryEncoding.encode(key);

	// resize values
	if (eKey >= values.length) {
	    values = DoubleArrays2D.resizeArray(values, values.length*2, 1, 0.0);
	    secondaryIdx = IntegerArrays2D.resizeArray(secondaryIdx, secondaryIdx.length*2, 1, 1);
	    secondaryRevIdx = IntegerArrays2D.resizeArray(secondaryRevIdx, secondaryRevIdx.length*2, 1, 1);
	    used = IntegerArrays.resizeArray(used, used.length*2);
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
	return new Counter<F>(values[ePrimary], secondaryIdx[ePrimary], used[ePrimary], secondaryEncoding);
    }

    public double getCount(E primary, F secondary) {
	int ePrimary = encodePrimary(primary);
	int eSecondary = encodeSecondary(secondary);

        if (secondaryRevIdx[ePrimary][eSecondary] == NOT_PRESENT)
	    return 0.0;

	return values[ePrimary][secondaryRevIdx[ePrimary][eSecondary]];
    }

    public void incrementCount(E primary, F secondary) {
	this.incrementCount(primary, secondary, 1.0);
    }

    public void incrementCount(E primary, F secondary, double val) {
	int ePrimary = encodePrimary(primary);
	int eSecondary = encodeSecondary(secondary);

	int idx = secondaryRevIdx[ePrimary][eSecondary];

	// Is it not in the sparse array?
	if (idx == NOT_PRESENT) {
	    idx = used[ePrimary];
	    used[ePrimary] += 1;

	    // Do we need to resize the sparse array?
	    if (used[ePrimary] == values[ePrimary].length) {
		// Resize sparse array & indexes
		values[ePrimary] = DoubleArrays.resizeArray(values[ePrimary], values[ePrimary].length*2);
		secondaryIdx[ePrimary] = IntegerArrays.resizeArray(secondaryIdx[ePrimary], secondaryIdx[ePrimary].length*2);
		secondaryRevIdx[ePrimary] = IntegerArrays.resizeArray(secondaryRevIdx[ePrimary], secondaryRevIdx[ePrimary].length*2);
	    }

	    secondaryRevIdx[ePrimary][eSecondary] = idx;
	    secondaryIdx[ePrimary][idx] = eSecondary;
	}

	values[ePrimary][idx] += val;
    }

    public void normalize() {
	for (int primary = 0; primary < primaryEncoding.size(); primary++) {
	    double totalCount = DoubleArrays.sum(values[primary], 0, used[primary]);
	    DoubleArrays.inPlaceDivide(values[primary], totalCount, 0, used[primary]);
	}
    }

    public F argMax(E primary) {
	int ePrimary = encodePrimary(primary);
	int argMaxIdx = DoubleArrays.argMax(values[ePrimary], 0, used[ePrimary]);

	return decodeSecondary(secondaryIdx[ePrimary][argMaxIdx]);
    }

    public String toString() {
	String ret = "";

	for (int primary = 0; primary < primaryEncoding.size(); primary++) {
	    ret += decodePrimary(primary) + " : ";
	    ret += "[ ";
	    for (int pos = 0; pos < used[primary]; pos++)
		ret += decodeSecondary(secondaryRevIdx[primary][pos]) + " : " + values[primary][pos] + ", ";
	    ret += "]\n";
	}

	return ret;
    }

    public CounterMap(int primaryKeySetSize, int[] secondaryDistribution, Encoding<E> primaryEncoding, Encoding<F> secondaryEncoding) {
	values = new double[primaryKeySetSize][];
	secondaryIdx = new int[primaryKeySetSize][];
	secondaryRevIdx = new int[primaryKeySetSize][];

	used = new int[primaryKeySetSize];

	for (int i = 0; i < primaryKeySetSize; i++) {
	    values[i] = new double[secondaryDistribution[i]];
	    secondaryIdx[i] = new int[secondaryDistribution[i]];
	    secondaryRevIdx[i] = new int[secondaryDistribution[i]];
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

}