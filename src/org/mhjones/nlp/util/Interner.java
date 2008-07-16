package org.mhjones.nlp.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Interner<E> implements Serializable {
    Map<E,E> canonicalMap;

    public E intern(E item) {
	E cItem = canonicalMap.get(item);

	if (cItem == null) {
	    canonicalMap.put(item, item);
	    cItem = item;
	}

	return cItem;
    }

    public int size() {
	return canonicalMap.size();
    }

    public Interner(Interner<E> other) {
	this.canonicalMap = new HashMap<E,E>(other.canonicalMap);
    }
    
    public Interner() {
	canonicalMap = new HashMap<E,E>();
    }
}