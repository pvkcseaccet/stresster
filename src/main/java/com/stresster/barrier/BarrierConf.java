package com.stresster.barrier;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
public class BarrierConf
{
    public String uri;
    public int noOfRequests;
}
