package com.PTU.service;

import com.PTU.dto.AiChatRequestDTO;
import com.PTU.result.Result;
import com.PTU.vo.AiChatReplyVO;

public interface BookstoreAiAssistantService {

    Result<AiChatReplyVO> chat(AiChatRequestDTO request);
}
