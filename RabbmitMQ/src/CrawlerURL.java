import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;

public class CrawlerURL {

	public CrawlerURL() {
		// System.out.println(1);
	}

	public String crawler(String url) {
		System.setProperty("webdriver.chrome.driver","D://jar//chromedriver.exe"); //windows��jar����λ��
//		System.setProperty("webdriver.chrome.driver","/home/rdpuser/AmazonCrawlerNode/chromdriver"); //linux��jar����λ��/home/rdpuser/AmazonCrawlerNode
//	    System.setProperty("webdriver.chrome.driver","/home/ubuntu/AmazonCrawlerNode/chromedriver_2_25_64bit"); //linux��jar����λ��/home/rdpuser/AmazonCrawlerNode
		
	    ChromeDriver driver = new ChromeDriver();
		driver.get(url);
		String response = driver.findElement(By.id("su")).getAttribute("value");
		driver.close();
		return response;
	}
	
	public static void main(String[] args) {
		System.setProperty("webdriver.chrome.driver","D://jar//chromedriver.exe"); //windows��jar����λ��
		ChromeDriver driver = new ChromeDriver();
		driver.get("https://www.baidu.com");
		String response = driver.findElement(By.id("su")).getAttribute("value");
		driver.close();
		System.out.println(response);
	}
}
