package com.tyut.utils;

import com.tyut.constant.DiseaseLevelConstant;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 智能分诊工具类
 * 结合AI模型+关键词匹配，确保分诊准确性
 * 
 * @author Lingma
 */
@Slf4j
@Component
public class TriageUtil {

    @Autowired
    private OllamaChatModel chatModel;

    /**
     * 根据症状描述确定分诊等级（优化版）
     * 结合AI模型+关键词匹配，确保分级准确
     *
     * @param symptom 症状描述
     * @return 分诊等级 (1-4)
     */
    public Integer determineTriageLevel(String symptom) {
        if (symptom == null || symptom.trim().isEmpty()) {
            return DiseaseLevelConstant.LEVEL_4_MINOR;
        }

        // 先通过关键词快速匹配（优先级最高）
        int keywordLevel = getLevelByKeyword(symptom);
        
        // 如果关键词匹配到的是1级或2级，直接返回（确保危急症状不被误判）
        if (keywordLevel == DiseaseLevelConstant.LEVEL_1_CRITICAL || 
            keywordLevel == DiseaseLevelConstant.LEVEL_2_SEVERE) {
            log.info("关键词匹配分级：{} -> {}", symptom, keywordLevel);
            return keywordLevel;
        }

        try {
            // 进一步优化的提示词：更严格的规则和更多示例
            String prompt = "你是一位经验丰富的急诊科分诊专家。请严格按照以下标准对患者症状进行分级。\n\n" +
                    "【分级标准详解】\n" +
                    "1级（危急重症，需立即抢救）：\n" +
                    "- 生命体征不稳定：心脏骤停、呼吸停止、大出血不止\n" +
                    "- 意识障碍：昏迷、意识丧失、严重精神异常\n" +
                    "- 循环衰竭：休克、血压极低、脉搏微弱\n" +
                    "- 严重胸痛：疑似心肌梗死、主动脉夹层\n" +
                    "- 严重呼吸困难：窒息、哮喘持续状态\n" +
                    "- 严重神经系统症状：癫痫持续状态、脑卒中\n\n" +
                    
                    "2级（急重症，需尽快处理）：\n" +
                    "- 骨折：任何部位的骨折、脱臼\n" +
                    "- 高热：体温≥39℃或伴有惊厥\n" +
                    "- 剧烈疼痛：疼痛评分≥8分，难以忍受\n" +
                    "- 严重外伤：深度切割伤、穿透伤、大面积软组织损伤\n" +
                    "- 急性过敏：过敏性休克、喉头水肿\n" +
                    "- 烧伤烫伤：面积≥10%或深度烧伤\n" +
                    "- 急性中毒：药物过量、化学物质中毒\n\n" +
                    
                    "3级（普通急诊，可等候）：\n" +
                    "- 轻中度腹痛：可忍受的腹痛、胃肠不适\n" +
                    "- 轻微发热：体温37.5-38.9℃\n" +
                    "- 上呼吸道感染：咳嗽、咽喉痛、鼻塞\n" +
                    "- 轻微外伤：表浅擦伤、小面积挫伤\n" +
                    "- 轻度扭伤：关节轻微肿胀、活动受限\n" +
                    "- 轻微头痛：紧张性头痛、可缓解的头痛\n" +
                    "- 轻度腹泻：每日3-5次，无脱水征象\n\n" +
                    
                    "4 级（非紧急，门诊处理）：\n" +
                    "- 普通感冒：流鼻涕、打喷嚏、轻微咳嗽\n" +
                    "- 轻微擦伤：表皮破损、无出血\n" +
                    "- 慢性疾病：高血压、糖尿病常规复查\n" +
                    "- 健康咨询：体检、疫苗接种\n" +
                    "- 轻微皮肤问题：皮疹、蚊虫叮咬\n" +
                    "- 轻微不适：疲劳、失眠、一般性不适\n" +
                    "- 无法识别的症状描述、无意义词汇、乱码、非医学相关输入\n\n" +
                    
                    "【判断原则】\n" +
                    "1. 严格按标准执行，宁可低估不可高估\n" +
                    "2. 只输出数字 1、2、3 或 4，绝不添加其他内容\n" +
                    "3. 多症状并存时，按最严重级别判定\n" +
                    "4. 儿童症状适当从严把握\n" +
                    "5. 骨折无论部位均属 2 级\n" +
                    "6. 遇到无法识别的词汇、无意义输入（如 XXXX、呜哩哇啦等）直接判为 4 级\n\n" +
                    
                    "【关键区分点】\n" +
                    "- 腹痛：轻微可忍受→3级；剧烈难忍→2级\n" +
                    "- 头痛：轻微能忍受→3级；剧烈无法忍受→2级\n" +
                    "- 发热：≥39℃→2级；<39℃→3级\n" +
                    "- 外伤：有骨折→2级；仅擦伤→4级；较深伤口→3级\n" +
                    "- 感冒症状：单纯流鼻涕→4级；伴高热→2级\n\n" +
                    
                    "【标准示例】\n" +
                    "患者：胸痛剧烈伴大汗\n" +
                    "分级：1\n" +
                    "患者：右臂骨折畸形\n" +
                    "分级：2\n" +
                    "患者：发热39.5℃伴寒战\n" +
                    "分级：2\n" +
                    "患者：腹痛可忍受，轻度腹泻\n" +
                    "分级：3\n" +
                    "患者：轻微头痛，休息后缓解\n" +
                    "分级：3\n" +
                    "患者：流清鼻涕，轻微咳嗽\n" +
                    "分级：4\n" +
                    "患者：手指轻微擦伤\n" +
                    "分级：4\n" +
                    "患者：定期复查高血压\n" +
                    "分级：4\n" +
                    "患者：XXXX\n" +
                    "分级：4\n" +
                    "患者：呜哩哇啦\n" +
                    "分级：4\n" +
                    "患者：asdfgh\n" +
                    "分级：4\n" +
                    "患者：啊啊啊啊\n" +
                    "分级：4\n\n" +
                    
                    "【当前患者】\n" +
                    "症状：" + symptom + "\n" +
                    "请严格按照上述标准判断，只输出数字：";

            String response = chatModel.generate(prompt).trim();
            log.info("AI模型原始响应：{}", response);

            // 更严格的数字提取和验证
            String cleanResponse = response.replaceAll("[^1-4\\n\\r\\s]", "").trim();
            log.debug("清理后响应：{}", cleanResponse);
            
            // 提取第一行中的数字
            String[] lines = cleanResponse.split("[\\n\\r]+");
            for (String line : lines) {
                String digit = line.replaceAll("[^1-4]", "");
                if (!digit.isEmpty()) {
                    int aiLevel = Character.getNumericValue(digit.charAt(0));
                    log.info("提取到AI分级：{}", aiLevel);
                    
                    // AI分级结果校验
                    if (aiLevel == DiseaseLevelConstant.LEVEL_1_CRITICAL) {
                        // 检查是否是骨折被误判为1级
                        if (containsAny(symptom, "骨折", "骨裂") && 
                            !containsAny(symptom, "昏迷", "呼吸停止", "大出血", "休克")) {
                            log.info("AI分级修正：骨折不应为1级，修正为2级");
                            return DiseaseLevelConstant.LEVEL_2_SEVERE;
                        }
                        return aiLevel;
                    }
                    
                    // 如果AI分级明显不合理，使用关键词分级
                    if (isAiResultUnreasonable(symptom, aiLevel)) {
                        log.warn("AI分级{}对症状'{}'不合理，使用关键词分级{}", aiLevel, symptom, keywordLevel);
                        return keywordLevel;
                    }
                    
                    // 如果AI分级低于关键词匹配，取更高级别
                    if (aiLevel > keywordLevel) {
                        log.info("AI分级({})低于关键词分级({})，采用更高级别", aiLevel, keywordLevel);
                        return keywordLevel;
                    }
                    
                    return aiLevel;
                }
            }
            
            return keywordLevel;
        } catch (Exception e) {
            log.error("AI分级失败，使用关键词分级", e);
            return keywordLevel;
        }
    }

