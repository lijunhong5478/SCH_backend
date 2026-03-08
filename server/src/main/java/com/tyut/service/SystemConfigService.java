package com.tyut.service;

import com.tyut.entity.SystemConfig;
import java.util.List;

public interface SystemConfigService {

    /**
     * 新增系统配置
     * @param systemConfig 系统配置信息
     */
    void addSystemConfig(SystemConfig systemConfig);

    /**
     * 更新系统配置
     * @param systemConfig 系统配置信息
     */
    void updateSystemConfig(SystemConfig systemConfig);

    /**
     * 查询所有系统配置
     * @return 系统配置列表
     */
    List<SystemConfig> getAllSystemConfigs();

    /**
     * 根据配置键获取配置值
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValueByKey(String configKey);
    /**
     * 根据id键删除配置项
     * @param id 配置id
     */
    void deleteSystemConfig(Long id);
}
