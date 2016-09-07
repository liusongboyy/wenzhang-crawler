package com.wiscom.sina.bolt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.jsoup.nodes.Element;

import com.wiscom.sina.entity.SinaWenzhang;
import com.wiscom.sina.parser.ParseWenzhangElementHtml;

import lombok.extern.log4j.Log4j;

@Log4j
public class ParseWenzhangElementsHtmlBolt extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	private ParseWenzhangElementHtml parseElementHtml;

	private Map<String, Long> map = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
		parseElementHtml = new ParseWenzhangElementHtml();
		map = new HashMap<String, Long>();
	}

	@Override
	public void execute(Tuple input) {
		String weiboPersonName = (String) input
				.getValueByField("weiboPersonName");	
		List<Element> elements = (List<Element>) input
				.getValueByField("elements");
		log.info(elements.size());
		if (elements == null || elements.size()<=0) {
			collector.ack(input);
			return;
		}else
		for (Element element : elements) {
			StringBuffer sb = new StringBuffer();
			try {
				SinaWenzhang sinaWenzhang = parseElementHtml.parseElement(
						element, false);
				sb.append(sinaWenzhang.getTitle()).append("$$$")
						.append(sinaWenzhang.getCommentCount()).append("$$$")
						.append(sinaWenzhang.getZanCount()).append("$$$")
						.append(sinaWenzhang.getReadCount());
				sb.append(sinaWenzhang.getPubTime()).append("\n");
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			collector.emit(new Values(weiboPersonName,sb.toString()));
		}
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("weiboPersonName", "wenzhangs"));
	}

}
