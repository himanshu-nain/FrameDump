package com.example.framedump;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

    private static ThreadPool instance;
    private ExecutorService executors;

    private ThreadPool() {
        this.executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
    }

    public ExecutorService getPoolService() {
        return this.executors;
    }

    public static ThreadPool getInstance() {
        if(instance == null)
            instance = new ThreadPool();
        return instance;
    }

}
