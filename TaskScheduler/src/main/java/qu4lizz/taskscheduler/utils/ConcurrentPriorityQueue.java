package qu4lizz.taskscheduler.utils;

import java.util.*;

public class ConcurrentPriorityQueue<E> extends PriorityQueue<E> {
    private final Object lock = new Object();

    public ConcurrentPriorityQueue(Comparator<? super E> comparator) {
        super(comparator);
    }

    public boolean add(E e) {
        synchronized (lock) {
            return super.add(e);
        }
    }
    public E poll() {
        synchronized (lock) {
            return super.poll();
        }
    }
    public E peek() {
        synchronized (lock) {
            return super.peek();
        }
    }
    public boolean isEmpty() {
        synchronized (lock) {
            return super.isEmpty();
        }
    }
    public int size() {
        synchronized (lock) {
            return super.size();
        }
    }
    public boolean contains(Object e) {
        synchronized (lock) {
            return super.contains(e);
        }
    }
    public boolean remove(Object e) {
        synchronized (lock) {
            return super.remove(e);
        }
    }
    public void clear() {
        synchronized (lock) {
            super.clear();
        }
    }
    public boolean offer(E e) {
        synchronized (lock) {
            return super.offer(e);
        }
    }

}
