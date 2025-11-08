package com.lyw.commonUtil.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
public class BaseDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String cru;
    private String crd;
    private String luu;
    private String lud;
    private int pageSize;
    private int pageNo;
    //接收前端搜索条件
    private String searchStr;
}
