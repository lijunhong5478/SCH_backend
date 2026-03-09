package com.tyut.controller.common;

import com.tyut.result.Result;
import com.tyut.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/common/file")
@Api(tags="通用-文件上传接口")
@Slf4j
public class FileController {
    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("上传文件")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);
        try {
            String originalFilename=file.getOriginalFilename();
            String extension=originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName= UUID.randomUUID().toString()+extension;
            String filePath=aliOssUtil.upload(file.getBytes(),objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}",e);
            return Result.error("文件上传失败！");
        }
    }

    // 放在 FileController 类里
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 8000;
    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024; // 5MB

    @GetMapping("/proxy-image")
    @ApiOperation("代理获取远程图片")
    public ResponseEntity<byte[]> proxyImage(@RequestParam("url") String imageUrl) {
        log.info("代理图片请求: {}", imageUrl);

        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return badRequest("url不能为空");
            }

            URL url = new URL(imageUrl.trim());
            String protocol = url.getProtocol();
            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                return badRequest("仅支持http/https协议");
            }

            String host = url.getHost();
            if (host == null || host.isBlank()) {
                return badRequest("非法host");
            }

            // SSRF 防护：禁止内网/回环地址
            if (isPrivateOrLoopbackHost(host)) {
                return badRequest("禁止访问内网地址");
            }

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setInstanceFollowRedirects(false); // 避免重定向绕过校验
            conn.setRequestProperty("User-Agent", "SCH-Backend-ImageProxy/1.0");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(("远程图片请求失败，状态码: " + status).getBytes(StandardCharsets.UTF_8));
            }

            String contentType = conn.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                return badRequest("目标资源不是图片");
            }

            byte[] body;
            try (InputStream in = conn.getInputStream()) {
                body = readWithLimit(in, MAX_IMAGE_BYTES);
            }

            MediaType mediaType = MediaType.parseMediaType(contentType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(body.length)
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(body);

        } catch (IOException e) {
            log.error("代理图片失败", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("代理图片失败".getBytes(StandardCharsets.UTF_8));
        }
    }

    private ResponseEntity<byte[]> badRequest(String message) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.TEXT_PLAIN)
                .body(message.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] readWithLimit(InputStream in, int maxBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int len;
        while ((len = in.read(buffer)) != -1) {
            total += len;
            if (total > maxBytes) {
                throw new IOException("图片大小超限");
            }
            out.write(buffer, 0, len);
        }
        return out.toByteArray();
    }

    private boolean isPrivateOrLoopbackHost(String host) throws IOException {
        if ("localhost".equalsIgnoreCase(host)) {
            return true;
        }
        InetAddress[] addresses = InetAddress.getAllByName(host);
        for (InetAddress addr : addresses) {
            if (addr.isAnyLocalAddress()
                    || addr.isLoopbackAddress()
                    || addr.isLinkLocalAddress()
                    || addr.isSiteLocalAddress()
                    || isUniqueLocalAddress(addr.getHostAddress())) {
                return true;
            }
        }
        return false;
    }

    // IPv6 ULA: fc00::/7
    private boolean isUniqueLocalAddress(String ip) {
        if (ip == null) {
            return false;
        }
        String lower = ip.toLowerCase();
        return lower.startsWith("fc") || lower.startsWith("fd");
    }
}
