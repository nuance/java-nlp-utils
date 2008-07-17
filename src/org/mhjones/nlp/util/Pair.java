package org.mhjones.nlp.util;

public class Pair<E,F> {
    private E first;
    private F second;

    public E getFirst() {
	return first;
    }

    public F getSecond() {
	return second;
    }

    public Pair(E first, F second) {
	this.first = first;
	this.second = second;
    }
}