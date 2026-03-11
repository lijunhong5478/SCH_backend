package com.tyut.controller.doctor;

import com.tyut.constant.ConsultationConstant;
import com.tyut.context.BaseContext;
import com.tyut.dto.ConsultationQueryDTO;
import com.tyut.dto.DeliveryAckDTO;
import com.tyut.dto.MarkSessionReadDTO;
import com.tyut.dto.SendImageMessageDTO;
import com.tyut.dto.SendMessageDTO;
import com.tyut.dto.StartConsultationDTO;
import com.tyut.result.Result;
import com.tyut.service.ConsultationService;
import com.tyut.vo.ConsultationMessageVO;
import com.tyut.vo.ConsultationSessionVO;
import com.tyut.vo.ConsultationSyncVO;
import com.tyut.vo.ConsultationUnreadVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("doctorConsultationController")
@RequestMapping("/doctor/consultation")
@Api(tags = "医生-咨询接口")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @PostMapping("/session/ensure")
    @ApiOperation("创建或获取进行中会话")
    public Result<Long> createOrGetActiveSession(@RequestBody StartConsultationDTO dto) {
        Long sessionId = consultationService.createOrGetActiveSession(BaseContext.getCurrentId(), dto.getResidentId());
        return Result.success(sessionId);
    }

    @PostMapping("/message/text")
    @ApiOperation("发送文本消息")
    public Result<String> sendTextMessage(@RequestBody SendMessageDTO dto) {
        consultationService.sendMessage(dto);
        return Result.success();
    }

    @PostMapping("/message/image")
    @ApiOperation("发送图片消息")
    public Result<String> sendImageMessage(@RequestBody SendImageMessageDTO dto) {
        consultationService.handleImageMessage(dto);
        return Result.success();
    }

    @GetMapping("/sessions")
    @ApiOperation("查询会话列表")
    public Result<List<ConsultationSessionVO>> listSessions(ConsultationQueryDTO queryDTO) {
        return Result.success(consultationService.listSessions(queryDTO));
    }

    @GetMapping("/messages/incremental/{sessionId}")
    @ApiOperation("增量查询会话消息")
    public Result<List<ConsultationMessageVO>> listMessagesAfter(
            @PathVariable Long sessionId,
            @RequestParam(value = "lastMessageId", required = false) Long lastMessageId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        return Result.success(consultationService.listMessagesAfter(sessionId, lastMessageId, limit));
    }

    @PutMapping("/messages/read-up-to")
    @ApiOperation("会话消息批量已读")
    public Result<String> markSessionReadUpTo(@RequestBody MarkSessionReadDTO dto) {
        consultationService.markSessionReadUpTo(
                dto.getSessionId(),
                BaseContext.getCurrentId(),
                ConsultationConstant.DOCTOR_SENDER,
                dto.getLastReadMessageId());
        return Result.success();
    }

    @GetMapping("/sessions/unread")
    @ApiOperation("查询会话未读统计")
    public Result<List<ConsultationUnreadVO>> unreadBySession() {
        return Result.success(consultationService.getUnreadCountBySession(
                BaseContext.getCurrentId(),
                ConsultationConstant.DOCTOR_SENDER));
    }

    @PostMapping("/messages/delivered-ack")
    @ApiOperation("上报消息送达回执")
    public Result<String> ackDelivered(@RequestBody DeliveryAckDTO dto) {
        consultationService.ackDelivered(
                dto.getSessionId(),
                dto.getMessageId(),
                BaseContext.getCurrentId(),
                ConsultationConstant.DOCTOR_SENDER);
        return Result.success();
    }

    @GetMapping("/sync/{sessionId}")
    @ApiOperation("同步会话状态")
    public Result<ConsultationSyncVO> syncConversationState(@PathVariable Long sessionId) {
        return Result.success(consultationService.syncConversationState(sessionId));
    }
}
