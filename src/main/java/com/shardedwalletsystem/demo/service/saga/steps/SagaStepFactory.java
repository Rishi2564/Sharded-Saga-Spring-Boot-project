package com.shardedwalletsystem.demo.service.saga.steps;

import com.shardedwalletsystem.demo.service.saga.SagaStepInterface;

import java.util.Map;

public class SagaStepFactory {
    private Map<String, SagaStepInterface> sagaStepMap;
    public SagaStepInterface getStepName(String stepName){
        return sagaStepMap.get(stepName);
    }
}
