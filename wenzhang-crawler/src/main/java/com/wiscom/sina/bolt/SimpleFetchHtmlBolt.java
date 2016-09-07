package com.wiscom.sina.bolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.jsoup.nodes.Element;

import com.wiscom.sina.parser.HtmlCrawler;


@Log4j
public class SimpleFetchHtmlBolt extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	private HtmlCrawler htmlCrawler;
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
		htmlCrawler = new HtmlCrawler();
	}

	@Override
	public void execute(Tuple input) {
		// "weiboPersonName", "url", "fetchDeadDate"
		String weiboPersonName = (String) input
				.getValueByField("weiboPersonName");
		log.info("---"+weiboPersonName);
		String url = (String) input.getValueByField("url");
		String fetchDeadDate = (String) input.getValueByField("fetchDeadDate");

		List<Element> elements = new ArrayList<Element>();
	
			elements = htmlCrawler.retAritcleElements(url, fetchDeadDate);

		collector.emit(new Values(weiboPersonName, elements));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("weiboPersonName", "elements"));
	}

}
