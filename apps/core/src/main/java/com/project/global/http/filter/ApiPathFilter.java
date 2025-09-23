package com.project.global.http.filter;

import jakarta.servlet.*;

import java.io.IOException;

/**
 * 요청 url에서 "/api/" 제거
 */
public class ApiPathFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        /*HttpServletRequest httpRequest = (HttpServletRequest)request;
        String uri = httpRequest.getRequestURI();
        if (uri.startsWith("/api/")) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(uri.substring(4));
            dispatcher.forward(request, response);

            return;
        }
        chain.doFilter(request, response);*/
    }
}
