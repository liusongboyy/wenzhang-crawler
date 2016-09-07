package com.wiscom.sina.entity;

import java.util.Date;

import com.wiscom.utils.DateConvertor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SinaWenzhang {

	private String title; // 文章标题
	private String url; // 文章链接
	private String wenzhangsubContent; // 内容摘要
	private String pubTime; // 发布时间
	private String zanCount; // 点赞数
	private String readCount; // 阅读数
	private String commentCount; // 评论数
	private String forwordCount = "0"; // 转发数
	private long timestamp;
	
	public void setPubTime(String pubTime){
		this.pubTime=pubTime;
		timestamp = DateConvertor.convert(pubTime, new Date()).getTime();
	}
	
}
