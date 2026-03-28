package com.PTU.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 兼容 OpenAI Chat Completions 的大模型，需在对应平台申请 API Key。
 */
@Component
@ConfigurationProperties(prefix = "store.ai")
@Data
public class BookstoreAiProperties {

    /** 关闭时接口返回提示，不调用外网 */
    private boolean enabled = false;

    private String apiKey = "";

    /**
     * 完整 Chat Completions URL（OpenAI 兼容）。
     * 阿里百炼/通义千问: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
     * 硅基流动: https://api.siliconflow.cn/v1/chat/completions
     * DeepSeek: https://api.deepseek.com/chat/completions
     * 智谱: https://open.bigmodel.cn/api/paas/v4/chat/completions
     */
    private String chatUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /** 与 chat-url 厂商一致，如 qwen-turbo、qwen-plus、deepseek-chat、glm-4-flash */
    private String model = "qwen-turbo";
}
