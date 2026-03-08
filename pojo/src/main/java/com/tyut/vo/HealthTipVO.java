package com.tyut.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "健康宣教VO")
public class HealthTipVO {

    @ApiModelProperty(value = "宣教ID")
    private Long id;

    @ApiModelProperty(value = "宣教类型 0-政策 1-科普")
    private Integer type;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "发布者ID")
    private Long publisherId;

    @ApiModelProperty(value = "发布者姓名")
    private String publisherName;

    @ApiModelProperty(value = "发布时间")
    private LocalDateTime publishTime;

    @ApiModelProperty(value = "背景图片")
    private String backgroundImage;
}
