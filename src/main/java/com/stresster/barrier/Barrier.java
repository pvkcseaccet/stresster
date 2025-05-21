package com.stresster.barrier;

import com.stresster.exception.StressterException;
import lombok.Getter;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Logger;

@Getter
public class Barrier
{
    private CyclicBarrier barrier;

    private static final Logger LOGGER = Logger.getLogger(Barrier.class.getName());

    Barrier(BarrierConf barrierConf) throws StressterException
    {
        Util.validate(barrierConf);
        this.barrier = new CyclicBarrier(barrierConf.getNoOfRequests());
    }

    void install() throws BrokenBarrierException, InterruptedException, StressterException
    {
        try
        {
            barrier.await();
        }
        finally
        {
            if (barrier.isBroken())
            {
                barrier.reset();
            }
        }
    }

}
