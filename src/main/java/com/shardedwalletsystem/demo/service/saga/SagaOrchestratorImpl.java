package com.shardedwalletsystem.demo.service.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shardedwalletsystem.demo.exceptions.ResourceNotFoundException;
import com.shardedwalletsystem.demo.exceptions.SagaException;
import com.shardedwalletsystem.demo.model.SagaInstance;
import com.shardedwalletsystem.demo.model.SagaStatus;
import com.shardedwalletsystem.demo.model.SagaStep;
import com.shardedwalletsystem.demo.model.StepStatus;
import com.shardedwalletsystem.demo.repository.SagaInstanceRepository;
import com.shardedwalletsystem.demo.repository.SagaOrchestrator;
import com.shardedwalletsystem.demo.repository.SagaStepRepository;
import com.shardedwalletsystem.demo.service.saga.steps.SagaStepFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator {
    private final SagaStepRepository sagaStepRepository;
    private final SagaInstanceRepository sagaInstanceRepository;

    private final ObjectMapper objectMapper;
    private final SagaStepFactory sagaStepFactory;

    @Override
    @Transactional
    public Long startSaga(SagaContext context){
        if(context==null || context.getData()==null || context.getData().isEmpty()){
            throw new SagaException("Saga context cannnot be null or empty");
        }
        try{
            String contextJson=objectMapper.writeValueAsString(context.getData());

            SagaInstance sagaInstance=SagaInstance.builder()
                    .context(contextJson)
                    .status(SagaStatus.STARTED)
                    .build();
            sagaInstance= sagaInstanceRepository.save(sagaInstance);
            log.info("Saga started successfully with id {}",sagaInstance.getId());
            return sagaInstance.getId();
        } catch(JsonProcessingException e){
            log.error("Error serializing saga context",e);
            throw new SagaException("Error serializing saga context",e);
        }catch(DataAccessException e){
            log.error("Database error while starting saga",e);
            throw new SagaException("Failed to start saga due to database error",e);
        }
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean executeStep(Long sagaInstanceId, String stepName ){
        log.info("Executing step '{}' for saga instance {}",stepName,sagaInstanceId);
        if(sagaInstanceId==null){
            throw new IllegalArgumentException("Saga instance id cannot be null");
        }
        if(stepName==null|| stepName.trim().isEmpty()){
            throw new IllegalArgumentException("Step name cannot be null or empty");
        }

        try{
            SagaInstance sagaInstance=sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(()->new ResourceNotFoundException("Saga instance not found with id: "+sagaInstanceId));
            if(sagaInstance.getStatus()==SagaStatus.FAILED||sagaInstance.getStatus()==SagaStatus.COMPENSATED||sagaInstance.getStatus()==SagaStatus.COMPENSATING){
                log.warn("Cannot execute step for saga in {} state", sagaInstance.getStatus());
                return false;
            }
            SagaStepInterface step= sagaStepFactory.getStepName(stepName);
            if(step==null){
                log.error("Saga step implementation not found for step name:{}",stepName);
                throw new SagaException("Saga step implementation not found for step name:"+stepName);
            }
            SagaStep sagaStepDB=sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId,stepName, StepStatus.PENDING)
                    .orElseGet(()->{
                        var completed = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId,stepName,StepStatus.COMPLETED);
                        if(completed.isPresent()){
                            log.info("Step '{}' already completed for saga {}",stepName,sagaInstanceId);
                            return null;
                        }
                        return SagaStep.builder()
                                .sagaInstanceId(sagaInstanceId)
                                .stepName(stepName)
                                .status(StepStatus.PENDING)
                                .build();
                    });
            if(sagaStepDB==null){
                return true;
            }
            if(sagaStepDB.getId()==null){
                sagaStepDB=sagaStepRepository.save(sagaStepDB);
            }
            SagaContext sagaContext=parseSagaContext(sagaInstance.getContext());
            sagaStepDB.markAsRunning();
            sagaStepRepository.save(sagaStepDB);
            boolean success=step.execute(sagaContext);

            if(success){
                sagaStepDB.markAsCompleted();
                sagaStepRepository.save(sagaStepDB);
                sagaInstance.setCurrentStep(stepName);
                sagaInstance.markAsRunning();
                String updatedContext=objectMapper.writeValueAsString(sagaContext.getData());
                sagaInstance.setContext(updatedContext);
                sagaInstanceRepository.save(sagaInstance);

                log.info("Step '{}' executed successfully for saga {}",stepName,sagaInstanceId);
                return true;
            }else{
                sagaStepDB.markAsFailed();
                sagaStepDB.setErrorMessage("Step execution returned false");
                sagaStepRepository.save(sagaStepDB);
                log.error("Step '{}' failed for saga {}",stepName,sagaInstanceId);
                return false;
            }
        }catch(JsonProcessingException e){
            log.error("Error processing Saga context for step '{}'",stepName,e);
            updateStepAsFailed(sagaInstanceId,stepName,"Context serialization error: "+e.getMessage());
            throw new SagaException("Failed to process saga context",e);
        } catch (Exception e) {
            log.error("Unexpected error executing step '{}' for saga {}",stepName,sagaInstanceId,e);
            updateStepAsFailed(sagaInstanceId,stepName,"Unexpected error: "+e.getMessage());
            return false;
        }
    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName){
        return false;
    }
    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId){
        return null;
    }
    @Override
    public void compensateSaga(Long sagaInstanceId){

    }

    @Override
    public void failSaga(Long sagaInstanceId){

    }

    @Override
    public void completeSaga(Long sagaInstanceId){

    }
    private SagaContext parseSagaContext(String contextJson){
        try{
            @SuppressWarnings("unchecked")
                    var data=objectMapper.readValue(contextJson,java.util.Map.class);
            return SagaContext.builder().data(data).build();
        }catch(JsonProcessingException e){
            log.error("Error parsing Saga context",e);
            throw new SagaException("Error parsing Saga context",e);
        }
    }
    private void updateStepAsFailed(Long sagaInstanceId,String stepName,String errorMessage){
        try{
            sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId,stepName,StepStatus.RUNNING)
                    .ifPresent(step->{
                        step.markAsFailed();
                        step.setErrorMessage(errorMessage);
                        sagaStepRepository.save(step);
                    });
        }catch(Exception e){
            log.error("Failed to update step '{}' for saga {}",stepName,sagaInstanceId,e);
        }
    }
}
