package com.village.portal.constants;

public final class AppConstants {

    private AppConstants() {}

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE    = 10;
    public static final int MAX_PAGE_SIZE        = 100;

    // File upload
    public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L; // 10 MB
    public static final String[] ALLOWED_MIME_TYPES = {
            "image/jpeg", "image/png", "image/jpg", "application/pdf"
    };

    // Business rules
    public static final int MAX_PROGRESS_PERCENT = 100;
    public static final int MIN_PROGRESS_PERCENT = 0;

    // Financial year pattern
    public static final String FINANCIAL_YEAR_PATTERN = "^\\d{4}-\\d{2}$";

    // Table names (used in audit logs)
    public static final String TABLE_USERS              = "users";
    public static final String TABLE_FUNDS              = "funds";
    public static final String TABLE_PROJECTS           = "projects";
    public static final String TABLE_CONTRACTORS        = "contractors";
    public static final String TABLE_PROJECT_CONTRACTORS= "project_contractors";
    public static final String TABLE_EXPENDITURES       = "expenditures";
    public static final String TABLE_DOCUMENTS          = "documents";
}
