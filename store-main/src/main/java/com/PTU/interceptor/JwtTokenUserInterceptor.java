package com.PTU.interceptor;

import com.PTU.constant.JwtClaimsConstant;
import com.PTU.context.BaseContext;
import com.PTU.properties.JwtProperties;
import com.PTU.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /** 与 WebMvcConfiguration 中 excludePathPatterns 一致；拦截器内再判一次，避免仅「/user/book/{数字}」类路径未命中排除时误拦为 401 */
    private static final String[] PUBLIC_PATH_PATTERNS = {
            "/user/user/login",
            "/user/user/register",
            "/user/user/logout",
            "/user/shop/status",
            "/user/book/**",
            "/user/category/page",
            "/user/major/page",
            "/user/home/recommend",
            "/user/home/banners",
            "/user/secondHand/onSale",
            "/user/secondHand/detail/**"
    };

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        String lookupPath = urlPathHelper.getLookupPathForRequest(request);
        if (!lookupPath.startsWith("/")) {
            lookupPath = "/" + lookupPath;
        }
        // 与 AntPathMatcher 排除规则互补：部分环境下 /user/book/** 未命中时仍误拦为 401，前缀放行最稳妥
        if (lookupPath.startsWith("/user/book/") || "/user/book".equals(lookupPath)) {
            return true;
        }
        for (String pattern : PUBLIC_PATH_PATTERNS) {
            if (pathMatcher.match(pattern, lookupPath)) {
                return true;
            }
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户id：{}", userId);
            BaseContext.setCurrentId(userId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            log.error("JWT校验失败: {}", ex.getMessage(), ex);
            response.setStatus(401);
            return false;
        }
    }
}
