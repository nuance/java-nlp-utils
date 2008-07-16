package org.mhjones.nlp.math;

import org.mhjones.nlp.util.Pair;

public abstract class Function {
    private double[] lastValuePoint;
    private double valueCache;

    private double[] lastGradientPoint;
    private double[] gradientCache;

    // Implement these methods without considering cacheing values
    abstract protected double calculateValue(double[] point);
    abstract protected double[] calculateGradient(double[] point);
    abstract protected Pair<Double,double[]> calculateValueAndGradient(double[] point);

    abstract public int dimension();
    abstract public Pair<Double, Double> range();
    
    public double value(double[] point) {
	if (lastValuePoint != point) {
	    valueCache = calculateValue(point);
	    lastValuePoint = point;
	}

	return valueCache;
    }

    public double[] gradient(double[] point) {
	if (lastGradientPoint != point) {
	    gradientCache = calculateGradient(point);
	    lastGradientPoint = point;
	}

	return gradientCache;
    }

    public Pair<Double, double[]> valueAndGradient(double[] point) {
	Pair<Double, double[]> valAndGrad;

	if (point != lastValuePoint && point != lastGradientPoint) {
	    valAndGrad = calculateValueAndGradient(point);
	    lastValuePoint = point;
	    lastGradientPoint = point;
	}
	else
	    valAndGrad = new Pair<Double, double[]>(valueCache, gradientCache);

	return valAndGrad;	
    }
    
}
