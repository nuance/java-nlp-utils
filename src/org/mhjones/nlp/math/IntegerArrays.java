package org.mhjones.nlp.math;

import java.util.Arrays;

public class IntegerArrays {
    public static int sum(int[] x) {
	int sum = 0;

	for (int i = 0; i < x.length; i++)
	    sum += x[i];

	return sum;
    }

    public static int sum(int[] x, int start, int length) {
	int sum = 0;

	for (int i = start; i < length+start; i++)
	    sum += x[i];

	return sum;
    }

    public static void inPlaceAdd(int[] x, int y) {
	for (int i = 0; i < x.length; i++)
	    x[i] += y;
    }

    public static void inPlaceAdd(int[] x, int y, int start, int length) {
	for (int i = start; i < length+start; i++)
	    x[i] += y;
    }

    public static void inPlaceAdd(int[] x, int[] y) {
	if (x.length != y.length) throw new IllegalArgumentException();
	
	for (int i = 0; i < x.length; i++)
	    x[i] += y[i];
    }

    public static void inPlaceAdd(int[] x, int[] y, int start, int length) {
	if (x.length != y.length) throw new IllegalArgumentException();
	
	for (int i = start; i < length+start; i++)
	    x[i] += y[i];
    }

    public static void inPlaceDivide(int[] x, int y) {
	for (int i = 0; i < x.length; i++)
	    x[i] /= y;
    }

    public static void inPlaceDivide(int[] x, int y, int start, int length) {
	for (int i = start; i < length+start; i++)
	    x[i] /= y;
    }

    public static int argMax(int[] x) {
	int arg = 0;
	int val = x[0];

	for (int i = 1; i < x.length; i++) {
	    if (x[i] > val) {
		arg = i;
		val = x[i];
	    }
	}

	return arg;
    }
    
    public static int argMax(int[] x, int start, int length) {
	int arg = start;
	int val = x[start];

	for (int i = start+1; i < length+start; i++) {
	    if (x[i] > val) {
		arg = i;
		val = x[i];
	    }
	}

	return arg;
    }
    public static int[] resizeArray(int[] array, int newLength) {
	int[] newArray = new int[newLength];

	if (newLength > array.length)
	    System.arraycopy(array, 0, newArray, 0, array.length);
	else
	    System.arraycopy(array, 0, newArray, 0, newLength);

	return newArray;
    }

    public static int[] constantArray(int length, int defaultValue) {
	int[] ret = new int[length];
	
	Arrays.fill(ret, defaultValue);

	return ret;
    }

}
