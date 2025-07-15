package com.magese.ai.communication.domain.iot;

import com.magese.ai.utils.JsonUtil;
import lombok.Data;

import java.util.Map;

/**
 * Iot设备描述信息
 */
@Data
public class IotDescriptor {
    private String name;
    private String description;
    private Map<String, IotProperty> properties;
    private Map<String, IotMethod> methods;

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
