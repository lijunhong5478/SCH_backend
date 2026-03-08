package com.tyut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tyut.annotation.DataBackUp;
import com.tyut.constant.ModuleConstant;
import com.tyut.context.UserContext;
import com.tyut.entity.SystemConfig;
import com.tyut.exception.BaseException;
import com.tyut.mapper.SystemConfigMapper;
import com.tyut.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    @Resource
    private SystemConfigMapper systemConfigMapper;
    @DataBackUp(module = ModuleConstant.SYSTEM_CONFIG)

    @Override
    public void addSystemConfig(SystemConfig systemConfig) {

        // 检查配置键是否已存在
        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, systemConfig.getConfigKey());
        if (systemConfigMapper.selectCount(queryWrapper) > 0) {
            throw new BaseException("配置键已存在");
        }

        // 设置更新时间
        systemConfig.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        systemConfigMapper.insert(systemConfig);
    }
    @DataBackUp(module = ModuleConstant.SYSTEM_CONFIG)
    @Override
    public void updateSystemConfig(SystemConfig systemConfig) {

        // 检查配置是否存在
        SystemConfig existingConfig = systemConfigMapper.selectById(systemConfig.getId());
        if (existingConfig == null) {
            throw new BaseException("配置不存在");
        }

        // 如果修改了配置键，检查新键是否已存在
        if (!existingConfig.getConfigKey().equals(systemConfig.getConfigKey())) {
            LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemConfig::getConfigKey, systemConfig.getConfigKey());
            if (systemConfigMapper.selectCount(queryWrapper) > 0) {
                throw new BaseException("配置键已存在");
            }
        }

        // 设置更新时间
        systemConfig.setUpdateTime(LocalDateTime.now());

        // 更新数据库
        systemConfigMapper.updateById(systemConfig);
    }

    @Override
    public List<SystemConfig> getAllSystemConfigs() {
        // 所有用户都可以查询系统配置
        return systemConfigMapper.selectList(null);
    }

    @Override
    public String getConfigValueByKey(String configKey) {
        // 所有用户都可以根据键查询配置值
        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(queryWrapper);
        return config != null ? config.getConfigValue() : null;
    }
    @DataBackUp(module = ModuleConstant.SYSTEM_CONFIG)
    @Override
    public void deleteSystemConfig(Long id) {
        systemConfigMapper.deleteById(id);
    }
}
