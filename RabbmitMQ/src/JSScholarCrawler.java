
import java.io.IOException;
import java.util.HashMap;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JSScholarCrawler {

	public static void main(String[] args) {
		JSScholarCrawler c = new JSScholarCrawler();
		
		HashMap<String, String> map = c.getCoauthors("https://scholar.google.com/citations?user=nQRs24kAAAAJ&hl=en"); //用这个来当种子
		System.out.println(map.entrySet());
	}	
	
	//ThreadManage 的子进程该调用这个方法
	public HashMap<String, String> getCoauthors(String url) {
		
		Document doc = fetch(url);		
		
		HashMap<String, String> map = new HashMap<>();
		try 
		{
			Element coauthorsContainer = doc.getElementById("gsc_rsb_co")
					.getElementsByTag("ul").first();
			Elements coauthors = coauthorsContainer.getElementsByTag("li");

			for (Element coauthor : coauthors) {
				try {
					if (coauthor.attr("class").equalsIgnoreCase("gsc_rsb_fade")) {
						continue;
					}
					Element link = coauthor.getElementsByTag("a").first();
					String linkURL = link.absUrl("href");
					String name = coauthor.text();
					map.put(name, linkURL);
				} catch (Exception e) {
					System.err.println("A list item with class name \'"
							+ coauthor.attr("class")
							+ "\' couldn't be fetched.");
				}
			}
			System.out.println(coauthors.size());
			
			//爬取到结果了，现在，把这个结果 map 以适当的结构写进 result 表格 （按照之前设定的表格格式）
		
			//然后，把 map 中的新 url 写回到 job 表格中，继续爬取，方法如下
			//已经新建一个表格 GoogleScholarCheckTable 它的主键是 name
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) 
			{
				Map.Entry pair = (Map.Entry)it.next();
				String coauthor_name = (String)pair.getKey();
				String coauthor_url = (String)pair.getValue();
				
				//第一步，查看 GoogleScholarCheckTable 中是否有这个 coauthor_name, 如果有，直接跳过
					//第二步，把这个 coauthor_name 写进 GoogleScholarCheckTable
					//第三步，把 coauthor_url 以正确的格式写入 job 表格中（按照之前设定的表格格式）			
			}	
			
			
			
			return map;
		} catch (Exception e) {
			System.err.println("Coudln't fetch the coauthors");
			e.printStackTrace();
			return null;
		}
	}
	
	public Document fetch(String link) {

		if (link == null || link.isEmpty()) {
			System.err.println("Cannot fetch this link");
			return null;
		} else {
			try {
				Document doc = Jsoup
						.connect(link)
						.timeout(60 * 1000)
						.referrer("http://www.google.com")
						.validateTLSCertificates(false)
						.userAgent(
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36")
						.get();
				return doc;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

}
