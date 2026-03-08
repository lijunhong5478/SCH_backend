package com.tyut;

import com.tyut.properties.AliOssProperties;
import com.tyut.utils.AliOssUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class BatchImageUploadTest {

    @Autowired
    private AliOssProperties aliOssProperties;

    // 要上传的图片目录路径
    private static final String IMAGE_SOURCE_DIR = "D:\\工作\\毕业设计\\图片素材";

    // 支持的图片格式
    private static final String[] SUPPORTED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

    @Test
    public void batchUploadImagesToOSS() {
        System.out.println("=== 开始批量上传图片到阿里云OSS ===");
        System.out.println("源目录: " + IMAGE_SOURCE_DIR);

        // 检查源目录是否存在
        File sourceDir = new File(IMAGE_SOURCE_DIR);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.err.println("❌ 源目录不存在或不是目录: " + IMAGE_SOURCE_DIR);
            return;
        }

        // 获取所有图片文件
        List<File> imageFiles = getImageFiles(sourceDir);
        if (imageFiles.isEmpty()) {
            System.out.println("⚠️  目录中没有找到支持的图片文件");
            return;
        }

        System.out.println("发现 " + imageFiles.size() + " 个图片文件");

        // 初始化OSS工具类
        AliOssUtil ossUtil = new AliOssUtil(
            aliOssProperties.getEndpoint(),
            aliOssProperties.getAccessKeyId(),
            aliOssProperties.getAccessKeySecret(),
            aliOssProperties.getBucketName()
        );

        // 执行批量上传
        int successCount = 0;
        int failCount = 0;
        List<String> uploadedUrls = new ArrayList<>();

        for (int i = 0; i < imageFiles.size(); i++) {
            File imageFile = imageFiles.get(i);
            try {
                // 读取文件内容
                byte[] fileBytes = Files.readAllBytes(imageFile.toPath());

                // 生成对象名称（使用原文件名）
                String fileName = imageFile.getName();
                String objectName = "images/" + fileName; // 添加images前缀分类存储

                // 上传文件
                String fileUrl = ossUtil.upload(fileBytes, objectName);

                successCount++;
                uploadedUrls.add(fileUrl);
                System.out.println(String.format("✅ [%d/%d] 上传成功: %s -> %s",
                    i + 1, imageFiles.size(), fileName, fileUrl));

            } catch (IOException e) {
                failCount++;
                System.err.println(String.format("❌ [%d/%d] 读取文件失败: %s, 错误: %s",
                    i + 1, imageFiles.size(), imageFile.getName(), e.getMessage()));
            } catch (Exception e) {
                failCount++;
                System.err.println(String.format("❌ [%d/%d] 上传失败: %s, 错误: %s",
                    i + 1, imageFiles.size(), imageFile.getName(), e.getMessage()));
            }
        }

        // 输出统计结果
        System.out.println("\n=== 上传完成统计 ===");
        System.out.println("总文件数: " + imageFiles.size());
        System.out.println("成功上传: " + successCount + " 个");
        System.out.println("上传失败: " + failCount + " 个");
        System.out.println("成功率: " + String.format("%.2f%%",
            (double) successCount / imageFiles.size() * 100));

        // 输出所有上传成功的URL
        if (!uploadedUrls.isEmpty()) {
            System.out.println("\n=== 上传成功的文件URL ===");
            for (int i = 0; i < uploadedUrls.size(); i++) {
                System.out.println(String.format("[%d] %s", i + 1, uploadedUrls.get(i)));
            }
        }

        System.out.println("=== 批量上传任务完成 ===");
    }

    /**
     * 递归获取目录下所有支持的图片文件
     */
    private List<File> getImageFiles(File directory) {
        List<File> imageFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归处理子目录
                    imageFiles.addAll(getImageFiles(file));
                } else if (isSupportedImageFile(file)) {
                    imageFiles.add(file);
                }
            }
        }

        return imageFiles;
    }

    /**
     * 判断是否为支持的图片文件
     */
    private boolean isSupportedImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        for (String extension : SUPPORTED_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 测试单个文件上传（可选的辅助测试方法）
     */
    @Test
    public void testSingleImageUpload() {
        System.out.println("=== 单文件上传测试 ===");

        // 测试文件路径（需要根据实际情况修改）
        String testFilePath = "D:\\工作\\毕业设计\\图片素材\\test.jpg";
        File testFile = new File(testFilePath);

        if (!testFile.exists()) {
            System.out.println("测试文件不存在: " + testFilePath);
            return;
        }

        try {
            AliOssUtil ossUtil = new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName()
            );

            byte[] fileBytes = Files.readAllBytes(testFile.toPath());
            String objectName = "test/" + testFile.getName();
            String fileUrl = ossUtil.upload(fileBytes, objectName);

            System.out.println("✅ 单文件上传成功: " + fileUrl);

        } catch (Exception e) {
            System.err.println("❌ 单文件上传失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
