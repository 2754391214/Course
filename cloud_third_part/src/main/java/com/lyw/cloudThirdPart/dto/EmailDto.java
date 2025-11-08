package com.lyw.cloudThirdPart.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EmailDto {
    private String email;
    private String title;
    private String content;
}
