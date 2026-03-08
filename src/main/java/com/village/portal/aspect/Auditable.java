package com.village.portal.aspect;

import com.village.portal.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate any service method to trigger automatic audit logging.
 *
 * Usage:
 *   @Auditable(action = AuditAction.CREATE, tableName = "projects")
 *   public ProjectResponse createProject(...) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    AuditAction action();

    String tableName();

    String description() default "";
}
