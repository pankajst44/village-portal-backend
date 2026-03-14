package com.village.portal.constants;

public final class AppConstants {

    private AppConstants() {}

    // ── Pagination ────────────────────────────────────────────
    public static final int DEFAULT_PAGE_SIZE    = 10;
    public static final int MAX_PAGE_SIZE        = 100;

    // ── File upload ───────────────────────────────────────────
    public static final long   MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L;
    public static final String[] ALLOWED_MIME_TYPES = {
            "image/jpeg", "image/png", "image/jpg", "application/pdf"
    };

    // ── Business rules (existing) ─────────────────────────────
    public static final int MAX_PROGRESS_PERCENT  = 100;
    public static final int MIN_PROGRESS_PERCENT  = 0;
    public static final String FINANCIAL_YEAR_PATTERN = "^\\d{4}-\\d{2}$";

    // ── Table names (existing) ────────────────────────────────
    public static final String TABLE_USERS               = "users";
    public static final String TABLE_FUNDS               = "funds";
    public static final String TABLE_PROJECTS            = "projects";
    public static final String TABLE_CONTRACTORS         = "contractors";
    public static final String TABLE_PROJECT_CONTRACTORS = "project_contractors";
    public static final String TABLE_EXPENDITURES        = "expenditures";
    public static final String TABLE_DOCUMENTS           = "documents";

    // ── CMS table names ───────────────────────────────────────
    public static final String TABLE_COMPLAINTS          = "complaints";
    public static final String TABLE_COMPLAINT_TIMELINE  = "complaint_timeline";
    public static final String TABLE_COMPLAINT_EVIDENCE  = "complaint_evidence";
    public static final String TABLE_ESCALATION_LOG      = "escalation_log";
    public static final String TABLE_VILLAGES            = "villages";

    // ── CMS business rules ────────────────────────────────────
    public static final int  COMPLAINT_MIN_DESCRIPTION_LENGTH = 20;
    public static final int  COMPLAINT_MAX_EVIDENCE_FILES     = 5;
    public static final int  COMPLAINT_RATE_LIMIT_PER_DAY     = 3;
    public static final int  CITIZEN_RESOLUTION_RESPONSE_DAYS = 7;
    public static final int  DUPLICATE_DETECTION_WINDOW_DAYS  = 30;
    public static final int  SUPPORT_THRESHOLD_FOR_ESCALATION = 50;
    public static final long DEFAULT_VILLAGE_ID               = 1L;

    // ── OTP ───────────────────────────────────────────────────
    public static final int OTP_EXPIRY_MINUTES  = 10;
    public static final int OTP_MAX_ATTEMPTS    = 5;
    public static final int OTP_LENGTH          = 6;
}
