import com.tyut.Application;
import com.tyut.dto.ResidentRegisterDTO;
import com.tyut.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest(classes = Application.class)
public class BatchResidentGeneratorTest {

    @Autowired
    private UserService userService;

    private final Random random = new Random();

    // 姓氏列表
    private static final String[] SURNAMES = {
        "王", "李", "张", "刘", "陈", "杨", "赵", "黄", "周", "吴",
        "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗",
        "梁", "宋", "郑", "谢", "韩", "唐", "冯", "于", "董", "萧"
    };

    // 名字列表
    private static final String[] GIVEN_NAMES = {
        "伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
        "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞",
        "平", "刚", "桂英", "华", "红", "梅", "丽娟", "丹", "萍", "波",
        "建华", "建军", "建国", "建军", "建民", "建平", "建英", "建红", "建霞", "建兰"
    };

    // 地址前缀
    private static final String[] ADDRESS_PREFIXES = {
        "阳泉市南山街道办事处", "阳泉市四矿街道", "阳泉市义井街道", "阳泉市郊区",
        "阳泉市矿区", "阳泉市城区", "阳泉市开发区", "阳泉市盂县", "阳泉市平定县"
    };

    // 小区名称
    private static final String[] COMMUNITIES = {
        "康居苑", "幸福家园", "和谐小区", "安居小区", "阳光花园", "温馨家园",
        "绿景花园", "世纪花园", "锦绣花园", "金辉小区", "如意小区", "平安小区"
    };

    @Test
    public void generateBatchResidents() {
        System.out.println("开始批量生成100个社区居民...");

        List<ResidentRegisterDTO> residents = new ArrayList<>();

        // 生成100个居民数据
        for (int i = 1; i <= 100; i++) {
            ResidentRegisterDTO dto = generateResidentData(i);
            residents.add(dto);
        }

        // 注册每个居民
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < residents.size(); i++) {
            try {
                userService.registerResident(residents.get(i));
                successCount++;
                System.out.println(String.format("第%d个居民注册成功: %s (%d岁)",
                    i + 1, residents.get(i).getName(), residents.get(i).getAge()));
            } catch (Exception e) {
                failCount++;
                System.err.println(String.format("第%d个居民注册失败: %s, 错误: %s",
                    i + 1, residents.get(i).getName(), e.getMessage()));
            }
        }

        System.out.println("\n=== 批量注册完成 ===");
        System.out.println("成功注册: " + successCount + " 个");
        System.out.println("注册失败: " + failCount + " 个");
        System.out.println("总数量: " + residents.size() + " 个");
    }

    private ResidentRegisterDTO generateResidentData(int index) {
        ResidentRegisterDTO dto = new ResidentRegisterDTO();

        // 生成姓名
        String surname = SURNAMES[random.nextInt(SURNAMES.length)];
        String givenName = GIVEN_NAMES[random.nextInt(GIVEN_NAMES.length)];
        String fullName = surname + givenName;
        dto.setName(fullName);

        // 生成用户名 (保证唯一性)
        String username = "resident" + String.format("%03d", index);
        dto.setUsername(username);

        // 生成手机号 (保证唯一性)
        String phone = "138" + String.format("%08d", 10000000 + index);
        dto.setPhone(phone);

        // 生成身份证号 (18位，保证唯一性)
        String idCard = generateUniqueIdCard(index);
        dto.setIdCard(idCard);

        // 设置默认密码
        dto.setPassword("123456");

        // 生成年龄 (重点：40-70岁占比最多)
        int age = generateAgeWithDistribution();
        dto.setAge(age);

        // 生成性别 (0女 1男)
        int gender = random.nextInt(2);
        dto.setGender(gender);

        // 生成紧急联系方式
        String contact = "139" + String.format("%08d", 20000000 + index);
        dto.setContact(contact);

        // 生成地址
        String address = generateAddress();
        dto.setAddress(address);

        // 可选头像URL
        if (random.nextBoolean()) {
            dto.setAvatarUrl("http://localhost:8080/avatar" + index + ".png");
        }

        return dto;
    }

    /**
     * 生成符合年龄分布要求的数据
     * 20-39岁: 20%
     * 40-70岁: 60%
     * 71-80岁: 20%
     */
    private int generateAgeWithDistribution() {
        int rand = random.nextInt(100);

        if (rand < 20) {
            // 20-39岁 (20%)
            return 20 + random.nextInt(20);
        } else if (rand < 80) {
            // 40-70岁 (60%)
            return 40 + random.nextInt(31);
        } else {
            // 71-80岁 (20%)
            return 71 + random.nextInt(10);
        }
    }

    /**
     * 生成唯一的18位身份证号码
     */
    private String generateUniqueIdCard(int index) {
        // 前6位：地区代码 (示例：山西省阳泉市)
        String regionCode = "140300";

        // 中间8位：出生日期 (1940-2005年间随机)
        int birthYear = 1940 + random.nextInt(66);
        int birthMonth = 1 + random.nextInt(12);
        int birthDay = 1 + random.nextInt(28); // 简化处理，都按28天计算

        String birthDate = String.format("%04d%02d%02d", birthYear, birthMonth, birthDay);

        // 后3位：顺序码 + 校验码
        String sequenceCode = String.format("%03d", index % 1000);
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
     * 生成地址信息
     */
    private String generateAddress() {
        String prefix = ADDRESS_PREFIXES[random.nextInt(ADDRESS_PREFIXES.length)];
        String community = COMMUNITIES[random.nextInt(COMMUNITIES.length)];
        int building = 1 + random.nextInt(20);
        int unit = 1 + random.nextInt(6);
        int floor = 1 + random.nextInt(12);
        int room = 1 + random.nextInt(4);

        return String.format("%s%s%d区%d号楼%d单元%d层%d室",
            prefix, community, random.nextInt(5) + 1, building, unit, floor, room);
    }
}