    /**
     * 关键词匹配备用方案（优化版）
     * 逻辑：从致命性症状向轻微症状逐级过滤
     */
    private int getLevelByKeyword(String symptom) {
        if (symptom == null) return DiseaseLevelConstant.LEVEL_4_MINOR;
        String s = symptom.toLowerCase();

        // --- 1级：危急 (立即抢救) ---
        if (containsAny(s, "呼吸停止", "心跳停止", "大出血", "昏迷", "意识丧失", 
                        "休克", "窒息", "心脏骤停", "严重抽搐", "濒死")) {
            return DiseaseLevelConstant.LEVEL_1_CRITICAL;
        }

        // --- 2级：紧急 (需尽快处理) ---
        // 特别注意：骨折是明确的2级
        if (containsAny(s, "骨折", "骨裂", "脱臼", "高热", "39度", "40度", 
                        "剧痛", "严重外伤", "刀伤", "枪伤", "烧伤", "烫伤",
                        "呼吸困难", "过敏", "吐血", "便血")) {
            return DiseaseLevelConstant.LEVEL_2_SEVERE;
        }

        // --- 3级：普通急诊 ---
        if (containsAny(s, "腹痛", "呕吐", "腹泻", "拉肚子", "头晕", "头痛",
                        "低烧", "38度", "咳嗽", "扭伤", "割伤", "伤口",
                        "尿频", "尿急", "皮疹", "瘙痒")) {
            return DiseaseLevelConstant.LEVEL_3_MODERATE;
        }

        // --- 4级：非紧急 ---
        if (containsAny(s, "感冒", "流鼻涕", "打喷嚏", "擦伤", "复诊",
                        "咨询", "体检", "配药", "轻微", "慢性")) {
            return DiseaseLevelConstant.LEVEL_4_MINOR;
        }

        return DiseaseLevelConstant.LEVEL_4_MINOR;
    }

