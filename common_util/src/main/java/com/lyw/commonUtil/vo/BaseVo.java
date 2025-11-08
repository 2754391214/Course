package com.lyw.commonUtil.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class BaseVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Pattern linePattern = Pattern.compile("\\B(\\p{Upper})(\\p{Lower}*)");

    @TableField(exist = false)
    protected String orderBy = null;
    @TableField(exist = false)
    protected String order = null;
    @TableField(exist = false)
    private String userGroup;

    @TableField(value = "crd")
    private String crd;

    @TableField(value = "cru")
    private String cru;

    @TableField(value = "luu")
    private String luu;

    @TableField(value = "lud")
    private String lud;

    @TableField(exist = false)
    private int pageSize;

    @TableField(exist = false)
    private int pageNo;

    @TableField(exist = false)
    private Map<String, List<String>> filters;

    //搜索条件
    @TableField(exist = false)
    private String searchStr;

    public void setOrder(String order) {
        if (order != null) {
            Matcher matcher = linePattern.matcher(order);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
            }
            matcher.appendTail(sb);
            this.order = sb.toString();
        }
    }

    /**
     * cru、luu 赋值
     *
     * @param u user_id or user_code
     */
    public void setCruAndLuu(String u) {
        this.cru = u;
        this.luu = u;
    }

    /**
     * crd、lud 赋值
     *
     * @param u user_id or user_code
     */
    public void setCrdAndLud(String u) {
        this.crd = u;
        this.lud = u;
    }
}
