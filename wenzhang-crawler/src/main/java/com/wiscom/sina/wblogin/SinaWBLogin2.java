package com.wiscom.sina.wblogin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.wiscom.leuconota.httpclient.HttpClientFetcher;
import com.wiscom.leuconota.httpclient.HttpPostUtils;
import com.wiscom.utils.BigIntegerRSA;

@Log4j
public class SinaWBLogin2 {

	public SinaWBLogin2() {

	}
	private static final String loginUrl = "http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.11)";
	private SinaWBCookieSave cookieSave = new SinaWBCookieSave();
	/*HashMap<String, String> account = new HashMap<String, String>() {
		{
			put("18779103903@163.com", "q33141155");
			put("wanghui2014@yeah.net", "whui2014");
		}
	};*/

	public List<org.apache.http.cookie.Cookie> retLoginedHttpClient(String username, String password)
			 {
		List<org.apache.http.cookie.Cookie> cookies = null;
		HttpPost httpPost = HttpPostUtils.createHttpPost(loginUrl, null,
				createPostInfo(username, password), null);
		BasicCookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore).build();
		HttpResponse response;
		try {
			response = httpclient.execute(httpPost);

			String entity = HttpClientFetcher.getResponseContent(response);
			if (entity.contains("retcode=0")) {
				log.info(username + " : 登陆成功！");
				String url = entity.substring(
						entity.indexOf("http://weibo.com/ajaxlogin.php?"),
						entity.indexOf("retcode=0") + 9);
				HttpGet httpGet = new HttpGet(url);
				response = httpclient.execute(httpGet);
				cookies = cookieStore.getCookies();
				cookieSave.saveResponseCookie(cookies, username);
				entity = HttpClientFetcher.getResponseContent(response);
				return cookies;
			} else {
				log.info("登录失败，请检查");
				return null;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cookies;
	}

	private Map<String,String> createPostInfo(String username, String password) {
		PreLoginInfo info = getPreLoginBean();
		long servertime = info.servertime;
		String nonce = info.nonce;

		String pwdString = servertime + "\t" + nonce + "\n" + password;
		String sp = new BigIntegerRSA().rsaCrypt(BigIntegerRSA.SINA_PUB,
				"10001", pwdString);

		Map<String, String> nvps = new HashMap<String, String>();

		nvps.put("entry", "weibo");
		nvps.put("gateway", "1");
		nvps.put("from", "");
		nvps.put("savestate", "7");
		nvps.put("useticket", "1");
		nvps.put("ssosimplelogin", "1");
		nvps.put("vsnf", "1");
		nvps.put("su", encodeUserName(username));
		nvps.put("service", "miniblog");
		nvps.put("servertime", servertime + "");
		nvps.put("nonce", nonce);
		nvps.put("pwencode", "rsa2");
		nvps.put("rsakv", info.rsakv);
		nvps.put("sp", sp);
		nvps.put("encoding", "UTF-8");
		nvps.put("prelt", "118");
		nvps.put("returntype", "META");
		nvps.put(
				"url",
				"http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack");
		nvps.put("service", "miniblog");
		return nvps;
	}

	/**
	 * 预登录
	 * 
	 * @return
	 */
	private PreLoginInfo getPreLoginBean() {
		CookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		String jsonBody = getPreLoginInfo(client);
		PreLoginInfo info = new PreLoginInfo();
		try {
			JSONObject jsonInfo = new JSONObject(jsonBody);
			info.nonce = jsonInfo.getString("nonce");
			info.pcid = jsonInfo.getString("pcid");
			info.pubkey = jsonInfo.getString("pubkey");
			info.retcode = jsonInfo.getInt("retcode");
			info.rsakv = jsonInfo.getString("rsakv");
			info.servertime = jsonInfo.getLong("servertime");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}

	private static String getPreLoginInfo(HttpClient client) {
		String preloginurl = "http://login.sina.com.cn/sso/prelogin.php?entry=weibo"
				+ "&callback=sinaSSOController.preloginCallBack"
				+ "&su=emhhbmdwZWlfMTE3JTQwc2luYS5jb20%3D"
				+ "&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.11)"
				+ "&_="
				+ getCurrentTime();
		HttpGet get = new HttpGet(preloginurl);
		try {
			HttpResponse response = client.execute(get);
			String getResp = EntityUtils.toString(response.getEntity());
			int firstLeftBracket = getResp.indexOf("(");
			int lastRightBracket = getResp.lastIndexOf(")");
			String jsonBody = getResp.substring(firstLeftBracket + 1,
					lastRightBracket);
			return jsonBody;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String getCurrentTime() {
		long servertime = new Date().getTime() / 1000;
		return String.valueOf(servertime);
	}

	/**
	 * 用户名编码
	 * 
	 * @param userName
	 * @return
	 */
	private static String encodeUserName(String userName) {
		try {
			userName = Base64.encodeBase64String(URLEncoder.encode(userName,
					"UTF-8").getBytes());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return userName;
	}
}
