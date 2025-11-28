package com.lyw.commonUtil.constant;

public class RedisKeyConstant {
    public static final long DEFAULT_EXPIRE_SECONDS = 1 * 24 * 60 * 60; // 1天
    public static final String COURSE_INFO = "course_info:";
    public static final String LOCK_COURSE_INFO = "lock:course_info:";
    public static final String ENROLLMENT_STRATEGY = "enrollment_strategy:";
    public static final String LOCK_ENROLLMENT_STRATEGY = "lock:enrollment_strategy:";
    public static final String ENROLLMENT_BLACK = "enrollment_black:";
    public static final String LOCK_ENROLLMENT_BLACK = "lock:enrollment_black:";
    public static final String COURSE_SCHEDULES = "course_schedules:";
    public static final String STUDENT_COURSES = "student:courses:";
    public static final String LOCK_STUDENT_COURSES = "lock:student:courses:";
    public static final String COURSE_CURRENT = "course_current:";

    public static final String SYNC_SAVE_FAVORITE = "sync:save:favorite";
    public static final String LOCK_FAVORITE_ITEM= "lock:favorite_item:%d:%s";
    public static final String FAVORITE_ITEM = "favorite_item:%d:%s";
    public static final String FAVORITE_ITEM_COUNT = "favorite_item:count:%s";
    public static final String LOCK_SYNC_SAVE_FAVORITE = "lock:sync:save:favorite";


    public static final String SYNC_SAVE_LIKE = "sync:save:like";
    public static final String LOCK_LIKE_ITEM= "lock:like:%d:%s";
    public static final String LIKE_ITEM = "like:%d:%s";
    public static final String LIKE_COUNT = "like:count:%s";
    public static final String LOCK_SYNC_SAVE_LIKE = "lock:sync:save:like";


    public static final String RANKING_COURSE_HEAT = "ranking:course_heat";
    public static final String COURSE_HEAT_WHO = "course:heat:%d";
}
