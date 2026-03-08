package com.village.portal.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.village.portal.entity.AuditLog;
import com.village.portal.repository.AuditLogRepository;
import com.village.portal.security.UserDetailsImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper       = objectMapper;
    }

    /**
     * Intercepts any method annotated with @Auditable.
     * Writes a record to audit_logs AFTER successful execution.
     * On exception, logs a warning but does NOT suppress the original exception.
     */
    @Around("@annotation(com.village.portal.aspect.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);

        // Execute the actual service method first
        Object result = joinPoint.proceed();

        // After successful execution, write audit log
        try {
            writeAuditLog(auditable, joinPoint.getArgs(), result);
        } catch (Exception auditEx) {
            // Audit failure must NEVER break the main operation
            log.error("Audit logging failed for method [{}]: {}",
                    method.getName(), auditEx.getMessage(), auditEx);
        }

        return result;
    }

    private void writeAuditLog(Auditable auditable, Object[] args, Object result) {
        AuditLog auditLog = new AuditLog();
        auditLog.setTableName(auditable.tableName());
        auditLog.setAction(auditable.action());

        auditLog.setRecordId(extractRecordId(result, args));
        // Extract authenticated user details
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
            auditLog.setChangedBy(user.getId());
            auditLog.setChangedByUsername(user.getUsername());
            auditLog.setChangedByRole(user.getRole());
        }

        // Extract IP address from current request
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                auditLog.setIpAddress(extractClientIp(request));
            }
        } catch (Exception e) {
            log.debug("Could not extract IP address for audit log");
        }

        // Serialize result as new_values if it is serializable
        if (result != null) {
            try {
                auditLog.setNewValues(objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                auditLog.setNewValues("{\"error\":\"Could not serialize result\"}");
            }
        }

        // Build a human-readable summary
        String summary = auditable.description().isEmpty()
                ? auditable.action().name() + " on " + auditable.tableName()
                : auditable.description();
        auditLog.setChangeSummary(summary);

        auditLogRepository.save(auditLog);
    }
    private Long extractRecordId(Object result, Object[] args) {
        // 1 — try result.getId()
        if (result != null) {
            try {
                java.lang.reflect.Method getId = result.getClass().getMethod("getId");
                Object id = getId.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (Exception ignored) {
                // result does not have getId() — fall through to arg scan
            }
        }

        // 2 — scan args for the first Long (record ID param on update/delete)
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Long) {
                    return (Long) arg;
                }
            }
        }

        return null;
    }
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