    /**
     * 判断AI结果是否合理
     * @param symptom 症状描述
     * @param aiLevel AI判断的等级
     * @return 是否不合理
     */
    private boolean isAiResultUnreasonable(String symptom, int aiLevel) {
        String s = symptom.toLowerCase();
        
        // 如果AI给出2级，但症状明显轻微，则不合理
        if (aiLevel == DiseaseLevelConstant.LEVEL_2_SEVERE) {
            // 明显应该是3级或4级的症状却被判为2级
            if (containsAny(s, "流鼻涕", "擦伤", "轻微头痛", "轻微感冒", "咳嗽", "打喷嚏")) {
                return true;
            }
            // 腹痛但描述为"轻微"、"有点"等
            if (s.contains("腹痛") && containsAny(s, "轻微", "有点", "不太严重")) {
                return true;
            }
        }
        
        // 如果AI给出1级，但没有危急症状
        if (aiLevel == DiseaseLevelConstant.LEVEL_1_CRITICAL) {
            if (!containsAny(s, "昏迷", "呼吸停止", "大出血", "休克", "心脏骤停", "严重胸痛")) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 辅助方法：判断字符串是否包含数组中的任意关键词
     */
    private boolean containsAny(String target, String... keywords) {
        if (target == null || keywords == null) return false;
        String lowerTarget = target.toLowerCase();
        for (String key : keywords) {
            if (key != null && lowerTarget.contains(key.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取分诊等级描述
     */
    public String getLevelDescription(Integer level) {
        switch (level) {
            case 1:
                return "一级 - 危急重症（需立即抢救）";
            case 2:
                return "二级 - 急重症（需尽快处理）";
            case 3:
                return "三级 - 普通急诊（可等候）";
            case 4:
                return "四级 - 非紧急（可门诊或自愈）";
            default:
                return "未知等级";
        }
    }
}