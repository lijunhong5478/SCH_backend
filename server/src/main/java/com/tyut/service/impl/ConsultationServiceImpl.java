package com.tyut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tyut.constant.ConsultationConstant;
import com.tyut.context.BaseContext;
import com.tyut.dto.ConsultationQueryDTO;
import com.tyut.dto.SendImageMessageDTO;
import com.tyut.dto.SendMessageDTO;
import com.tyut.entity.ConsultationMessage;
import com.tyut.entity.ConsultationSession;
import com.tyut.entity.DoctorProfile;
import com.tyut.entity.ResidentProfile;
import com.tyut.exception.BaseException;
import com.tyut.mapper.ConsultationMessageMapper;
import com.tyut.mapper.ConsultationSessionMapper;
import com.tyut.mapper.DoctorProfileMapper;
import com.tyut.mapper.ResidentMapper;
import com.tyut.service.ConsultationService;
import com.tyut.vo.ConsultationMessageVO;
import com.tyut.vo.ConsultationSessionVO;
import com.tyut.vo.ConsultationSyncVO;
import com.tyut.vo.ConsultationUnreadVO;
import com.tyut.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultationServiceImpl implements ConsultationService {

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    @Autowired
    private ConsultationMessageMapper consultationMessageMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private ResidentMapper residentMapper;

    @Autowired
    private DoctorProfileMapper doctorProfileMapper;

    @Override
    @Transactional
    public Long createOrGetActiveSession(Long doctorId, Long residentId) {
        if (doctorId == null || residentId == null) {
            throw new BaseException("医生ID和居民ID不能为空");
        }

        LambdaQueryWrapper<ConsultationSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationSession::getDoctorId, doctorId)
                .eq(ConsultationSession::getResidentId, residentId)
                .eq(ConsultationSession::getStatus, ConsultationConstant.PROCEEDING)
                .orderByDesc(ConsultationSession::getCreateTime)
                .last("limit 1");

        ConsultationSession activeSession = consultationSessionMapper.selectOne(queryWrapper);
        if (activeSession != null) {
            return activeSession.getId();
        }

        ConsultationSession newSession = ConsultationSession.builder()
                .doctorId(doctorId)
                .residentId(residentId)
                .status(ConsultationConstant.PROCEEDING)
                .createTime(LocalDateTime.now())
                .build();

        consultationSessionMapper.insert(newSession);
        return newSession.getId();
    }

    @Override
    @Transactional
    public void sendMessage(SendMessageDTO dto) {
        if (dto == null || dto.getSessionId() == null || !StringUtils.hasText(dto.getContent())) {
            throw new BaseException("文本消息参数不完整");
        }

        Long currentUserId = getCurrentUserId();
        Integer senderType = getCurrentSenderType();
        validateSessionParticipant(dto.getSessionId(), currentUserId, senderType);

        ConsultationSession session = consultationSessionMapper.selectById(dto.getSessionId());
        if (session == null || !ConsultationConstant.PROCEEDING.equals(session.getStatus())) {
            throw new BaseException("会话不存在或已结束");
        }

        ConsultationMessage message = ConsultationMessage.builder()
                .sessionId(dto.getSessionId())
                .senderType(senderType)
                .senderId(currentUserId)
                .messageType(ConsultationConstant.TEXT_MSG)
                .content(dto.getContent().trim())
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();

        consultationMessageMapper.insert(message);
        sendWebSocketMessage(dto.getSessionId(), message);
    }

    @Override
    @Transactional
    public void handleImageMessage(SendImageMessageDTO dto) {
        if (dto == null || dto.getSessionId() == null || !StringUtils.hasText(dto.getImageUrl())) {
            throw new BaseException("图片消息参数不完整");
        }

        String imageUrl = dto.getImageUrl().trim();
        if (!(imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            throw new BaseException("图片URL格式非法");
        }

        Long currentUserId = getCurrentUserId();
        Integer senderType = getCurrentSenderType();
        validateSessionParticipant(dto.getSessionId(), currentUserId, senderType);

        ConsultationSession session = consultationSessionMapper.selectById(dto.getSessionId());
        if (session == null || !ConsultationConstant.PROCEEDING.equals(session.getStatus())) {
            throw new BaseException("会话不存在或已结束");
        }

        ConsultationMessage message = ConsultationMessage.builder()
                .sessionId(dto.getSessionId())
                .senderType(senderType)
                .senderId(currentUserId)
                .messageType(ConsultationConstant.IMAGE_MSG)
                .content(imageUrl)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();

        consultationMessageMapper.insert(message);
        sendWebSocketMessage(dto.getSessionId(), message);
    }

    @Override
    public void validateSessionParticipant(Long sessionId, Long userId, Integer userType) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BaseException("会话不存在");
        }

        boolean isResident = ConsultationConstant.RESIDENT_SENDER.equals(userType)
                && userId.equals(session.getResidentId());
        boolean isDoctor = ConsultationConstant.DOCTOR_SENDER.equals(userType)
                && userId.equals(session.getDoctorId());

        if (!isResident && !isDoctor) {
            throw new BaseException("无权访问该会话");
        }
    }

    @Override
    public List<ConsultationSessionVO> listSessions(ConsultationQueryDTO queryDTO) {
        Long currentUserId = getCurrentUserId();
        Integer senderType = getCurrentSenderType();

        LambdaQueryWrapper<ConsultationSession> queryWrapper = new LambdaQueryWrapper<>();
        if (ConsultationConstant.RESIDENT_SENDER.equals(senderType)) {
            queryWrapper.eq(ConsultationSession::getResidentId, currentUserId);
        } else {
            queryWrapper.eq(ConsultationSession::getDoctorId, currentUserId);
        }

        if (queryDTO != null && queryDTO.getStatus() != null) {
            queryWrapper.eq(ConsultationSession::getStatus, queryDTO.getStatus());
        }

        queryWrapper.orderByDesc(ConsultationSession::getCreateTime);

        List<ConsultationSession> sessions = consultationSessionMapper.selectList(queryWrapper);
        return sessions.stream().map(this::convertToSessionVO).collect(Collectors.toList());
    }

    @Override
    public List<ConsultationMessageVO> listMessagesAfter(Long sessionId, Long lastMessageId, Integer limit) {
        Long currentUserId = getCurrentUserId();
        Integer senderType = getCurrentSenderType();
        validateSessionParticipant(sessionId, currentUserId, senderType);

        int fetchSize = (limit == null || limit <= 0) ? 50 : Math.min(limit, 200);

        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                .gt(lastMessageId != null && lastMessageId > 0, ConsultationMessage::getId, lastMessageId)
                .orderByAsc(ConsultationMessage::getCreateTime)
                .orderByAsc(ConsultationMessage::getId)
                .last("limit " + fetchSize);

        List<ConsultationMessage> messages = consultationMessageMapper.selectList(queryWrapper);
        return messages.stream().map(this::convertToMessageVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markSessionReadUpTo(Long sessionId, Long receiverId, Integer receiverType, Long lastReadMessageId) {
        if (sessionId == null || receiverId == null || receiverType == null || lastReadMessageId == null) {
            throw new BaseException("已读参数不完整");
        }

        validateCurrentUser(receiverId, receiverType);
        validateSessionParticipant(sessionId, receiverId, receiverType);

        LambdaUpdateWrapper<ConsultationMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                .ne(ConsultationMessage::getSenderType, receiverType)
                .le(ConsultationMessage::getId, lastReadMessageId)
                .eq(ConsultationMessage::getIsRead, 0)
                .set(ConsultationMessage::getIsRead, 1);

        consultationMessageMapper.update(null, updateWrapper);

        String readAck = "{"
                + "\"type\":\"read_ack\"," 
                + "\"sessionId\":" + sessionId + ","
                + "\"lastReadMessageId\":" + lastReadMessageId + ","
                + "\"receiverId\":" + receiverId
                + "}";
        pushToPeer(sessionId, receiverType, readAck);
    }

    @Override
    public List<ConsultationUnreadVO> getUnreadCountBySession(Long userId, Integer userType) {
        validateCurrentUser(userId, userType);

        LambdaQueryWrapper<ConsultationSession> sessionWrapper = new LambdaQueryWrapper<>();
        if (ConsultationConstant.RESIDENT_SENDER.equals(userType)) {
            sessionWrapper.eq(ConsultationSession::getResidentId, userId);
        } else {
            sessionWrapper.eq(ConsultationSession::getDoctorId, userId);
        }

        List<ConsultationSession> sessions = consultationSessionMapper.selectList(sessionWrapper);
        List<ConsultationUnreadVO> unreadVOList = new ArrayList<>();

        for (ConsultationSession session : sessions) {
            LambdaQueryWrapper<ConsultationMessage> unreadWrapper = new LambdaQueryWrapper<>();
            unreadWrapper.eq(ConsultationMessage::getSessionId, session.getId())
                    .ne(ConsultationMessage::getSenderType, userType)
                    .eq(ConsultationMessage::getIsRead, 0);

            Long count = consultationMessageMapper.selectCount(unreadWrapper);
            ConsultationUnreadVO unreadVO = new ConsultationUnreadVO();
            unreadVO.setSessionId(session.getId());
            unreadVO.setUnreadCount(Math.toIntExact(count));
            unreadVOList.add(unreadVO);
        }

        return unreadVOList;
    }

    @Override
    public void ackDelivered(Long sessionId, Long messageId, Long receiverId, Integer receiverType) {
        if (sessionId == null || messageId == null || receiverId == null || receiverType == null) {
            throw new BaseException("送达回执参数不完整");
        }

        validateCurrentUser(receiverId, receiverType);
        validateSessionParticipant(sessionId, receiverId, receiverType);

        ConsultationMessage message = consultationMessageMapper.selectById(messageId);
        if (message == null || !sessionId.equals(message.getSessionId())) {
            throw new BaseException("消息不存在");
        }

        String ackPayload = "{"
                + "\"type\":\"delivered_ack\"," 
                + "\"sessionId\":" + sessionId + ","
                + "\"messageId\":" + messageId + ","
                + "\"receiverId\":" + receiverId
                + "}";

        pushToPeer(sessionId, receiverType, ackPayload);
    }

    @Override
    public ConsultationSyncVO syncConversationState(Long sessionId) {
        Long currentUserId = getCurrentUserId();
        Integer currentUserType = getCurrentSenderType();
        validateSessionParticipant(sessionId, currentUserId, currentUserType);

        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        ConsultationSyncVO syncVO = new ConsultationSyncVO();
        syncVO.setSessionId(sessionId);
        syncVO.setSessionStatus(session.getStatus());

        if (ConsultationConstant.RESIDENT_SENDER.equals(currentUserType)) {
            syncVO.setPeerId(session.getDoctorId());
            syncVO.setPeerName(getUserNameByType(session.getDoctorId(), ConsultationConstant.DOCTOR_SENDER));
        } else {
            syncVO.setPeerId(session.getResidentId());
            syncVO.setPeerName(getUserNameByType(session.getResidentId(), ConsultationConstant.RESIDENT_SENDER));
        }

        LambdaQueryWrapper<ConsultationMessage> lastMsgWrapper = new LambdaQueryWrapper<>();
        lastMsgWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                .orderByDesc(ConsultationMessage::getCreateTime)
                .orderByDesc(ConsultationMessage::getId)
                .last("limit 1");
        ConsultationMessage lastMessage = consultationMessageMapper.selectOne(lastMsgWrapper);

        if (lastMessage != null) {
            syncVO.setLastMessageId(lastMessage.getId());
            syncVO.setLastMessageType(lastMessage.getMessageType());
            syncVO.setLastMessageContent(lastMessage.getContent());
            syncVO.setLastMessageTime(lastMessage.getCreateTime());
        }

        LambdaQueryWrapper<ConsultationMessage> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                .ne(ConsultationMessage::getSenderType, currentUserType)
                .eq(ConsultationMessage::getIsRead, 0);
        syncVO.setUnreadCount(Math.toIntExact(consultationMessageMapper.selectCount(unreadWrapper)));

        return syncVO;
    }

    private void sendWebSocketMessage(Long sessionId, ConsultationMessage message) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            return;
        }

        String messageContent = buildMessageContent(message);
        String doctorSid = "doctor_" + session.getDoctorId();
        String residentSid = "resident_" + session.getResidentId();

        if (webSocketServer.isUserOnline("doctor", session.getDoctorId())) {
            webSocketServer.sendToClient(doctorSid, messageContent);
        }
        if (webSocketServer.isUserOnline("resident", session.getResidentId())) {
            webSocketServer.sendToClient(residentSid, messageContent);
        }
    }

    private void pushToPeer(Long sessionId, Integer currentType, String payload) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            return;
        }

        String peerSid;
        if (ConsultationConstant.RESIDENT_SENDER.equals(currentType)) {
            peerSid = "doctor_" + session.getDoctorId();
            if (webSocketServer.isUserOnline("doctor", session.getDoctorId())) {
                webSocketServer.sendToClient(peerSid, payload);
            }
        } else {
            peerSid = "resident_" + session.getResidentId();
            if (webSocketServer.isUserOnline("resident", session.getResidentId())) {
                webSocketServer.sendToClient(peerSid, payload);
            }
        }
    }

    private String buildMessageContent(ConsultationMessage message) {
        return "{"
                + "\"type\":\"message\"," 
                + "\"messageId\":" + message.getId() + ","
                + "\"sessionId\":" + message.getSessionId() + ","
                + "\"senderType\":" + message.getSenderType() + ","
                + "\"senderId\":" + message.getSenderId() + ","
                + "\"messageType\":" + message.getMessageType() + ","
                + "\"content\":\"" + escapeJsonString(message.getContent()) + "\","
                + "\"timestamp\":\"" + message.getCreateTime() + "\""
                + "}";
    }

    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private Long getCurrentUserId() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        return userId;
    }

    private Integer getCurrentSenderType() {
        Integer role = BaseContext.getCurrentRole();
        if (role == null) {
            throw new BaseException("用户身份无效");
        }
        if (role == 1) {
            return ConsultationConstant.DOCTOR_SENDER;
        }
        if (role == 2) {
            return ConsultationConstant.RESIDENT_SENDER;
        }
        throw new BaseException("当前角色不支持在线问诊");
    }

    private void validateCurrentUser(Long userId, Integer userType) {
        Long currentUserId = getCurrentUserId();
        Integer currentUserType = getCurrentSenderType();
        if (!currentUserId.equals(userId) || !currentUserType.equals(userType)) {
            throw new BaseException("非法用户参数");
        }
    }

    private ConsultationSessionVO convertToSessionVO(ConsultationSession session) {
        ConsultationSessionVO vo = new ConsultationSessionVO();
        vo.setId(session.getId());
        vo.setResidentId(session.getResidentId());
        vo.setDoctorId(session.getDoctorId());
        vo.setStatus(session.getStatus());
        vo.setCreateTime(session.getCreateTime());
        vo.setResidentName(getUserNameByType(session.getResidentId(), ConsultationConstant.RESIDENT_SENDER));
        vo.setDoctorName(getUserNameByType(session.getDoctorId(), ConsultationConstant.DOCTOR_SENDER));
        return vo;
    }

    private ConsultationMessageVO convertToMessageVO(ConsultationMessage message) {
        ConsultationMessageVO vo = new ConsultationMessageVO();
        vo.setId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setSenderType(message.getSenderType());
        vo.setSenderId(message.getSenderId());
        vo.setMessageType(message.getMessageType());
        vo.setContent(message.getContent());
        vo.setDuration(message.getDuration());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
        vo.setSenderName(getUserNameByType(message.getSenderId(), message.getSenderType()));
        return vo;
    }

    private String getUserNameByType(Long userId, Integer userType) {
        if (ConsultationConstant.RESIDENT_SENDER.equals(userType)) {
            LambdaQueryWrapper<ResidentProfile> residentQuery = new LambdaQueryWrapper<>();
            residentQuery.eq(ResidentProfile::getUserId, userId).last("limit 1");
            ResidentProfile resident = residentMapper.selectOne(residentQuery);
            return resident == null ? null : resident.getName();
        }

        if (ConsultationConstant.DOCTOR_SENDER.equals(userType)) {
            LambdaQueryWrapper<DoctorProfile> doctorQuery = new LambdaQueryWrapper<>();
            doctorQuery.eq(DoctorProfile::getUserId, userId).last("limit 1");
            DoctorProfile doctor = doctorProfileMapper.selectOne(doctorQuery);
            return doctor == null ? null : doctor.getName();
        }

        return null;
    }
}
