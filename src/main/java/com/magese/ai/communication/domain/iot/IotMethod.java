package com.magese.ai.communication.domain.iot;

import com.magese.ai.utils.JsonUtil;
import lombok.Data;

import java.util.Map;

/**
 * function_call的方法定义
 */
@Data
public class IotMethod {
    /**
     * 方法描述
     */
    private String description;
    /**
     * 方法参数
     */
    private Map<String, IotMethodParameter> parameters;

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
