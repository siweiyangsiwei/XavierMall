package com.xavier.mall.thirdparty;

import com.aliyun.oss.OSS;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import javax.sound.midi.Soundbank;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
class MallThirdPartyApplicationTests {

    @Resource
    private OSS ossClient;
    @Test
    void contextLoads() throws FileNotFoundException {
        ossClient.putObject("xaviermall","hello.jpg",new FileInputStream("D:\\34.jpg"));
        System.out.println("上传成功");
    }

}
