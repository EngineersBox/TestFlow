package com.engineersbox.testflow.util

import org.codehaus.groovy.util.IteratorBufferedIterator

import java.util.function.Consumer

class SeekableCollection<T> implements Iterator<T> {

    private final List<T> inner;
    private int index;

    SeekableCollection(final List<T> inner) {
        this.inner = inner;
        this.index = 0;
    }

    @Override
    boolean hasNext() {
        return this.index < Math.max(inner.size() - 1, 0);
    }

    @Override
    T next() {
        final T nextValue = this.inner[this.index];
        this.index++;
        return nextValue;
    }

    @Override
    void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    void forEachRemaining(final Consumer<? super T> action) {
        for (int i = this.index; i < this.inner.size(); i++) {
            action.accept(this.inner[i]);
        }
    }

    void seekRelative(final int offset) {
        this.index = Math.max(this.index + offset, 0);
    }

    @Override
    T getAt(final int idx) {
        return (T) this.inner[idx];
    }

    int size() {
        this.inner.size();
    }
}
