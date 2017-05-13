
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
		
		HashMap<String, String> map = c.getCoauthors("https://scholar.google.com/citations?user=nQRs24kAAAAJ&hl=en"); //�������������
		System.out.println(map.entrySet());
	}	
	
	//ThreadManage ���ӽ��̸õ����������
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
			
			//��ȡ������ˣ����ڣ��������� map ���ʵ��Ľṹд�� result ��� ������֮ǰ�趨�ı���ʽ��
		
			//Ȼ�󣬰� map �е��� url д�ص� job ����У�������ȡ����������
			//�Ѿ��½�һ����� GoogleScholarCheckTable ���������� name
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) 
			{
				Map.Entry pair = (Map.Entry)it.next();
				String coauthor_name = (String)pair.getKey();
				String coauthor_url = (String)pair.getValue();
				
				//��һ�����鿴 GoogleScholarCheckTable ���Ƿ������ coauthor_name, ����У�ֱ������
					//�ڶ���������� coauthor_name д�� GoogleScholarCheckTable
					//���������� coauthor_url ����ȷ�ĸ�ʽд�� job ����У�����֮ǰ�趨�ı���ʽ��			
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
