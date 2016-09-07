package com.wiscom.sina.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageInfo implements Serializable {
	private static final long serialVersionUID = -1869424625493126566L;
	
	private String domain;
	private String page_id;
	private String location;
	private String oid;
	private String onick;
}