package com.lyw.cloudChoose.chain;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证上下文，用于在处理器之间传递数据
 */
@Data
public class ValidationContext {
    private Map<String, Object> attributes = new HashMap<>();

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
}