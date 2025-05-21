package com.stresster.barrier;

import com.stresster.exception.StressterException;
import com.stresster.exception.StressterExceptionStore;

import java.util.concurrent.BrokenBarrierException;

public class BarrierManager
{
    public static Barrier create(String uri, int noOfRequests) throws StressterException
    {
        BarrierConf barrierConf = BarrierConf.builder()
                .uri(uri)
                .noOfRequests(noOfRequests)
                .build();

        return new Barrier(barrierConf);
    }

    public static void install(Barrier barrier) throws StressterException, BrokenBarrierException, InterruptedException
    {
        if (barrier.getBarrier().isBroken())
        {
            throw StressterExceptionStore.BROKEN_BARRIER_PROVIDED._new();
        }
        barrier.install();
    }
}
