package com.tyut.constant;

public enum ModuleConstant {
    // 用户管理模块
    USER_REGISTER("注册"),
    USER_UPDATE_PASSWORD("修改密码"),
    USER_PROFILE_UPDATE("信息修改"),
    USER_STATUS_CHANGE("状态变更"),
    USER_DELETE("删除用户"),
    USER_RESET_PASSWORD("重置密码"),

    // 家庭医生模块
    ONLINE_CONSULTATION("在线接诊"),

    // 预约挂号模块
    APPOINTMENT_BOOK("预约挂号"),
    APPOINTMENT_CANCEL("预约取消"),
    APPOINTMENT_QUERY("预约查询"),
    TRIAGE_DISPATCH("分诊调度"),
    APPOINTMENT_CALL("叫号"),
    APPOINTMENT_START("开始就诊"),
    APPOINTMENT_SKIP("过号操作"),
    APPOINTMENT_FINISH("结束就诊"),

    // 健康档案管理模块
    MEDICAL_HISTORY_INSERT("新增病史"),
    EXAMINATION_REPORT_INSERT("新增检查报告"),
    MEDICAL_VISIT_INSERT("新增就诊记录"),
    MEDICAL_VISIT_UPDATE("就诊记录修改"),
    DIAGNOSIS_RECORD_INSERT("新增诊断记录"),
    DIAGNOSIS_RECORD_UPDATE("更新诊断报告"),
    PHYSICAL_EXAM_INSERT("新增体检记录"),


    // 医疗政策与健康宣教模块
    HEALTH_EDUCATION_PUBLISH("健康宣教发布"),

    // 流行病预警模块
    EPIDEMIC_ALERT_PUBLISH("疫情预警发布"),

    // 系统管理模块
    SYSTEM_CONFIG("系统配置");


    private final String description;

    ModuleConstant(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
