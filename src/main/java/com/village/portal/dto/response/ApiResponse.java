package com.village.portal.dto.response;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data    = data;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public boolean isSuccess()        { return success; }
    public void setSuccess(boolean v) { this.success = v; }

    public String getMessage()        { return message; }
    public void setMessage(String v)  { this.message = v; }

    public T getData()                { return data; }
    public void setData(T v)          { this.data = v; }
}
