package com.shardedwalletsystem.demo.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.calcite.model.JsonType;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="saga_instance")
public class SagaInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private SagaStatus status;

    @Column(name = "context", columnDefinition = "json")
    private String context;

    @Column(name = "current_step", nullable = false)
    private String currentStep;
}
