package com.example.balaghi.utils;

public class Constants {

    private Constants() {}

    public static final String DATABASE_NAME    = "balaghi.db";
    public static final int    DATABASE_VERSION = 3;

    public static final String TABLE_USERS   = "users";
    public static final String TABLE_REPORTS = "reports";

    public static final String USER_ID            = "id";
    public static final String USER_FULL_NAME     = "full_name";
    public static final String USER_USERNAME      = "username";
    public static final String USER_EMAIL         = "email";
    public static final String USER_PHONE         = "phone";
    public static final String USER_PASSWORD      = "password";
    public static final String USER_BIRTH_DATE    = "birth_date";
    public static final String USER_PROFILE_IMAGE = "profile_image";

    public static final String REPORT_ID           = "id";
    public static final String REPORT_TITLE        = "title";
    public static final String REPORT_DESCRIPTION  = "description";
    public static final String REPORT_CATEGORY     = "category";
    public static final String REPORT_STATUS       = "status";
    public static final String REPORT_LOCATION     = "location";
    public static final String REPORT_IMAGE        = "image";
    public static final String REPORT_DATE         = "date";
    public static final String REPORT_IS_ANONYMOUS = "is_anonymous";
    public static final String REPORT_USER_ID      = "user_id";

    public static final String PREFS_NAME         = "balaghi_prefs";
    public static final String KEY_IS_LOGGED_IN   = "is_logged_in";
    public static final String KEY_USER_ID        = "user_id";
    public static final String KEY_SAVED_EMAIL    = "saved_email";
    public static final String KEY_SAVED_PASSWORD = "saved_password";
    public static final String KEY_REMEMBER_ME    = "remember_me";

    public static final String EXTRA_REPORT  = "extra_report";

    public static final String REPORT_EXTRACTED_TEXT = "extracted_text";
    public static final String REPORT_OCR_IMAGE= "ocr_image";
}