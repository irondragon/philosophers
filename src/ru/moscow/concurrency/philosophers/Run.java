package ru.moscow.concurrency.philosophers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Run {

    static final int count = 100;
    static final int eatingCount = 50;

    public static void main(String[] args) throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch(count);

        final List<Fork> forks = new ArrayList<Fork>();
        for (int i = 0; i < count; i++) {
            forks.add(new Fork());
        }

        final List<Philosopher> philosophers = new ArrayList<Philosopher>();

        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                philosophers
                        .add(new Philosopher(countDownLatch, forks.get(i), i + 1 == count ? null : forks.get(i + 1), eatingCount, i + 1));
            } else {
                philosophers
                        .add(new Philosopher(countDownLatch, null, null, eatingCount, i + 1));
            }

        }

        for (int i = 0; i < count; i++) {
            int rightIndex = i + 1;
            if (rightIndex == count) {
                rightIndex = 0;
            }

            int leftIndex = i - 1;
            if (leftIndex == -1) {
                leftIndex = count - 1;
            }

            philosophers.get(i).setLeftPhilosopher(philosophers.get(leftIndex));
            philosophers.get(i).setRightPhilosopher(philosophers.get(rightIndex));
        }

        final ExecutorService ex = Executors.newCachedThreadPool();
        final CompletionService cs = new ExecutorCompletionService(ex);

        for (Runnable philosopher : philosophers) {
            cs.submit(philosopher, null);
        }

        for (int i = 0; i < count; i++) {
            final Future<Void> future = cs.take();
            try {
                future.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
                ex.shutdownNow();
                break;
            }
        }
        ex.shutdown();
    }

}
