package com.example.gateway.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
@Slf4j
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        System.out.println("Global Filter MVC: " + httpRequest.getRequestURI());

        // Xử lý Logic Pre-filter ở đây
        log.info("ghi log request trước khi đi vào service con");

        chain.doFilter(request, response);

    }
}
