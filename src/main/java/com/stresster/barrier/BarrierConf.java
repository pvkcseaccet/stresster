package com.stresster.barrier;

import lombok.Builder;
import lombok.Getter;

/**
 * This class helps you build the barrier which you can use
 * to achieve the concurrent processing.
 *
 */
@Getter
@Builder(builderClassName = "Builder")
public class BarrierConf
{
    /**
     * It can be URI in case of Rest APIs, ClassName in case of RMIs <br />
     * or it can be anything to identify a particular group of requests
     */
    public String uniqueGroupID;
    public int noOfRequests;
}
