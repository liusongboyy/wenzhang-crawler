package com.wiscom.sina.util;

import redis.clients.jedis.Jedis;


public class JedisUtil {

	private static final String REDIS_HOST = "10.0.0.98";
	private static final int REDIS_PORT = 6379;
	
	private static JedisUtil instance;
	private JedisUtil(){
		
	}
	
	public static JedisUtil getInstance() {
		instance = new JedisUtil();
		return instance;
	}
	
	
	public Jedis getJedis(){
		return new Jedis(REDIS_HOST, REDIS_PORT);
	}
	
	
	
}
