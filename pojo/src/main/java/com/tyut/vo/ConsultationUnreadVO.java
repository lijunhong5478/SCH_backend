package com.tyut.vo;

import lombok.Data;

@Data
public class ConsultationUnreadVO {
    private Long sessionId;
    private Integer unreadCount;
}
