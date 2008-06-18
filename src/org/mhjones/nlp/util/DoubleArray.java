package org.mhjones.nlp.math;

import java.util.concurrent.LinkedBlockingQueue;

public class DoubleArray {
    double[] array;
    DoubleArrayWorker[] workers;
    Thread[] workerThreads;

    private class DoubleArrayFunction {
        public double result;
        public int resultIdx;

        public void execute(double[] array, int start, int stop) {
            // Default is no-op
        }

        public DoubleArrayFunction() {
            result = 0.0;
            resultIdx = 0;
        }
    }

    private class DoubleArrayWorker implements Runnable {
        double[] array;
        int start, stop; // what part of the array to work in
        public boolean finish;
        LinkedBlockingQueue<DoubleArrayFunction> queue;
        public double result;
        public int resultIdx;

        public void run() {
            DoubleArrayFunction currentFunction = null;

            while(true) {
                if (finish && queue.peek() == null) return;
                try {
                    currentFunction = queue.take();
                } catch (InterruptedException ignored) {}
                currentFunction.execute(array, start, stop);
                result = currentFunction.result;
                resultIdx = currentFunction.resultIdx;
            }
        }

        public void dispatch(DoubleArrayFunction function) {
            queue.add(function);
        }
        
        public DoubleArrayWorker(double[] array, int start, int stop) {
            this.array = array;
            this.start = start;
            this.stop = stop;
            queue = new LinkedBlockingQueue<DoubleArrayFunction>();
            finish = false;
        }
    }

    // Flow Control

    public void sync() {
        for (int i = 0; i < workerThreads.length; i++) {
            workers[i].finish = true;
            workers[i].dispatch(new DoubleArrayFunction()); 
            // Otherwise the thread could hang in queue.take() and not receive the finish request
            try {
                workerThreads[i].join();
            } catch (InterruptedException ignored) {}
            workers[i].finish = false;
        }
        for (int i = 0; i < workerThreads.length; i++) workerThreads[i] = new Thread(workers[i]);
    }

    protected void dispatch(DoubleArrayFunction func) {
        for (DoubleArrayWorker worker : workers)
            worker.dispatch(func);
    }

    protected double[] lastResult() {
        double[] result = new double[workers.length];
        
        for (int i = 0; i < workers.length; i++)
            result[i] = workers[i].result;
            
        return result;
    }
    
    protected int[] lastResultIdx() {
        int[] resultIdx = new int[workers.length];
        
        for (int i = 0; i < workers.length; i++)
            resultIdx[i] = workers[i].resultIdx;
            
        return resultIdx;
    }

    // Synchronous methods

    private class Sum extends DoubleArrayFunction {
        public void execute(double[] array, int start, int stop) {
            result = DoubleArrays.sum(array, start, stop);
        }
        
        public Sum() {
        }
    }

    public double sum() {
        dispatch(new Sum());
        sync();
        return DoubleArrays.sum(lastResult());
    }
    
    private class ArgMax extends DoubleArrayFunction {
        public void execute(double[] array, int start, int stop) {
            resultIdx = start;
            result = array[start];
            
            for (int i = start++; i < stop; i++) {
                if (array[i] <= result) continue;
                result = array[i];
                resultIdx = i;
            }
        }
        
        public ArgMax() {
        }
    }
    
    public int argMax() {
        dispatch(new ArgMax());
        sync();
        
        double[] results = lastResult();
        int[] resultIdx = lastResultIdx();
        
        double max = results[0];
        int argMax = resultIdx[0];
        
        for (int i = 1; i < results.length; i++) {
            if (results[i] <= max) continue;
            max = results[i];
            argMax = resultIdx[i];
        }
        
        return argMax;
    }
    
    public double[] result() {
        sync();
        return array;
    }
    
    // Async methods
    
    private class ArrayAdd extends DoubleArrayFunction {
        double[] other;

        public void execute(double[] array, int start, int stop) {
            DoubleArrays.inPlaceAdd(array, other, start, stop);
        }
        
        public ArrayAdd(double[] other) {
            this.other = other;
        }
    }
    
    public void add(double[] other) {
        dispatch(new ArrayAdd(other));
    }

    private class Add extends DoubleArrayFunction {
        double constant;

        public void execute(double[] array, int start, int stop) {
            DoubleArrays.inPlaceAdd(array, constant, start, stop);
        }

        public Add(double constant) {
            this.constant = constant;
        }
    }
    
    public void add(double constant) {
        dispatch(new Add(constant));
    }

    private class ArraySubtract extends DoubleArrayFunction {
        double[] other;

        public void execute(double[] array, int start, int stop) {
            DoubleArrays.inPlaceSubtract(array, other, start, stop);
        }
        
        public ArraySubtract(double[] other) {
            this.other = other;
        }
    }

    public void subtract(double[] other) {
        dispatch(new ArraySubtract(other));
    }

    private class ArrayMultiply extends DoubleArrayFunction {
        double[] other;

        public void execute(double[] array, int start, int stop) {
            DoubleArrays.inPlaceMultiply(array, other, start, stop);
        }
        
        public ArrayMultiply(double[] other) {
            this.other = other;
        }
    }
    
    public void multiply(double[] other) {
        dispatch(new ArrayMultiply(other));
    }

    private class Multiply extends DoubleArrayFunction {
        double constant;

        public void execute(double[] array, int start, int stop) {
            DoubleArrays.inPlaceMultiply(array, constant, start, stop);
        }

        public Multiply(double constant) {
            this.constant = constant;
        }
    }

    public void multiply(double constant) {
        dispatch(new Multiply(constant));
    }

    private class Divide extends DoubleArrayFunction {
        double constant;

        public void execute(double[] array, int start, int stop) {
            DoubleArrays.inPlaceDivide(array, constant, start, stop);
        }

        public Divide(double constant) {
            this.constant = constant;
        }
    }

    public void divide(double other) {
        dispatch(new Divide(other));
    }
    
    private class Log extends DoubleArrayFunction {
        public void execute(double[] array, int start, int stop) {
            DoubleArrays.inPlaceLog(array, start, stop);
        }

        public Log() {
        }
    }
    
    public void log() {
        dispatch(new Log());
    }

    public DoubleArray(int length, int value, int numThreads) {
        array = DoubleArrays.constantArray(length, value);
        workers = new DoubleArrayWorker[numThreads];
        workerThreads = new Thread[numThreads];

        int cut = length / numThreads;
        int overrun = length % numThreads;
        
        for (int i = 0; i < numThreads; i++) {
            int start = cut * i;
            int stop = cut * (i+1);
            if (i == numThreads) stop += overrun;
            
            workers[i] = new DoubleArrayWorker(array, start, stop);
            workerThreads[i] = new Thread(workers[i]);
            workerThreads[i].run();
        }
    }
}