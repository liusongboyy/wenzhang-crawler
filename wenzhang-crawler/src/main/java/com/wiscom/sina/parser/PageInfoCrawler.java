package com.wiscom.sina.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wiscom.sina.entity.PageInfo;
import com.wiscom.sina.util.JedisUtil;
import com.wiscom.sina.wblogin.SinaWBLogin2;
import com.wiscom.utils.CheckParam;
import com.wiscom.utils.StringUtil;

public class PageInfoCrawler {

	private int randomNum = 0;
	public PageInfoCrawler() {

	}

	public PageInfo fetchPageInfo(String url) {
		String entity = fetchHtml(url);
		Map<String, String> map = fetchConfigMap(entity);
		PageInfo pageInfo = new PageInfo(map.get("domain"), map.get("page_id"),
				map.get("location"), map.get("oid"), map.get("onick"));
		if (pageInfo.getPage_id() == null) {
			// 登陆账号可能不能用了 重新登陆试下
			reLogin(getRandomNum());
			fetchPageInfo(url);
		}
		return pageInfo;
	}
	/**
	 * 这里只是简单的处理要改
	 * @param randomNum
	 */
	public void reLogin(int randomNum){
		SinaWBLogin2 login2 = new SinaWBLogin2();
		login2.retLoginedHttpClient(sinaWBAccounts[randomNum], "q33141155");
	}
	
	public String getNormalHTMLData(String entity) {
		StringBuffer buffer = new StringBuffer("");
		Pattern pattern = Pattern.compile("\"html\":\"(.+?)\"\\}\\)");
		Pattern pattern2 = Pattern.compile("\"data\":\"(.+?)\"\\}");
		Matcher matcher = pattern.matcher(entity);
		Matcher matcher2 = pattern2.matcher(entity);
		while (matcher.find()) {
			String tmp = matcher.group().substring(8,
					matcher.group().length() - 3);
			buffer.append(tmp);
		}
		if (matcher2.find()) {
			String tmp = matcher2.group().substring(8,
					matcher2.group().length() - 2);
			buffer.append(tmp);
		}
		return buffer.toString();
	}

	private Map<String, String> fetchConfigMap(String html) {

		if (!html.contains("var $CONFIG = {};"))
			return new HashMap<String, String>();

		Pattern pattern = Pattern.compile("CONFIG = \\{\\}(.+?)</script>",
				Pattern.DOTALL);
		Matcher matcher = pattern.matcher(html);

		Map<String, String> map = new HashMap<String, String>();
		if (matcher.find()) {
			String temp = matcher.group();
			pattern = Pattern.compile("\\$CONFIG\\[(.*?)]=(.*?);");
			matcher = pattern.matcher(temp);
			while (matcher.find()) {
				String config = matcher.group().replaceAll(
						"\\$CONFIG|\\[|\\]|'|;", "");

				String[] arr = config.split("=");

				if (arr.length == 2 && CheckParam.checkString(arr[0], arr[1]))
					map.put(arr[0], arr[1]);

			}
		}
		return map;

	}

	private static final String[] sinaWBAccounts = { "18779103903@163.com","17702114267"
		/*"wnfe46301417to@163.com",
		"ug42859575to@163.com",
		"w37492667shikey@163.com",
		"t65588206beisu@163.com",
		"rau11636131fei@163.com",
		"o23985438yuanla@163.com"*/
};

	public String fetchHtml(String url) {

		String html = "";
		Gson gson = new GsonBuilder().serializeNulls().create();
		String account = randomAccount();
		
		CookieStore cookieStore = new BasicCookieStore();
		JedisUtil jedisUtil = JedisUtil.getInstance();
		Jedis jedis = jedisUtil.getJedis();
		jedis.select(2);
		String cookiesStr = jedis.get(account);
		List<Cookie> cookies = gson.fromJson(cookiesStr,
				new TypeToken<List<BasicClientCookie>>() {
				}.getType());
		for (Cookie cookie : cookies) {
			cookieStore.addCookie(cookie);
		}

		HttpClient client = HttpClients.custom()
				.setDefaultCookieStore(cookieStore).build();
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri(url)
				.addHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36")
				.build();

		try {
			HttpResponse response = client.execute(request);
			String entity = EntityUtils.toString(response.getEntity(), "utf-8");
			entity = StringUtil.UnicodeToGBK(entity);
			html = entity;
		} catch (Exception e) {
			e.printStackTrace();
			
		}finally{
			jedis.disconnect();
		}
		return html;
	}

	private String randomAccount() {
		 randomNum = (int) (Math.random() * sinaWBAccounts.length);
		return sinaWBAccounts[randomNum];
		
	}
	
	public int getRandomNum(){
		return randomNum;
	}

}
