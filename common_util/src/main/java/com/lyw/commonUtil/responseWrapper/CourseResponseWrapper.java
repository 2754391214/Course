package com.lyw.commonUtil.responseWrapper;

/**
 * 统一返回结果集
 *
 */
public class CourseResponseWrapper<E> {
    //成功true，失败false
    private boolean success;

    //错误提示信息
    private String errorMessage;

    //返回数据
    private E data;

    public CourseResponseWrapper() {
        super();
    }

    public CourseResponseWrapper(boolean success, String errorMessage, E data) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.data = data;
    }
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    /**
     * 请求成功
     *
     */
    public static <T> CourseResponseWrapper getSuccess() {
        return new CourseResponseWrapper<T>(true, null, null);
    }

    /**
     * 请求成功
     *
     */
    public static <T> CourseResponseWrapper getSuccess(T data) {
        return new CourseResponseWrapper<T>(true, null, data);
    }

    /**
     * 请求成功
     *
     */
    public static <T> CourseResponseWrapper getSuccess(String errorMessage, T data) {
        return new CourseResponseWrapper<T>(true, errorMessage, data);
    }

    /**
     * 请求失败
     *
     */
    public static CourseResponseWrapper getFailed(String errorMessage) {
        return new CourseResponseWrapper<Object>(false, errorMessage, null);
    }

    public static <T> CourseResponseWrapper<T> getFailed(String errorMessage, T data) {
        return new CourseResponseWrapper<>(false, errorMessage, data);
    }
}
