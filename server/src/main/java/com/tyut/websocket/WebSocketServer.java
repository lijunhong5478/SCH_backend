package com.tyut.websocket;

import com.tyut.context.WebSocketContext;
import com.tyut.service.ConsultationService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    // 存放会话对象
    private static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 设置ApplicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        WebSocketServer.applicationContext = applicationContext;
    }

    /**
     * 获取ConsultationService实例
     */
    private ConsultationService getConsultationService() {
        return applicationContext.getBean(ConsultationService.class);
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + "建立连接");
        sessionMap.put(sid, session);

        // 解析sid并存储用户信息到WebSocket上下文
        parseAndStoreUserInfo(sid);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + "的信息:" + message);
        // 处理消息逻辑
        handleMessage(sid, message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);
        // 清除WebSocket上下文
        WebSocketContext.remove();
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送给指定客户端
     */
    public void sendToClient(String sid, String message) {
        Session session = sessionMap.get(sid);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
                sessionMap.remove(sid); // 移除失效会话
            }
        }
    }

    /**
     * 解析sid并存储用户信息
     */
    private void parseAndStoreUserInfo(String sid) {
        Long userId = getUserIdFromSid(sid);
        Integer userType = getUserTypeFromSid(sid);
        String sessionId = sid;

        if (userId != null && userType != null) {
            WebSocketContext.setUserId(userId);
            WebSocketContext.setUserType(userType);
            WebSocketContext.setSessionId(sessionId);
        }
    }

    /**
     * 从sid中提取用户ID
     */
    private Long getUserIdFromSid(String sid) {
        try {
            if (sid.startsWith("doctor_")) {
                return Long.parseLong(sid.substring(7));
            } else if (sid.startsWith("resident_")) {
                return Long.parseLong(sid.substring(9));
            }
        } catch (NumberFormatException e) {
            System.err.println("解析用户ID失败: " + sid);
        }
        return null;
    }

    /**
     * 从sid中提取用户类型
     */
    private Integer getUserTypeFromSid(String sid) {
        if (sid.startsWith("doctor_")) {
            return 1; // 医生
        } else if (sid.startsWith("resident_")) {
            return 0; // 居民
        }else if (sid.startsWith("admin_")) {
            return 2; // 管理员
        }
        return null;
    }

    /**
     * 处理收到的消息
     */
    private void handleMessage(String sid, String message) {
        // 根据业务需求处理消息
        try {
            System.out.println("处理消息: " + message);
            // 可以调用咨询服务处理消息
            // getConsultationService().processMessage(sid, message);
        } catch (Exception e) {
            System.err.println("处理消息时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取当前在线的医生会话
     */
    public Collection<String> getOnlineDoctors() {
        return sessionMap.keySet().stream()
                .filter(key -> key.startsWith("doctor_"))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取当前在线的居民会话
     */
    public Collection<String> getOnlineResidents() {
        return sessionMap.keySet().stream()
                .filter(key -> key.startsWith("resident_"))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取当前在线的管理员会话
     */
    public Collection<String> getOnlineAdmins() {
        return sessionMap.keySet().stream()
                .filter(key -> key.startsWith("admin_"))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 检查特定用户是否在线
     */
    public boolean isUserOnline(String userTypePrefix, Long userId) {
        String sid = userTypePrefix + "_" + userId;
        return sessionMap.containsKey(sid) && sessionMap.get(sid).isOpen();
    }

    /**
     * 获取所有会话映射（用于管理员推送）
     */
    public ConcurrentHashMap<String, Session> getSessionMap() {
        return sessionMap;
    }
}
