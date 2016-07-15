package com.getsentry.test;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class EmptyEnumeration<E> implements Enumeration<E> {

    @Override
    public boolean hasMoreElements() {
        return false;
    }

    @Override
    public E nextElement() {
        throw new NoSuchElementException();
    }
}
