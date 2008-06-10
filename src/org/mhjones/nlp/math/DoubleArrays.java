package org.mhjones.nlp.math;

import java.util.Arrays;

public class DoubleArrays {
    public static double sum(double[] x) {
	double sum = 0.0;

	for (int i = 0; i < x.length; i++)
	    sum += x[i];

	return sum;
    }

    public static double sum(double[] x, int start, int length) {
	double sum = 0.0;

	for (int i = start; i < length+start; i++)
	    sum += x[i];

	return sum;
    }

    public static void inPlaceAdd(double[] x, double y) {
	for (int i = 0; i < x.length; i++)
	    x[i] += y;
    }

    public static void inPlaceAdd(double[] x, double y, int start, int length) {
	for (int i = start; i < length+start; i++)
	    x[i] += y;
    }

    public static void inPlaceAdd(double[] x, double[] y) {
	if (x.length != y.length) throw new IllegalArgumentException();
	
	for (int i = 0; i < x.length; i++)
	    x[i] += y[i];
    }

    public static void inPlaceAdd(double[] x, double[] y, int start, int length) {
	if (x.length != y.length) throw new IllegalArgumentException();
	
	for (int i = start; i < length+start; i++)
	    x[i] += y[i];
    }

    public static void inPlaceDivide(double[] x, double y) {
	for (int i = 0; i < x.length; i++)
	    x[i] /= y;
    }

    public static void inPlaceDivide(double[] x, double y, int start, int length) {
	for (int i = start; i < length+start; i++)
	    x[i] /= y;
    }

    public static void inPlaceLog(double[] x) {
	for (int i = 0; i < x.length; i++)
	    x[i] = Math.log(x[i]);
    }

    public static void inPlaceLog(double[] x, int start, int length) {
	for (int i = start; i < length+start; i++)
	    x[i] = Math.log(x[i]);
    }

    public static int argMax(double[] x) {
	int arg = 0;
	double val = x[0];

	for (int i = 1; i < x.length; i++) {
	    if (x[i] > val) {
		arg = i;
		val = x[i];
	    }
	}

	return arg;
    }
    
    public static int argMax(double[] x, int start, int length) {
	int arg = start;
	double val = x[start];

	for (int i = start+1; i < length+start; i++) {
	    if (x[i] > val) {
		arg = i;
		val = x[i];
	    }
	}

	return arg;
    }
    public static double[] resizeArray(double[] array, int newLength) {
	double[] newArray = new double[newLength];

	if (newLength > array.length)
	    System.arraycopy(array, 0, newArray, 0, array.length);
	else
	    System.arraycopy(array, 0, newArray, 0, newLength);

	return newArray;
    }

    public static double[] constantArray(int length, double defaultValue) {
	double[] ret = new double[length];
	
	Arrays.fill(ret, defaultValue);

	return ret;
    }

}
