package com.tyut.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalVisit {
    private Long id;
    private Long residentId;
    private Long doctorId;
    private String chiefComplaint;
    private String treatmentAdvice;
    private LocalDateTime createTime;
    private Integer isDeleted;
    private Long appointId;
}
