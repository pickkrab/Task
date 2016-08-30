package ru.sbt.task;

import java.util.concurrent.Callable;

public class Task<T> {
    private final Callable<? extends T> callable;
    private final Object lock = new Object();
    private volatile T result;
    private volatile boolean already_yet;
    private volatile boolean _exception;
    private volatile RuntimeException exception;
    private volatile boolean first_thread;

    public Task(Callable<? extends T> callable) {
        this.callable = callable;
    }

    public T get() {
        if (already_yet) return get_result();
        if (allow_to_do()) return compute();
        synchronized (this) {
            while (!already_yet) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread is interrupt");
                }
            }
        }
        return get_result();
    }

    private boolean allow_to_do() {
        synchronized (lock) {
            if (first_thread) {
                first_thread = false;
                return true;
            } else return false;
        }
    }

    private T compute() {
        try {
            result = callable.call();
        } catch (Exception e) {
            exception = new CallableException("Exception during compute result");
            _exception = true;
        }
        already_yet = true;
        synchronized (this) {
            notifyAll();
        }
        return get_result();
    }

    private T get_result() {
        if (_exception) throw exception;
        return result;
    }
}
