import com.tyut.Application;
import com.tyut.constant.DoctorTitleConstant;
import com.tyut.dto.AddDoctorDTO;
import com.tyut.entity.DoctorSchedule;
import com.tyut.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest(classes = Application.class)
public class BatchDoctorGeneratorTest {

    @Autowired
    private DoctorService doctorService;

    private final Random random = new Random();

    // 医生姓氏列表
    private static final String[] SURNAMES = {
        "王", "李", "张", "刘", "陈", "杨", "赵", "黄", "周", "吴",
        "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗",
        "梁", "宋", "郑", "谢", "韩", "唐", "冯", "于", "董", "萧"
    };

    // 医生名字列表
    private static final String[] GIVEN_NAMES = {
        "志强", "海燕", "丽华", "建国", "秀英", "永强", "桂芳", "建军", "伟", "娟",
        "涛", "明", "超", "秀兰", "霞", "平", "刚", "桂英", "华", "红",
        "梅", "丽娟", "丹", "萍", "波", "建华", "建军", "建民", "建平", "建英"
    };

    // 各科室专业特长
    private static final String[][] SPECIALTIES = {
        {"心血管内科", "消化内科", "呼吸内科", "内分泌科", "神经内科"},  // 内科
        {"普外科", "骨科", "泌尿外科", "胸外科", "神经外科"},              // 外科
        {"小儿内科", "小儿外科", "儿童保健", "新生儿科"},                  // 儿科
        {"妇科", "产科", "计划生育", "生殖医学"},                          // 妇产科
        {"口腔内科", "口腔外科", "修复科", "正畸科"},                      // 口腔科
        {"中医内科", "针灸科", "推拿科", "中药科"}                         // 中医科
    };

    // 各科室医生介绍模板
    private static final String[][] INTRODUCTIONS = {
        {"从事内科临床工作%d年，擅长%s等疾病的诊治，具有丰富的临床经验"},
        {"外科副主任医师，擅长%s，具有丰富的外科临床经验"},
        {"儿科专家，专门从事儿童疾病诊疗工作%d年，对%s有深入研究"},
        {"妇产科主任医师，从事妇产科工作%d年，擅长%s"},
        {"口腔科专家，精通%s治疗，临床经验丰富"},
        {"中医专家，运用传统中医理论治疗%s，疗效显著"}
    };

    @Test
    public void generateBatchDoctors() {
        System.out.println("开始批量生成60个医生...");

        // 6个科室，每个科室10个医生
        int[] departmentIds = {1, 2, 3, 4, 5, 6};
        int doctorsPerDepartment = 10;

        List<AddDoctorDTO> doctors = new ArrayList<>();

        // 为每个科室生成10个医生
        for (int deptIndex = 0; deptIndex < departmentIds.length; deptIndex++) {
            int departmentId = departmentIds[deptIndex];

            for (int i = 1; i <= doctorsPerDepartment; i++) {
                AddDoctorDTO dto = generateDoctorData(departmentId, deptIndex, i);
                doctors.add(dto);
            }
        }

        // 注册每个医生
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < doctors.size(); i++) {
            try {
                doctorService.registerDoctor(doctors.get(i));
                successCount++;
                System.out.println(String.format("第%d个医生注册成功: %s (科室ID: %d)",
                    i + 1, doctors.get(i).getName(), doctors.get(i).getDepartmentId()));
            } catch (Exception e) {
                failCount++;
                System.err.println(String.format("第%d个医生注册失败: %s, 错误: %s",
                    i + 1, doctors.get(i).getName(), e.getMessage()));
                e.printStackTrace();
            }
        }

