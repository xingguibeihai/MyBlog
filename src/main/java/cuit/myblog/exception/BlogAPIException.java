package cuit.myblog.exception;

import org.springframework.http.HttpStatus;

/**
 * 自定义 API 异常类。
 * 用于封装带有特定 HTTP 状态码的业务逻辑错误。
 */
public class BlogAPIException extends RuntimeException {

    // 异常对应的 HTTP 状态码
    private final HttpStatus status;

    // 详细的错误信息
    private final String message;

    /**
     * 构造函数
     * @param status HTTP 状态码 (例如 HttpStatus.BAD_REQUEST)
     * @param message 异常信息 (例如 "用户名已被占用!")
     */
    public BlogAPIException(HttpStatus status, String message) {
        // 调用父类的构造函数，传入消息
        super(message);
        this.status = status;
        this.message = message;
    }

    /**
     * 构造函数 (当消息与父类消息不同时使用)
     * @param status HTTP 状态码
     * @param message 异常信息
     * @param superMessage 传递给父类 RuntimeException 的消息
     */
    public BlogAPIException(HttpStatus status, String message, String superMessage) {
        super(superMessage);
        this.status = status;
        this.message = message;
    }

    // --- Getter 方法 ---

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}