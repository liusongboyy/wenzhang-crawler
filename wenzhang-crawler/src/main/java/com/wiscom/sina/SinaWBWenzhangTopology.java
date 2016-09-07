package com.wiscom.sina;

import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import com.wiscom.sina.bolt.ParseWenzhangElementsHtmlBolt;
import com.wiscom.sina.bolt.PersistenceWenzhangBolt;
import com.wiscom.sina.bolt.SimpleFetchHtmlBolt;
import com.wiscom.sina.spout.SinaWBURLSpout;
import com.wiscom.sina.wblogin.SinaWBLogin2;

public class SinaWBWenzhangTopology {

	private static final String topo_name = "sina_wenzhang_topo";
	private static final String spout_name = "scheduled_url_spout";
	private static final String simpl_fetch_html_bolt = "simpl_fetch_html_bolt";
	private static final String parse_wenzhang_elements_bolt = "parse_wenzhang_elements_bolt";
	private static final String persistence_bolt = "persistence_bolt";

	public static void main(String[] args) {
		String[] urls = {
				"chemitoutiao@@@http://weibo.com/u/5699800464?refer_flag=1001030201_@@@2016-06-01",
				"mingchezhi@@@http://weibo.com/caranddriver?refer_flag=1001030201_@@@2016-06-01",
				"qicheyefengkuang@@@http://weibo.com/u/1794993692?refer_flag=1001030201_@@@2016-06-01",
				"qichedianshang@@@http://weibo.com/gouchekuanghuanjie?refer_flag=1001030201_@@@2016-06-01" };
		SinaWBLogin2 login2 = new SinaWBLogin2();
		 login2.retLoginedHttpClient("18779103903@163.com",
				"q33141155");
		 login2.retLoginedHttpClient("17702114267",
					"q33141155");
		SinaWBURLSpout spout = new SinaWBURLSpout(urls);
		SimpleFetchHtmlBolt simpleFetchHtmlBolt = new SimpleFetchHtmlBolt();

		ParseWenzhangElementsHtmlBolt elementsHtmlBolt = new ParseWenzhangElementsHtmlBolt();
		PersistenceWenzhangBolt persisBolt = new PersistenceWenzhangBolt();

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout(spout_name, spout);
		builder.setBolt(simpl_fetch_html_bolt, simpleFetchHtmlBolt)
				.shuffleGrouping(spout_name);
		
		builder.setBolt(parse_wenzhang_elements_bolt, elementsHtmlBolt)
				.fieldsGrouping(simpl_fetch_html_bolt,
						new Fields("weiboPersonName"));

		builder.setBolt(persistence_bolt, persisBolt).fieldsGrouping(
				parse_wenzhang_elements_bolt, new Fields("weiboPersonName"));

		Config config = new Config();
		LocalCluster cluster = new LocalCluster();  
		if(args.length == 0)
			cluster.submitTopology(topo_name, config, builder.createTopology());
		else
			try {
				StormSubmitter.submitTopology(args[0], config, builder.createTopology());
			} catch (AlreadyAliveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidTopologyException e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AuthorizationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

}
