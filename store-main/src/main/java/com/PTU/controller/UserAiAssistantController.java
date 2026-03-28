package com.PTU.controller;

import com.PTU.dto.AiChatRequestDTO;
import com.PTU.result.Result;
import com.PTU.service.BookstoreAiAssistantService;
import com.PTU.vo.AiChatReplyVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/ai")
@Api(tags = "用户端-AI书店助手")
@Slf4j
public class UserAiAssistantController {

    @Autowired
    private BookstoreAiAssistantService bookstoreAiAssistantService;

    @PostMapping("/chat")
    @ApiOperation("书店助手对话（基于店内公告与特惠数据）")
    public Result<AiChatReplyVO> chat(@RequestBody AiChatRequestDTO request) {
        return bookstoreAiAssistantService.chat(request);
    }
}