        System.out.println("\n=== 批量注册完成 ===");
        System.out.println("成功注册: " + successCount + " 个");
        System.out.println("注册失败: " + failCount + " 个");
        System.out.println("总数量: " + doctors.size() + " 个");
    }

    private AddDoctorDTO generateDoctorData(int departmentId, int deptIndex, int doctorIndex) {
        AddDoctorDTO dto = new AddDoctorDTO();

        // 生成姓名
        String surname = SURNAMES[random.nextInt(SURNAMES.length)];
        String givenName = GIVEN_NAMES[random.nextInt(GIVEN_NAMES.length)];
        String fullName = surname + givenName;
        dto.setName(fullName);

        // 生成用户名 (保证唯一性)
        String username = "doctor" + departmentId + "_" + String.format("%02d", doctorIndex);
        dto.setUsername(username);

        // 生成手机号 (保证唯一性)
        String phone = "139" + String.format("%08d", 30000000 + (departmentId - 1) * 100 + doctorIndex);
        dto.setPhone(phone);

        // 生成身份证号
        String idCard = generateDoctorIdCard(departmentId, doctorIndex);
        dto.setIdCard(idCard);

        // 设置默认密码
        dto.setPassword("123456");

        // 生成年龄 (30-60岁)
        int age = 30 + random.nextInt(31);
        dto.setAge(age);

        // 生成性别 (0女 1男)
        int gender = random.nextInt(2);
        dto.setGender(gender);

        // 设置科室ID
        dto.setDepartmentId(departmentId);

        // 生成职称 (随机分配各级别)
        int[] titles = {DoctorTitleConstant.RESIDENT, DoctorTitleConstant.ATTENDING,
                       DoctorTitleConstant.ASSOCIATE, DoctorTitleConstant.CHIEF};
        int title = titles[random.nextInt(titles.length)];
        dto.setTitle(title);

        // 生成专业特长
        String[] deptSpecialties = SPECIALTIES[deptIndex];
        String specialty = deptSpecialties[random.nextInt(deptSpecialties.length)];
        dto.setSpecialty(specialty);

        // 生成个人介绍
        String[] deptIntros = INTRODUCTIONS[deptIndex];
        String introTemplate = deptIntros[0];
        int experienceYears = 5 + random.nextInt(26); // 5-30年经验
        String introduction = String.format(introTemplate, experienceYears, specialty);
        dto.setIntroduction(introduction);

        // 生成排班信息 (周一到周五，上午下午都有)
        List<DoctorSchedule> schedules = generateDoctorSchedules();
        dto.setDoctorSchedules(schedules);

        // 可选头像URL
        if (random.nextBoolean()) {
            dto.setAvatarUrl("http://localhost:8080/doctor_avatar" + departmentId + "_" + doctorIndex + ".png");
        }

        return dto;
    }

    /**
     * 生成医生身份证号
     */
    private String generateDoctorIdCard(int departmentId, int doctorIndex) {
        // 前6位：地区代码
        String regionCode = "140300";

        // 中间8位：出生日期 (1960-1990年间随机)
        int birthYear = 1960 + random.nextInt(31);
        int birthMonth = 1 + random.nextInt(12);
        int birthDay = 1 + random.nextInt(28);

        String birthDate = String.format("%04d%02d%02d", birthYear, birthMonth, birthDay);

        // 后3位：顺序码 + 校验码
        String sequenceCode = String.format("%02d%01d", departmentId, doctorIndex % 10);
        String checkCode = generateCheckCode(regionCode + birthDate + sequenceCode);

        return regionCode + birthDate + sequenceCode + checkCode;
    }

    /**
     * 生成身份证校验码
     */
    private String generateCheckCode(String idBody) {
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += Character.getNumericValue(idBody.charAt(i)) * weights[i];
        }

        int remainder = sum % 11;
        return String.valueOf(checkCodes[remainder]);
    }

    /**
     * 生成医生排班信息
     */
    private List<DoctorSchedule> generateDoctorSchedules() {
        List<DoctorSchedule> schedules = new ArrayList<>();

        // 周一到周五排班
        for (int weekday = 1; weekday <= 5; weekday++) {
            // 上午排班
            DoctorSchedule amSchedule = new DoctorSchedule();
            amSchedule.setWeekDay(weekday);
            amSchedule.setTimeSlot("AM");
            amSchedule.setMaxNumber(10 + random.nextInt(6)); // 10-15个号
            amSchedule.setCurrentNumber(0);
            amSchedule.setStatus(1); // 正常
            schedules.add(amSchedule);

            // 下午排班
            DoctorSchedule pmSchedule = new DoctorSchedule();
            pmSchedule.setWeekDay(weekday);
            pmSchedule.setTimeSlot("PM");
            pmSchedule.setMaxNumber(8 + random.nextInt(5)); // 8-12个号
            pmSchedule.setCurrentNumber(0);
            pmSchedule.setStatus(1); // 正常
            schedules.add(pmSchedule);
        }

        return schedules;
    }
}
