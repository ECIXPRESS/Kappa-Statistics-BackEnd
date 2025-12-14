package edu.dosw.Kappa_Stats_BackEnd.Exception;


public class ErrorCodes {

    public static final String INVALID_DATE_RANGE = "STATS_001";
    public static final String FUTURE_DATE = "STATS_002";
    public static final String INVALID_STORE_ID = "STATS_003";
    public static final String INVALID_YEAR = "STATS_004";
    public static final String INVALID_MONTH = "STATS_005";

    public static final String STORE_NOT_FOUND = "STATS_101";
    public static final String NO_SALES_DATA = "STATS_102";
    public static final String PRODUCT_NOT_FOUND = "STATS_103";

    public static final String REPORT_GENERATION_FAILED = "STATS_201";
    public static final String EXCEL_GENERATION_FAILED = "STATS_202";

    public static final String DATABASE_ERROR = "STATS_301";
    public static final String EXTERNAL_SERVICE_ERROR = "STATS_302";

    private ErrorCodes() {
    }
}