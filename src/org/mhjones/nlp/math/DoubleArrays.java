package org.mhjones.nlp.math;

public class DoubleArrays {
    public static double sum(double[] x) {
	double sum = 0.0;

	for (int i = 0; i < x.length; i++)
	    sum += x[i];

	return sum;
    }

    public static void inPlaceDivide(double[] x, double y) {
	for (int i = 0; i < x.length; i++)
	    x[i] /= y;
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
}