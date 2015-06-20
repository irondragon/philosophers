package ru.moscow.concurrency.philosophers;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Philosopher implements Runnable {

    final protected int id;

    protected volatile Fork left;
    protected volatile Fork right;

    private final int maxEats;

    private final CountDownLatch latch;

    private final Lock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    private Philosopher leftPhilosopher;

    private Philosopher rightPhilosopher;

    private static final int MAX_SLEEP = 100;
    private static final int MIN_SLEEP = 10;

    protected Philosopher(CountDownLatch latch, Fork left, Fork right,
                          int maxEats, int id) {
        this.latch = latch;
        this.left = left;
        this.right = right;
        this.maxEats = maxEats;
        this.id = id;
    }

    public void setLeftPhilosopher(Philosopher leftPhilosopher) {
        this.leftPhilosopher = leftPhilosopher;
    }

    public void setRightPhilosopher(Philosopher rightPhilosopher) {
        this.rightPhilosopher = rightPhilosopher;
    }

    public void run() {
        try {
            this.latch.countDown();
            this.latch.await();
            int mealsEaten = 0;
            while (mealsEaten < maxEats) {
                eat(mealsEaten);
                mealsEaten++;
            }
            System.out.println("Philosopher " + id + " eaten enough");
        } catch (InterruptedException ignored) {
        }

    }

    protected void eat(int mealsEaten) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (this.left != null && this.right == null) {
                condition.await(100, TimeUnit.MILLISECONDS);
                if (this.right == null) {
                    this.right = this.rightPhilosopher.getLeftFork();
                }
            }
            if (this.left == null) {
                this.left = this.leftPhilosopher.getRightFork();
            }
            if (this.right == null) {
                this.right = this.rightPhilosopher.getLeftFork();
            }
            System.out.println("Philosopher " + id + " eating meal #" + (mealsEaten + 1));
            left.use();
            right.use();
        } finally {
            lock.unlock();
        }

    }

    Fork getLeftFork() throws InterruptedException {
        return getFork(true);
    }

    Fork getRightFork() throws InterruptedException {
        return getFork(false);
    }

    private Fork getFork(boolean isLeft) throws InterruptedException {
        final Fork fork = isLeft ? this.left : this.right;
        lock.lockInterruptibly();
        try {
            if (isLeft) {
                left = null;
            } else {
                right = null;
            }
            fork.wash();
            condition.signalAll();
            return fork;
        } finally {
            lock.unlock();
        }
    }

    protected void randomSleep() throws InterruptedException {
        Thread.sleep((long) (new Random().nextInt(MAX_SLEEP - MIN_SLEEP) + MIN_SLEEP));
    }
}
