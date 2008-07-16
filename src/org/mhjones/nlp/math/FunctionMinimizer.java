package org.mhjones.nlp.math;

import org.mhjones.nlp.util.Pair;

public class FunctionMinimizer {

    private Function function;

    boolean iterationCap;
    int iterations;
    double convergence;

    public double[] minimize(double[] start) {
	double[] point = start;
	boolean done = false;
	double lastValue = 0.0; // this initial value is never used
	double stepSize = 0.1;
	int iteration = 0;

	while (!done) {
	    System.out.println("*** Starting gradient descent iteration " + iteration + " ***");
    	    Pair<Double,double[]> valueAndGradient = function.valueAndGradient(point);

	    // stupid gradient descent - subtract the gradient * stepSize from the function
	    double[] scaledGradient = DoubleArrays.multiply(valueAndGradient.getSecond(), stepSize);
	    DoubleArrays.inPlaceSubtract(point, scaledGradient);

	    iteration++;
	    
	    done = iterationCap && iteration == iterations;
	    if (iteration == 1) done |= !iterationCap && (Math.abs(valueAndGradient.getFirst()-lastValue) < convergence);
	    lastValue = valueAndGradient.getFirst();
	}
	
	return start;
    }
    
    public double[] minimize() {
	// no start point, so minimize from a random point
	return minimize(DoubleArrays.uniformRandomArray(function.dimension(), function.range()));
    }

    public FunctionMinimizer(Function function, int iterations) {
	this.function = function;
	this.iterations = iterations;
	this.iterationCap = true;
    }

    public FunctionMinimizer(Function function, double convergence) {
	this.function = function;
	this.convergence = convergence;
	this.iterationCap = false;
    }
    
}