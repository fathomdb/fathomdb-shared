package com.fathomdb;

import java.util.Iterator;

public abstract class SimpleIterator<T> implements Iterator<T> {
	T current;
	boolean calledHasNext;

	protected SimpleIterator() {
		this.calledHasNext = false;
	}

	@Override
	public boolean hasNext() {
		if (!calledHasNext) {
			T next = getNext(current);
			current = next;
			calledHasNext = true;
		}
		return current != null;
	}

	@Override
	public T next() {
		if (!calledHasNext) {
			if (!hasNext()) {
				throw new IllegalStateException();
			}
		}
		calledHasNext = false;
		return current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	protected abstract T getNext(T current);
}
