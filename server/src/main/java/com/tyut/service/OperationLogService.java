package com.tyut.service;

import com.tyut.dto.OperationLogQueryDTO;
import com.tyut.result.PageResult;

public interface OperationLogService {
    PageResult list(OperationLogQueryDTO queryDTO);
    void deleteById(Long id);
}
