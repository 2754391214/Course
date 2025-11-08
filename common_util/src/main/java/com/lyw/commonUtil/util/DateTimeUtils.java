package com.lyw.commonUtil.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateTimeUtils {

    public static DateTimeFormatter getDateTimeFormatter(String formatter) {
        return DateTimeFormatter.ofPattern(formatter);
    }
    /**
     * 获取当前时间 格式 yyyy-MM-dd HH:mm:ss.SSSSSSS
     *
     * @return
     */
    public static String getCurrentDateTime() {
        return parseLocalDateTime(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss.SSSSSSS");
    }
    /**
     * 获取当前时间到第二天凌晨00：00 相隔多少分钟
     * @return
     */
    public static int getMinuteToNextDay(){
        //当前的时间
        LocalDateTime currentDayTime = LocalDateTime.now();
        LocalDate currentDay = LocalDate.now();
        LocalDate nextDay = currentDay.plusDays(1);
        LocalDateTime nextDayTime = nextDay.atStartOfDay();
        return (int)currentDayTime.until(nextDayTime,ChronoUnit.MINUTES);
    }

    /**
     * 获取当前时间到第二年凌晨00：00 相隔多少分钟
     * @return
     */
    public static int getMinuteToNextYear(){
        //当前的时间
        LocalDateTime currentDayTime = LocalDateTime.now();
        LocalDate currentDay = LocalDate.now();
        LocalDate nextDay = currentDay.plusYears(1);
        LocalDateTime nextDayTime = nextDay.atStartOfDay();
        return (int)currentDayTime.until(nextDayTime,ChronoUnit.MINUTES);
    }

    /**
     * 获取当前时间到第二個月凌晨00：00 相隔多少分钟
     * @return
     */
    public static int getMinuteToNextMonth(){
        //当前的时间
        LocalDateTime currentDayTime = LocalDateTime.now();
        LocalDate currentDay = LocalDate.now();
        LocalDate nextDay = currentDay.plusMonths(1);
        LocalDateTime nextDayTime = nextDay.atStartOfDay();
        return (int)currentDayTime.until(nextDayTime,ChronoUnit.MINUTES);
    }

    /**
     * 返回当前的日期
     */
    public static LocalDate getCurrentLocalDate() {
        return LocalDate.now();
    }

    /**
     * 返回当前的日期
     */
    public static Date getCurrentDate() {
        return toDate(LocalDate.now());
    }

    /**
     * 返回当前时间
     */
    public static LocalTime getCurrentLocalTime() {
        return LocalTime.now();
    }

    /**
     * 返回当前日期时间
     */
    public static LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 返回当前日期时间字符串
     */
    public static String getNow() {
        return parseLocalDateTime(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss.SSS");
    }

    /**
     * 日期相隔秒
     */
    public static long periodHours(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return Duration.between(startDateTime, endDateTime).get(ChronoUnit.SECONDS);
    }

    /**
     * 日期相隔天数
     */
    public static long periodDays(LocalDate startDate, LocalDate endDate) {
        return startDate.until(endDate, ChronoUnit.DAYS);
    }

    /**
     * 日期相隔周数
     */
    public static long periodWeeks(LocalDate startDate, LocalDate endDate) {
        return startDate.until(endDate, ChronoUnit.WEEKS);
    }

    /**
     * 日期相隔月数
     */
    public static long periodMonths(LocalDate startDate, LocalDate endDate) {
        return startDate.until(endDate, ChronoUnit.MONTHS);
    }

    /**
     * 日期相隔年数
     */
    public static long periodYears(LocalDate startDate, LocalDate endDate) {
        return startDate.until(endDate, ChronoUnit.YEARS);
    }

    /**
     * 是否当天
     */
    public static boolean isToday(LocalDate date) {
        return getCurrentLocalDate().equals(date);
    }

    /**
     * 获取当前毫秒数
     */
    public static Long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 判断是否为闰年
     */
    public static boolean isLeapYear(LocalDate localDate) {
        return localDate.isLeapYear();
    }

    /**
     * LocalDate --> date
     */
    public static Date toDate(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * LocalDateTime --> date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * LocalTime --> date
     */
    public static Date toDate(LocalTime localTime) {
        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * java.util.Date --> java.time.LocalDateTime
     *
     * @param date
     * @return
     */
    public static LocalTime dateToLocalTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalTime();
    }

    /**
     * java.util.Date --> java.time.LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * java.util.Date --> java.time.LocalDate
     */
    public static LocalDate dateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalDate();
    }

    /**
     * java.util.Date --> java.util.String
     */
    public static String parseDate(Date date,String pattern) {
        LocalDate localDate = dateToLocalDate(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return localDate.format(dateTimeFormatter);

    }
    public static String parseDateTime(Date date,String pattern) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(dateTimeFormatter);

    }

    /**
     * java.util.String --> java.util.Date
     */
    public static Date parseString(String dateString,String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate date = LocalDate.parse(dateString, formatter);
        return toDate(date);
    }

    /**
     * java.util.String --> java.util.Date
     */
    public static Date parseDatetimeString(String dateString,String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime date = LocalDateTime.parse(dateString, formatter);
        return toDate(date);
    }

    /**
     * java.util.String --> java.util.Date
     */
    public static Date parseIOSString(String isoString) {
        if (!isValidISODate(isoString))return null;
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(isoString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return Date.from(offsetDateTime.toInstant());
    }
    /**
     * java.time.Date --> java.util.String
     */
    public static String parseDateToIOSString(Date date) {
        if (ObjectUtil.isEmpty(date))return null;
        Instant instant = date.toInstant();
        OffsetDateTime offsetDateTime = instant.atOffset(ZoneOffset.ofHours(8));
        return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
    /**
     * java.time.LocalDateTime --> java.util.String
     */
    public static String parseLocalDateTime(LocalDateTime localDateTime,String pattern) {
        if (ObjectUtil.isEmpty(localDateTime))return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(formatter);
    }

    /**
     * java.lang.Long --> java.util.String
     */
    public static String parseTimestampe(Long timestamp,String pattern) {
        if (ObjectUtil.isNull(timestamp))return null;
        // 将时间戳转换为 Instant 对象
        Instant instant = Instant.ofEpochMilli(timestamp);
        // 将 Instant 转换为 ZonedDateTime（可以根据需要选择时区，以下为系统默认时区）
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        // 格式化为字符串（年月日 时:分:秒）
        String formattedDate = DateTimeFormatter
                .ofPattern(StrUtil.isNotBlank(pattern)?pattern:"yyyy-MM-dd HH:mm:ss")
                .format(zonedDateTime);
        return formattedDate;
    }

    // 判断上传时间是否为空或ISO格式无效
    public static boolean isValidISODate(String uploadTime) {
        if (StrUtil.isBlank(uploadTime)) {
            return false;
        }
        // 定义ISO 8601日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        try {
            // 尝试解析时间字符串
            LocalDateTime.parse(uploadTime, formatter);
            return true; // 如果解析成功，说明时间有效
        } catch (DateTimeParseException e) {
            return false; // 如果抛出异常，说明时间格式无效
        }
    }

    /**
     *
     * @param date
     * @param days
     * @return
     */
    public static Date getPeriodDayToDate(Date date, Integer days){
        LocalDate localDate = date!=null?dateToLocalDate(date):LocalDate.now();
        return toDate(localDate.plusDays(days));
    }

    /**
     *
     * @param date
     * @param months
     * @return
     */
    public static Date getPeriodDayToMonth(Date date, Integer months){
        LocalDate localDate = date!=null?dateToLocalDate(date):LocalDate.now();
        return toDate(localDate.plusMonths(months));
    }

    // 获取当前时间后指定分钟数的时间
    public static Date getTimeAfterMinutes(int minutes) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 获取当前时间后指定分钟数的时间
        LocalDateTime targetTime = now.plusMinutes(minutes);
        // 将 LocalDateTime 转换为 Date
        Date date = Date.from(targetTime.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }
    // 获取当前时间前指定分钟数的时间
    public static Date getTimeBeforeMinutes(int minutes,LocalDateTime now) {
        // 获取当前时间
        now = ObjectUtil.isNotNull(now)?now:LocalDateTime.now();
        // 获取当前时间前指定分钟数的时间
        LocalDateTime targetTime = now.minusMinutes(minutes);
        // 将 LocalDateTime 转换为 Date
        Date date = Date.from(targetTime.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }
}
