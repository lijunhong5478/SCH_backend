package com.tyut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountQueryDTO {
    private String username;
    private String phone;
    private Integer status;
    private Integer isDeleted = 0;
    private Integer role;
    private Integer pageNum;
    private Integer pageSize;
}
