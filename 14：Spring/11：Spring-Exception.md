### Spring Exception

------

[TOC]

##### 01：统一异常处理

1. 统一在 Controller 层处理异常、参数校验；
1. service 层的异常没有被捕获，继续向上抛，到 Controller 被处理；

##### 02：示例

```java
 /**
 * 统一异常处理
 *
 * @author kangkang.li@qunar.com
 * @date 2020-09-11 11:02
 */
@Slf4j
@ControllerAdvice
public class ExceptionResolver {
  	/**
  	 * 所有 Controller 抛出的异常都会进行拦截处理
  	 */
    @ExceptionHandler(value = BizException.class)  
    @ResponseBody
    public ApiResult doBizException(Exception e) {
        log.error("unknow exception:", e);
        return ApiResult.error(CodeMessage.SYSTEM_ERROR, "未知问题");
    }
  
    @ExceptionHandler(RpcException.class)
    @ResponseBody
    public ApiResult doDubboException(RpcException e) {
        log.error("rpc remote call exception:", e);
        QMonitor.recordOne(MonitorConstant.RPC_CALL_EXCEPTION);
        return ApiResult.error(CodeMessage.SYSTEM_ERROR, "第三方服务调用问题，稍后再试");
    }

  	/**
  	 * 自定义异常处理器
  	 */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ApiResult doParamException(RpcException e) {
        log.error("Illegal arg exception:{}", e.getMessage());
        return ApiResult.error(CodeMessage.SYSTEM_ERROR, "参数异常");
    }
  
}
```

##### 03：返回状态码

```java
/**
 * 统一返回状态码
 *
 * @author kangkang.li@qunar.com
 * @date 2020-09-11 11:02
 */
@Data
public class ApiResult<T> implements Serializable {
    private static final long serialVersionUID = -6248840793491491648L;
    /**
     * 错误码
     */
    private Integer status;

    /**
     * 前端弹出消息
     */
    private String message;

    /**
     * 后端具体传递给前端的消息
     */
    private T data;

    public static <T> ApiResult builderSuccess(T data, String message) {
        ApiResult result = new ApiResult();
        result.status = CodeMessage.OK;
        result.data = data;
        result.message = message;
        return result;
    }

    public static <T> ApiResult builderSuccess(T data) {
        return builderSuccess(data, null);
    }

    public static ApiResult builderSuccess() {
        return builderSuccess(null, null);
    }

    public static ApiResult error(Integer status, String message) {
        ApiResult result = new ApiResult();
        result.status = status;
        result.message = message;
        return result;
    }

    public static ApiResult error(Integer status) {
        return error(status, null);
    }

    public static ApiResult error(String message) {
        return error(CodeMessage.SYSTEM_ERROR, message);
    }

    public static ApiResult error() {
        return error(CodeMessage.SYSTEM_ERROR, null);
    }
}
```