package com.wiscom.sina.parser;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.wiscom.sina.entity.SinaWenzhang;

public class ParseWenzhangElementHtml {

	private PageInfoCrawler pageInfoCrawler;

	public ParseWenzhangElementHtml() {
		pageInfoCrawler = new PageInfoCrawler();
	}

	public SinaWenzhang parseElement(Element element, boolean isCrawlReadCount)
			throws ClientProtocolException, IOException {
		SinaWenzhang sinaWenzhang = new SinaWenzhang();
		Element element2 = element.select(".info_box").first();
		Element element3 = element2.select("div.title a").first();
		String titleUrl = element3.absUrl("href");
		String title = element3.text();
		sinaWenzhang.setTitle(title);
		sinaWenzhang.setUrl(titleUrl);
		if (isCrawlReadCount) {
			sinaWenzhangInfo(sinaWenzhang, titleUrl);
		} else {
			sinaWenzhang.setReadCount("0");
		}
		Element element4 = element2.select(".text a").first();
		if (element4 != null) {
			String subContent = element4.text();
			sinaWenzhang.setWenzhangsubContent(subContent);
		}
		Element element5 = element2.select(".subinfo_box .subinfo").first();
		String pubTime = element5.text();
		sinaWenzhang.setPubTime(pubTime);
		Element element6 = element2.select(".WB_praishare a").first()
				.select("span").first();
		if (element6 != null) {
			String zanCount = element6.text();
			sinaWenzhang.setZanCount(zanCount);
		}
		Element element7 = element2.select(".WB_praishare a").last();
		if (element7 != null) {
			String commentCountStr = element7.text();
			if (commentCountStr.contains("评论(")) {
				commentCountStr = commentCountStr.replaceAll("评论\\(", "");
				commentCountStr = commentCountStr.replace("\\)", "");
				sinaWenzhang.setCommentCount(commentCountStr);
			}
		}
		// log.info(sinaWenzhang);
		return sinaWenzhang;

	}

	private void sinaWenzhangInfo(SinaWenzhang sinaWenzhang, String titleUrl)
			throws ClientProtocolException, IOException {
		String html = pageInfoCrawler.fetchHtml(titleUrl);
		String viewNums = "0";
		Document doc = Jsoup.parse(html);
		if (!titleUrl.contains("show?id")) {
			String data = pageInfoCrawler.getNormalHTMLData(html);
			doc = Jsoup.parse(data);
		}
		Element element = doc.select("span.num").first();
		if (element != null) {
			viewNums = element.text().replaceAll("阅读", "").replaceAll("数：", "")
					.trim();
		}
		Element element2 = doc.select("span.pos").first();
		if (element2 != null) {
			String text = element2.text();
			if (text.contains("转发")) {
				text = text.replace("转发", "").trim();
				sinaWenzhang.setForwordCount(text);
			}
		}
		sinaWenzhang.setReadCount(viewNums);
	}

}
