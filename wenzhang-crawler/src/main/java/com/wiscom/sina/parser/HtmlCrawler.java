package com.wiscom.sina.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wiscom.sina.entity.PageInfo;
import com.wiscom.sina.util.JedisUtil;
import com.wiscom.utils.CheckParam;
import com.wiscom.utils.DateConvertor;

@Log4j
public class HtmlCrawler {

	private PageInfoCrawler pageInfoCrawler;

	public HtmlCrawler() {
		pageInfoCrawler = new PageInfoCrawler();
	}
	public List<Element> retAritcleElements(String url, String fetchDeadDate)
			 {
		PageInfo pageInfo = pageInfoCrawler.fetchPageInfo(url);
		String wenzhangurl = "http://weibo.com/p/" + pageInfo.getPage_id()
				+ "/wenzhang";

		String finalWenzhangurl = pageInfo.getPage_id();
		if (finalWenzhangurl == null) {
			log.info("账号登录失效了,采集被知道了");
		}

		JedisUtil jedisUtil = JedisUtil.getInstance();
		Jedis jedis = jedisUtil.getJedis();
		jedis.select(3);
		String lastTime = null;
		if (finalWenzhangurl != null) {
			lastTime = jedis.get(finalWenzhangurl);
		}
		List<Element> results = new ArrayList<Element>();
		boolean timeflag = false;
		Long lastUpdateWenzhangTime = null;
		if (lastTime != null) {
			do {
				String entity = pageInfoCrawler.fetchHtml(wenzhangurl);
				String data = pageInfoCrawler.getNormalHTMLData(entity);
				Document document = Jsoup.parse(data);
				document.setBaseUri("http://weibo.com");
				Elements elements = document
						.select("li[node-type=article_list_item]");
				long eleTime = 0;
				for (Element element : elements) {
					String time = element.select("div.subinfo_box span").text();
					String t = time.replaceAll(" ([年|月|日]) ", "$1").replace(
							"日", "日 ");
					element.select("div.subinfo_box span").first().text(t);
					Date wenzhangDate = DateConvertor.convert(t, new Date());
					eleTime = wenzhangDate.getTime();
					if (eleTime <= Long.parseLong(lastTime)) {
						timeflag = true;
						break;
					} else {
						results.add(element);
						jedis.set(pageInfo.getPage_id(),
								String.valueOf(eleTime));
					}
				}
				Element last = document.select("a.next").last();
				if (last == null)
					wenzhangurl = "";
				else
					wenzhangurl = last.absUrl("href");
			} while (CheckParam.checkString(wenzhangurl) && !timeflag);
		} else {
			do {
				String entity = pageInfoCrawler.fetchHtml(wenzhangurl);
				String data = pageInfoCrawler.getNormalHTMLData(entity);
				Document document = Jsoup.parse(data);
				document.setBaseUri("http://weibo.com");
				Elements elements = document
						.select("li[node-type=article_list_item]");
				for (Element element : elements) {
					String time = element.select("div.subinfo_box span").text();
					String t = time.replaceAll(" ([年|月|日]) ", "$1").replace(
							"日", "日 ");
					element.select("div.subinfo_box span").first().text(t);
					Date deadline = DateConvertor.convert(fetchDeadDate,
							new Date());
					Date wenzhangDate = DateConvertor.convert(t, new Date());
					if (lastUpdateWenzhangTime == null) {
						lastUpdateWenzhangTime = wenzhangDate.getTime();
					} else if (lastUpdateWenzhangTime < wenzhangDate.getTime()) {
						lastUpdateWenzhangTime = wenzhangDate.getTime();
					}
					if (deadline != null) {
						if (lastUpdateWenzhangTime < deadline.getTime()) {
							lastUpdateWenzhangTime = null;
						}
						if (wenzhangDate.before(deadline)) {
							timeflag = true;
							break;
						} else {
							results.add(element);
						}
					} else {
						results.add(element);
					}
				}
				Element last = document.select("a.next").last();
				if (last == null)
					wenzhangurl = "";
				else
					wenzhangurl = last.absUrl("href");
			} while (CheckParam.checkString(wenzhangurl) && !timeflag);
		}
		if (lastUpdateWenzhangTime != null)
			jedis.set(pageInfo.getPage_id(),
					String.valueOf(lastUpdateWenzhangTime));
		jedis.disconnect();

		return results;
	}

}
