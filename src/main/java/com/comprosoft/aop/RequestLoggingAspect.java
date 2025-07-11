package com.comprosoft.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class RequestLoggingAspect {

	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
	public void restControllerMethods() {
	}

	@Before("restControllerMethods()")
	public void logCallerInfo(JoinPoint joinPoint) {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs == null)
			return;

		HttpServletRequest request = attrs.getRequest();

		String ip = getClientIp(request);
		String host = request.getRemoteHost();
		String uri = request.getRequestURI();
		String method = request.getMethod();
		String userAgent = request.getHeader("User-Agent");
		String referer = request.getHeader("Referer");
        Object[] args = joinPoint.getArgs();
        String arguments = Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest)) // avoid printing full HttpServletRequest object
                .map(Object::toString)
                .reduce("", (a, b) -> a + "\n    🔸 " + b);
        
        log.info("""

                📥 === API Caller Info ===
                🌐 URI:        {}
                📬 Method:     {}
                🧑‍💻 Client IP:  {}
                🖥️ Host:       {}
                🧭 User-Agent: {}
                🔗 Referer:    {}
                🎯 Target:     {}
                📦 Arguments:  {}
                ==========================
                """,
                uri, method, ip, host, userAgent, referer, joinPoint.getSignature(), arguments.isEmpty() ? "None" : arguments);
    }

	
	
	private String getClientIp(HttpServletRequest request) {
		String header = request.getHeader("X-Forwarded-For");
		if (header != null && !header.isEmpty()) {
			return header.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
