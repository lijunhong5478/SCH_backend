package com.tyut.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationLogVO {
    private Long id;
    private Long userId;
    private Integer roleType;
    private String methodName;
    private String moduleName;
    private LocalDateTime createTime;
    private String userName;
}
