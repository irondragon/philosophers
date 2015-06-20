package ru.moscow.concurrency.philosophers;

import java.util.concurrent.atomic.AtomicBoolean;


final class Fork {

    private final AtomicBoolean clean = new AtomicBoolean(true);

    boolean isClean() {
        return this.clean.get();
    }

    public void wash() {
        this.clean.set(true);
    }

    public void use() {
        this.clean.set(false);
    }

}
