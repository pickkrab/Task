package ru.sbt.task;

public class CallableException extends RuntimeException {
    public CallableException(String s) {
        super("Exception during running Callable");
    }
}
