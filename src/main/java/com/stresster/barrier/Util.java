package com.stresster.barrier;

import com.stresster.exception.StressterException;
import com.stresster.exception.StressterExceptionStore;

public class Util {
    static void validate(BarrierConf barrierConf) throws StressterException
    {
        if (barrierConf == null)
        {
            throw StressterExceptionStore.CONFIGURE_BARRIER_BEFORE_INSTALLING._new();
        }

        if (2 > barrierConf.getNoOfRequests())
        {
            throw StressterExceptionStore.ENSURE_TWO_OR_MORE_REQUESTS._new();
        }
    }
}
