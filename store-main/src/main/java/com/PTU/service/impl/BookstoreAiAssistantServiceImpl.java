package com.PTU.service.impl;

import com.PTU.dto.AiChatRequestDTO;
import com.PTU.entity.Book;
import com.PTU.properties.BookstoreAiProperties;
import com.PTU.result.Result;
import com.PTU.service.BookService;
import com.PTU.service.BookstoreAiAssistantService;
import com.PTU.service.SpecialOfferService;
import com.PTU.service.SystemNoticeService;
import com.PTU.vo.AiChatReplyVO;
import com.PTU.vo.SpecialOfferVO;
import com.PTU.vo.SystemNoticeVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class BookstoreAiAssistantServiceImpl implements BookstoreAiAssistantService {

    private static final int MAX_USER_MESSAGE_LEN = 2000;
    private static final int MAX_OFFERS_IN_CONTEXT = 24;
    private static final int MAX_CHEAPEST_BOOKS = 18;
    private static final int MAX_PRIEST_BOOKS = 8;
    private static final int MAX_KEYWORD_BOOKS_TOTAL = 24;
    private static final int KEYWORD_SEARCH_LIMIT_EACH = 12;
    private static final int MAX_QUERY_KEYWORDS = 8;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Pattern BOOK_TITLE_GUILLEMET = Pattern.compile("《([^》\\n]{1,60})》");
    private static final Pattern CHINESE_RUN = Pattern.compile("[\\u4e00-\\u9fff]{2,30}");
    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^\\u4e00-\\u9fffA-Za-z0-9《》._\\-]+");

    private static final Set<String> KEYWORD_STOP = new HashSet<>(Arrays.asList(
            "我想知道", "请问", "一下", "告诉", "查询", "帮我", "麻烦", "这本书", "那本", "这本",
            "有没有", "有没有卖", "有卖", "店里", "系统内", "是否", "吗", "呢", "啊", "嘛",
            "最便宜", "最低价", "最贵", "最高价", "价格最高", "哪一本", "一本", "什么书", "最便宜的书",
            "图书", "书籍", "书店", "在售", "网站", "搜索", "浏览", "有没有那", "推荐", "介绍"));

    private static final String[] KEY_PREFIX_STRIP = {
            "店里有没有卖", "店里有没有", "有没有卖", "有没有", "是否卖", "我想买", "想买", "请问",
            "我想知道", "想知道", "查询", "帮我查", "麻烦帮", "系统内有没有", "系统内"
    };

    private static final String[] KEY_SUFFIX_STRIP = {
            "这本书", "这本", "那本", "这本书吗", "吗", "呢", "啊", "嘛"
    };

    @Autowired
    private BookstoreAiProperties aiProperties;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SpecialOfferService specialOfferService;
    @Autowired
    private SystemNoticeService systemNoticeService;
    @Autowired
    private BookService bookService;

    @Override
    public Result<AiChatReplyVO> chat(AiChatRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            return Result.error("请输入内容");
        }
        String userMsg = request.getMessage().trim();
        if (userMsg.length() > MAX_USER_MESSAGE_LEN) {
            return Result.error("单次提问请勿超过 " + MAX_USER_MESSAGE_LEN + " 字");
        }
        if (!aiProperties.isEnabled()) {
            return Result.error("书店助手未启用：请在配置中设置 store.ai.enabled=true 并配置 store.ai.api-key（见 application-dev.yml 说明）");
        }
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            return Result.error("未配置大模型 API Key：可在 application-local.yml 填写 store.ai.api-key，或设置环境变量 DASHSCOPE_API_KEY（阿里百炼）/ SILICONFLOW_API_KEY / STORE_AI_API_KEY（见 application-dev.yml 说明）");
        }

        String systemPrompt = buildSystemPrompt(userMsg);
        String bodyJson = buildRequestBody(userMsg, systemPrompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey().trim());
        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    aiProperties.getChatUrl().trim(),
                    entity,
                    String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return Result.error("模型服务返回异常，请稍后重试");
            }
            String reply = extractReply(resp.getBody());
            if (!StringUtils.hasText(reply)) {
                return Result.error("模型未返回有效内容，请换种方式提问");
            }
            return Result.success(new AiChatReplyVO(reply.trim()));
        } catch (HttpStatusCodeException e) {
            log.warn("AI 请求 HTTP 异常: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                return Result.error("API Key 无效或无权限，请检查 store.ai.api-key 与 chat-url、model 是否与厂商一致");
            }
            if (e.getStatusCode().value() == 402) {
                return Result.error("大模型账户余额不足或欠费（402）。请在对应平台充值/检查额度；阿里百炼、硅基、DeepSeek 等均为按量计费，新用户一般有试用额度。可调整 store.ai.chat-url、model、api-key，详见 application-dev.yml。");
            }
            return Result.error("调用大模型失败，请稍后重试");
        } catch (RestClientException e) {
            log.error("AI 请求失败", e);
            return Result.error("网络异常，无法连接大模型服务");
        }
    }

    private String buildSystemPrompt(String userMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「莆田学院校园书店」的在线客服助手，只用简体中文回答，语气友好简洁。\n");
        sb.append("你只能根据下面【店内事实数据】（系统公告、特惠活动、在售图书目录片段）作答；其中图书相关回答必须严格依据「在售图书数据」里列出的行，不得编造未列出的书名、价格或库存。\n");
        sb.append("在售图书数据仅为当前快照且条数有限；若用户问的书未出现在片段中，或关键词未命中，请如实说明并建议到网站「浏览/搜索图书」页检索；不要说「知识库没有」就否定全店，应区分「下列片段中未找到」与「全店没有」。\n");
        sb.append("可提示用户：首页有「特惠专区」（/#/special-offer）、「二手书专区」；登录后可查看订单与个人资料。\n\n");
        sb.append("【店内事实数据】\n");

        SystemNoticeVO notice = systemNoticeService.getActive();
        if (notice != null && StringUtils.hasText(notice.getTitle())) {
            sb.append("【系统公告】").append(notice.getTitle()).append("\n");
            if (StringUtils.hasText(notice.getContent())) {
                String c = notice.getContent().trim();
                if (c.length() > 2000) {
                    c = c.substring(0, 2000) + "…";
                }
                sb.append(c).append("\n");
            }
        } else {
            sb.append("【系统公告】当前无启用中的公告。\n");
        }

        List<SpecialOfferVO> offers = specialOfferService.listActive();
        if (offers == null || offers.isEmpty()) {
            sb.append("【特惠专区活动】当前没有进行中的特惠活动。\n");
        } else {
            sb.append("【特惠专区活动】以下为当前有效期内且已启用的活动（下单以专区页为准）：\n");
            int n = Math.min(offers.size(), MAX_OFFERS_IN_CONTEXT);
            for (int i = 0; i < n; i++) {
                SpecialOfferVO o = offers.get(i);
                sb.append(i + 1).append(". 名称：").append(nullToEmpty(o.getName())).append("\n");
                sb.append("   类型：").append(o.getOfferType() != null && o.getOfferType() == 2 ? "组合套餐" : "单品").append("；");
                sb.append("优惠方式：").append(discountDesc(o.getDiscountType(), o.getDiscountValue())).append("\n");
                if (o.getStartTime() != null || o.getEndTime() != null) {
                    sb.append("   时间：");
                    sb.append(o.getStartTime() != null ? o.getStartTime().format(DT) : "不限");
                    sb.append(" ~ ");
                    sb.append(o.getEndTime() != null ? o.getEndTime().format(DT) : "不限");
                    sb.append("\n");
                }
                if (o.getOriginalAmount() != null && o.getDealAmount() != null) {
                    sb.append("   参考原价合计：¥").append(o.getOriginalAmount()).append("，折后约：¥").append(o.getDealAmount()).append("\n");
                }
                if (o.getItems() != null && !o.getItems().isEmpty()) {
                    sb.append("   包含图书：");
                    List<String> titles = new ArrayList<>();
                    for (SpecialOfferVO.Item it : o.getItems()) {
                        if (it != null && StringUtils.hasText(it.getTitle())) {
                            titles.add(it.getTitle() + (it.getQuantity() != null && it.getQuantity() > 1 ? "×" + it.getQuantity() : ""));
                        }
                    }
                    sb.append(String.join("；", titles)).append("\n");
                }
            }
            if (offers.size() > n) {
                sb.append("（另有 ").append(offers.size() - n).append(" 个活动未列出，请到特惠专区页查看完整列表）\n");
            }
        }

        appendBooksSection(sb, userMessage);
        return sb.toString();
    }

    private void appendBooksSection(StringBuilder sb, String userMessage) {
        sb.append("\n【在售图书数据】以下为数据库中状态为「起售」的图书记录片段，供回答书目、标价、库存等问题；完整列表以网站为准。\n");
        long total = bookService.countOnSale();
        sb.append("在售图书总数（起售）：").append(total).append("\n");

        if (total == 0) {
            sb.append("当前没有在售图书。\n");
            return;
        }

        List<Book> cheapest = bookService.listCheapestOnSale(MAX_CHEAPEST_BOOKS);
        BigDecimal minPrice = null;
        for (Book b : cheapest) {
            if (b.getPrice() != null) {
                minPrice = b.getPrice();
                break;
            }
        }
        if (minPrice != null) {
            sb.append("当前可比的最低标价（在下列「低价优先」片段中）：¥").append(minPrice.stripTrailingZeros().toPlainString());
            sb.append("（并列最低价可能有多本）。\n");
        } else {
            sb.append("下列图书标价字段多为空，无法比较最低价。\n");
        }

        sb.append("「低价优先」片段（最多 ").append(MAX_CHEAPEST_BOOKS).append(" 本，按标价升序）：\n");
        for (Book b : cheapest) {
            appendBookLine(sb, b);
        }

        if (wantsPriciestHint(userMessage)) {
            List<Book> priciest = bookService.listPriciestOnSale(MAX_PRIEST_BOOKS);
            sb.append("「高价参考」片段（最多 ").append(MAX_PRIEST_BOOKS).append(" 本，按标价降序，仅在与「最贵/最高价」类问题相关时使用）：\n");
            for (Book b : priciest) {
                appendBookLine(sb, b);
            }
        }

        List<String> keywords = extractBookQueryKeywords(userMessage);
        if (!keywords.isEmpty()) {
            LinkedHashMap<Long, Book> matched = new LinkedHashMap<>();
            for (String kw : keywords) {
                for (Book b : bookService.searchOnSaleByKeyword(kw, KEYWORD_SEARCH_LIMIT_EACH)) {
                    matched.putIfAbsent(b.getId(), b);
                    if (matched.size() >= MAX_KEYWORD_BOOKS_TOTAL) {
                        break;
                    }
                }
                if (matched.size() >= MAX_KEYWORD_BOOKS_TOTAL) {
                    break;
                }
            }
            sb.append("与用户提问相关的可能匹配（书名/作者/ISBN 模糊包含关键词「")
                    .append(String.join("」「", keywords))
                    .append("」，去重后最多 ").append(MAX_KEYWORD_BOOKS_TOTAL).append(" 条；无结果表示关键词未命中，不代表全店无货）：\n");
            if (matched.isEmpty()) {
                sb.append("（无命中条目）\n");
            } else {
                for (Book b : matched.values()) {
                    appendBookLine(sb, b);
                }
            }
        }
    }

    private static boolean wantsPriciestHint(String userMessage) {
        if (!StringUtils.hasText(userMessage)) {
            return false;
        }
        String m = userMessage;
        return m.contains("最贵") || m.contains("最高价") || m.contains("价格最高") || m.contains("最贵的一")
                || m.contains("哪本最贵") || m.contains("最贵的书");
    }

    /**
     * 从用户句子里抽取可能的书名/关键词，用于 SQL 模糊匹配。
     */
    private static List<String> extractBookQueryKeywords(String userMessage) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (!StringUtils.hasText(userMessage)) {
            return new ArrayList<>();
        }
        String msg = userMessage.trim();

        Matcher gm = BOOK_TITLE_GUILLEMET.matcher(msg);
        while (gm.find()) {
            addKeyword(out, gm.group(1));
        }

        Matcher cm = CHINESE_RUN.matcher(msg);
        while (cm.find()) {
            addKeyword(out, cm.group());
        }

        for (String raw : TOKEN_SPLIT.split(msg)) {
            String t = raw.trim();
            if (t.startsWith("《") && t.endsWith("》") && t.length() >= 4) {
                t = t.substring(1, t.length() - 1).trim();
            }
            addKeyword(out, t);
        }

        List<String> list = new ArrayList<>(out);
        return list.subList(0, Math.min(MAX_QUERY_KEYWORDS, list.size()));
    }

    private static void addKeyword(LinkedHashSet<String> out, String raw) {
        if (!StringUtils.hasText(raw)) {
            return;
        }
        String t = normalizeQueryKeyword(raw.trim());
        if (t.length() < 2 || t.length() > 60) {
            return;
        }
        if (KEYWORD_STOP.contains(t) || isWeakToken(t)) {
            return;
        }
        out.add(t);
    }

    /** 去掉「有没有卖」等口语前缀/后缀，便于书名模糊匹配 */
    private static String normalizeQueryKeyword(String s) {
        String t = s.trim();
        boolean changed = true;
        while (changed && StringUtils.hasText(t)) {
            changed = false;
            for (String p : KEY_PREFIX_STRIP) {
                if (t.startsWith(p) && t.length() > p.length()) {
                    t = t.substring(p.length()).trim();
                    changed = true;
                }
            }
            for (String suf : KEY_SUFFIX_STRIP) {
                if (t.endsWith(suf) && t.length() > suf.length()) {
                    t = t.substring(0, t.length() - suf.length()).trim();
                    changed = true;
                }
            }
        }
        return t;
    }

    /** 过滤过短或纯数字等弱关键词，减少误命中 */
    private static boolean isWeakToken(String t) {
        if (t.length() < 2) {
            return true;
        }
        if (t.matches("\\d+")) {
            return t.length() < 10;
        }
        if (t.matches("[a-zA-Z]")) {
            return true;
        }
        return false;
    }

    private static void appendBookLine(StringBuilder sb, Book b) {
        sb.append("· id=").append(b.getId() != null ? b.getId() : "—");
        sb.append(" 《").append(nullToEmpty(b.getTitle())).append("》");
        sb.append(" 作者：").append(nullToEmpty(b.getAuthor()));
        sb.append(" ISBN：").append(nullToEmpty(b.getIsbn()));
        sb.append(" 标价：");
        if (b.getPrice() != null) {
            sb.append("¥").append(b.getPrice().stripTrailingZeros().toPlainString());
        } else {
            sb.append("—");
        }
        sb.append(" 库存：").append(b.getStock() != null ? b.getStock() : "—");
        sb.append("\n");
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String discountDesc(Integer discountType, BigDecimal discountValue) {
        if (discountType == null || discountValue == null) {
            return "见活动说明";
        }
        if (discountType == 1) {
            return discountValue.stripTrailingZeros().toPlainString() + "% 折扣（按原价百分比）";
        }
        if (discountType == 2) {
            return "一口价 ¥" + discountValue.stripTrailingZeros().toPlainString();
        }
        if (discountType == 3) {
            return "立减 ¥" + discountValue.stripTrailingZeros().toPlainString();
        }
        return "见活动说明";
    }

    private String buildRequestBody(String userMessage, String systemPrompt) {
        JSONObject root = new JSONObject();
        root.put("model", aiProperties.getModel());
        JSONArray messages = new JSONArray();
        JSONObject sys = new JSONObject();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", userMessage);
        messages.add(user);
        root.put("messages", messages);
        root.put("temperature", 0.5);
        root.put("max_tokens", 1024);
        return root.toJSONString();
    }

    private String extractReply(String responseBody) {
        try {
            JSONObject root = JSON.parseObject(responseBody);
            if (root == null) {
                return null;
            }
            JSONArray choices = root.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            JSONObject first = choices.getJSONObject(0);
            JSONObject message = first.getJSONObject("message");
            if (message == null) {
                return null;
            }
            return message.getString("content");
        } catch (Exception e) {
            log.warn("解析模型响应失败: {}", e.getMessage());
            return null;
        }
    }
}
