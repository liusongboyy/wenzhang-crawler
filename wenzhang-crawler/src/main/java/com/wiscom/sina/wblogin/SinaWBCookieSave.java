package com.wiscom.sina.wblogin;

import java.util.List;

import org.apache.http.cookie.Cookie;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wiscom.sina.util.JedisUtil;

public class SinaWBCookieSave {
	
	/**
	 * 
	 * @param response
	 * @param accountID 新浪微博账号
	 */
	Gson gson = new GsonBuilder().serializeNulls().create();
	public List<Cookie> saveResponseCookie(List<Cookie> cookies,String accountID){
		JedisUtil jedisUtil = JedisUtil.getInstance();
		Jedis jedis = jedisUtil.getJedis();
		jedis.select(2);
		String cookiesStr = gson.toJson(cookies);
		jedis.set(accountID, cookiesStr);
		jedis.disconnect();
		return cookies;
	}

}
