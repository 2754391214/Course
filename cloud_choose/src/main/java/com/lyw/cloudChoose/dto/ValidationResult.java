package com.lyw.cloudChoose.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证结果
 */
@Data
public class ValidationResult {
    private boolean valid;
    private List<String> rejectionReasons;
    private String handlerName;

    public ValidationResult() {
        this.valid = true;
        this.rejectionReasons = new ArrayList<>();
    }

    public void addRejectionReason(String reason) {
        this.valid = false;
        this.rejectionReasons.add(reason);
    }

    public static ValidationResult success() {
        return new ValidationResult();
    }

    public static ValidationResult failed(String reason) {
        ValidationResult result = new ValidationResult();
        result.addRejectionReason(reason);
        return result;
    }
}