package org.mhjones.nlp.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.IdentityHashMap;


public class Encoding<E> implements Serializable {
    Interner<E> interner;
    Map<E,Integer> encoder;
    E[] decoder;

    public int encode(E key) {
	E cKey = interner.intern(key);
	if (encoder.containsKey(cKey)) return encoder.get(cKey);

	int eKey = encoder.size();
	encoder.put(cKey, eKey);

	if (decoder.length == eKey) {
	    E[] newDecoder = (E[]) new Object[decoder.length*2];

	    for (int i = 0; i < newDecoder.length; i++)
		newDecoder[i] = decoder[i];

	    decoder = newDecoder;
	}

	decoder[eKey] = cKey;

	return eKey;
    }

    public E decode(int eKey) {
	return decoder[eKey];
    }

    public boolean containsKey(E key) {
	E cKey = interner.intern(key);
	return encoder.containsKey(cKey);
    }

    public Set<E> keySet() {
	return encoder.keySet();
    }

    public int size() {
	return encoder.size();
    }

    public Encoding(int defaultSize) {
	interner = new Interner<E>();
	encoder = new IdentityHashMap<E,Integer>();
	decoder = (E[]) new Object[defaultSize];
    }

    public Encoding() {
	this(128);
    }

}