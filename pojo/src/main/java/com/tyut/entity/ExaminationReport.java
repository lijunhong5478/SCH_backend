package com.tyut.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 检测报告
 * 每次诊断不一定会生成
 * 因为不是所有症状都需要预先检测
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExaminationReport {
    private Long id;
    private Integer reportType;
    private String reportContent;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private Long recordId;
}
