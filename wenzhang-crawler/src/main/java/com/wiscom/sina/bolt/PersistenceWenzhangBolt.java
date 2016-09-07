package com.wiscom.sina.bolt;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import com.wiscom.utils.FileUtils;

public class PersistenceWenzhangBolt extends BaseRichBolt{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		
//		map = new HashMap<String,String>();
	}

	@Override
	public void execute(Tuple input) {
		String personName = (String) input.getValueByField("weiboPersonName");
		String wenzhangs =  (String) input.getValueByField("wenzhangs");
		FileUtils.writeTofile("/home/storm/"+personName+".txt", wenzhangs);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		
	}

}
