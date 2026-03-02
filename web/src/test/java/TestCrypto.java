import com.tyut.Application;
import com.tyut.entity.SysUser;
import com.tyut.mapper.UserMapper;
import com.tyut.utils.CryptoUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = Application.class)
public class TestCrypto {
    @Autowired
    private CryptoUtil cryptoUtil;
    @Autowired
    private UserMapper userMapper;
    @Test
    public void test() {
        String originalText = "123456";
        //ed55f05984b0298da080fcefd3e3bd3c
        String encryptedText = cryptoUtil.encodePassword(originalText);
        System.out.println("加密后的密码：" + encryptedText);
        System.out.println("解密后的密码：" + cryptoUtil.matches(originalText, encryptedText));
    }
    @Test
    public void getIdCard(){
        List<SysUser> sysUsers = userMapper.selectList(null);
        for(SysUser user:sysUsers){
            System.out.println(user.getUsername()+" idCard: "+cryptoUtil.decodeIdCard(user.getIdCard()));
        }

    }
}
