package com.wiscom.sina.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.wiscom.sina.SinaWBWenzhangTopology;
import com.wiscom.sina.entity.SinaWenzhang;

public class StoreWenzhangDao {

	
	private Connection conn;
	private PreparedStatement stmt;
	private ResultSet rs;
	
	public void saveWenzhang(SinaWenzhang wenzhang){
		String sql = "insert into tb_wenzhang('','','','','','') values(?,?,?,?,?)";
	}
}
