package com.ducnguyen2102.videosynchronization;

import java.util.ArrayList;
import java.util.Collection;

public class AccessRequestQueue<T> extends ArrayList<T> {
    private OnQueueChangeListener mListener;

    public AccessRequestQueue(int initialCapacity, OnQueueChangeListener listener) {
        super(initialCapacity);
        mListener = listener;
    }

    public AccessRequestQueue(OnQueueChangeListener listener) {
        mListener = listener;
    }

    public AccessRequestQueue(Collection<? extends T> c, OnQueueChangeListener listener) {
        super(c);
        mListener = listener;
    }

    @Override
    public boolean add(T t) {
        mListener.onItemChanged();
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
        mListener.onItemChanged();
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        mListener.onItemChanged();
        return super.addAll(c);
    }


    @Override
    public boolean addAll(int index,Collection<? extends T> c) {
        mListener.onItemChanged();
        return super.addAll(index, c);
    }

    @Override
    public T remove(int index) {
        mListener.onItemChanged();
        return super.remove(index);
    }

    @Override
    public boolean remove( Object o) {
        mListener.onItemChanged();
        return super.remove(o);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        mListener.onItemChanged();
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        mListener.onItemChanged();
        return super.removeAll(c);
    }

    @Override
    public void clear() {
        mListener.onItemChanged();
        super.clear();
    }


    public interface OnQueueChangeListener {
        void onItemChanged();
    }
}
