package com.wiscom.sina.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

	private static final String SQL_DRIVER = "com.mysql.jdbc.Driver";
	private static final String BD_URL = "jdbc:mysql://10.0.0.182:3306/sswenzhang";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "wanghui";
	
	public DBUtil(){
		try {
			Class.forName(SQL_DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println("加载mysql驱动失败");
			e.printStackTrace();
		}
	}
	
	public Connection getConnection(){
		try {
			Connection conn = DriverManager.getConnection(BD_URL, USERNAME, PASSWORD);
			return conn;
		} catch (SQLException e) {
			System.out.println("连接出错");
			e.printStackTrace();
		}
		return null;
	}
	
}
