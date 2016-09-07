package com.wiscom.sina.spout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;

public class SinaWBURLSpout extends BaseRichSpout {

	private static final long serialVersionUID = 1L;

	private SpoutOutputCollector collector;
	String[] urls = null;

	public SinaWBURLSpout(String... urls) {
		this.urls = urls;
	}

	@Override
	public void open(@SuppressWarnings("rawtypes") Map conf,
			TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void nextTuple() {
		for (String u : urls) {
			String[] splits = u.split("@@@");
			String weiboPersonName = splits[0];
			String url = splits[1];
			String fetchDeadDate = splits[2];
			List<Object> tuple = new ArrayList<Object>();
			tuple.add(weiboPersonName);
			tuple.add(url);
			tuple.add(fetchDeadDate);
			collector.emit(tuple);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("weiboPersonName", "url", "fetchDeadDate"));
	}

}
