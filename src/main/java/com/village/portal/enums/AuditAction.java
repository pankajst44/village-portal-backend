package com.village.portal.enums;

public enum AuditAction {
    // Existing
    CREATE,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    FILE_UPLOAD,
    FILE_DELETE,
    VERIFY,
    EXPORT,
    // CMS additions
    ASSIGN,
    RESOLVE,
    REJECT,
    ESCALATE,
    VOTE,
    OTP_SENT,
    OTP_VERIFIED,
    REGISTER
}