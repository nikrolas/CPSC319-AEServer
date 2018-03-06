package com.discovery.channel.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Random;


public class LoggingInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final Random RAN = new Random(System.currentTimeMillis());

    private static final String REQUEST_START_TIME_MDC_KEY = "request.start.time.millis";
    private static final String REQUEST_ID_MDC_KEY = "Request.id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.put(REQUEST_START_TIME_MDC_KEY, String.valueOf(System.currentTimeMillis()));
        MDC.put(REQUEST_ID_MDC_KEY, String.format("[%08d]", RAN.nextLong() & 0X0FFFFFFFFFFFFFFFL));

        LOGGER.info("Received request " + "[" + request.getMethod()
                + "]" + request.getRequestURI() + getParameters(request));

        return true;
    }

    private String getParameters(HttpServletRequest request) {
        StringBuilder posted = new StringBuilder();
        Enumeration<?> e = request.getParameterNames();
        if (e == null) {
            return "";
        }else {
            posted.append("?");
        }
        while (e.hasMoreElements()) {
            if (posted.length() > 1) {
                posted.append("&");
            }
            String curr = (String) e.nextElement();
            posted.append(curr + "=");
            if (curr.contains("password")
                    || curr.contains("pass")
                    || curr.contains("pwd")) {
                posted.append("*****");
            } else {
                posted.append(request.getParameter(curr));
            }
        }
        String ip = request.getHeader("X-FORWARDED-FOR");
        String ipAddr = (ip == null) ? getRemoteAddr(request) : ip;
        if (ipAddr!=null && !ipAddr.equals("")) {
            posted.append("&_psip=" + ipAddr);
        }
        return posted.toString();
    }

    private String getRemoteAddr(HttpServletRequest request) {
        String ipFromHeader = request.getHeader("X-FORWARDED-FOR");
        if (ipFromHeader != null && ipFromHeader.length() > 0) {
            LOGGER.debug("ip from proxy - X-FORWARDED-FOR : " + ipFromHeader);
            return ipFromHeader;
        }
        return request.getRemoteAddr();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long requestStartTime = Long.parseLong(MDC.get(REQUEST_START_TIME_MDC_KEY));
        LOGGER.info("Response time {}ms", System.currentTimeMillis() - requestStartTime);
        MDC.clear();
    }
}
