package com.tyut.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 诊断结果报告
 * 每次诊断必生成
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisReport {
    private Long id;
    private Long visitId;
    private String diagnosisResult;
    private String diagnosisDetail;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer isDeleted;
    private Long healthRecordId;
}
