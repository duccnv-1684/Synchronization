package com.ducnguyen.synchronization.synchronizationalgorithm.centralization;

import java.util.ArrayList;
import java.util.Collection;

final class CentralizationRequestQueue<T> extends ArrayList<T> {
    private OnQueueChangeListener mListener;

    CentralizationRequestQueue(int initialCapacity, OnQueueChangeListener listener) {
        super(initialCapacity);
        mListener = listener;
    }

    CentralizationRequestQueue(OnQueueChangeListener listener) {
        mListener = listener;
    }

    CentralizationRequestQueue(Collection<? extends T> c, OnQueueChangeListener listener) {
        super(c);
        mListener = listener;
    }

    @Override
    public boolean add(T t) {
        mListener.onDataChanged();
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
        mListener.onDataChanged();
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        mListener.onDataChanged();
        return super.addAll(c);
    }


    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        mListener.onDataChanged();
        return super.addAll(index, c);
    }

    @Override
    public T remove(int index) {
        mListener.onDataChanged();
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        mListener.onDataChanged();
        return super.remove(o);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        mListener.onDataChanged();
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        mListener.onDataChanged();
        return super.removeAll(c);
    }

    @Override
    public void clear() {
        mListener.onDataChanged();
        super.clear();
    }


    public interface OnQueueChangeListener {
        void onDataChanged();
    }
}
