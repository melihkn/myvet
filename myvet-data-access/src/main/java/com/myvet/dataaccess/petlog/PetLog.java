package com.myvet.dataaccess.petlog;

import com.myvet.dataaccess.enums.Enums.LogType;
import com.myvet.dataaccess.pet.Pet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "pet_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", nullable = false, length = 20)
    private LogType logType;

    @Column(name = "log_value", nullable = false, length = 200)
    private String logValue;

    @Convert(converter = JsonbConverter.class)
    @Column(name = "log_metric", columnDefinition = "jsonb")
    private Map<String, Object> logMetric;

    @Column(name = "log_created", nullable = false)
    @Builder.Default
    private LocalDateTime logCreated = LocalDateTime.now();

    @Column(name = "log_start")
    private LocalDateTime logStart;

    @Column(name = "log_end")
    private LocalDateTime logEnd;

    @Column(name = "log_scheduled")
    private LocalDateTime logScheduled;
}
