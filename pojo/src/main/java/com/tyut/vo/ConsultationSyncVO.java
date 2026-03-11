package com.tyut.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultationSyncVO {
    private Long sessionId;
    private Integer sessionStatus;
    private Long peerId;
    private String peerName;
    private Long lastMessageId;
    private Integer lastMessageType;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
}
