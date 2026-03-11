package com.tyut.service;

import com.tyut.dto.ConsultationQueryDTO;
import com.tyut.dto.SendImageMessageDTO;
import com.tyut.dto.SendMessageDTO;
import com.tyut.vo.ConsultationMessageVO;
import com.tyut.vo.ConsultationSyncVO;
import com.tyut.vo.ConsultationUnreadVO;
import com.tyut.vo.ConsultationSessionVO;

import java.util.List;

public interface ConsultationService {

    /**
     * 创建或获取进行中的会话
     */
    Long createOrGetActiveSession(Long doctorId, Long residentId);

    /**
     * 发送文本消息
     */
    void sendMessage(SendMessageDTO dto);

    /**
     * 处理图片消息发送
     */
    void handleImageMessage(SendImageMessageDTO dto);

    /**
     * 校验会话参与者
     */
    void validateSessionParticipant(Long sessionId, Long userId, Integer userType);

    /**
     * 查询会话列表
     */
    List<ConsultationSessionVO> listSessions(ConsultationQueryDTO queryDTO);

    /**
     * 增量查询消息
     */
    List<ConsultationMessageVO> listMessagesAfter(Long sessionId, Long lastMessageId, Integer limit);

    /**
     * 将会话中消息批量标记为已读
     */
    void markSessionReadUpTo(Long sessionId, Long receiverId, Integer receiverType, Long lastReadMessageId);

    /**
     * 获取会话维度未读数
     */
    List<ConsultationUnreadVO> getUnreadCountBySession(Long userId, Integer userType);

    /**
     * 处理消息送达回执
     */
    void ackDelivered(Long sessionId, Long messageId, Long receiverId, Integer receiverType);

    /**
     * 会话状态同步
     */
    ConsultationSyncVO syncConversationState(Long sessionId);
}
