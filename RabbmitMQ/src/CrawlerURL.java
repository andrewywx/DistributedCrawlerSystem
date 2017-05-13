import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;

public class CrawlerURL {

	public CrawlerURL() {
		// System.out.println(1);
	}

	public String crawler(String url) {
		System.setProperty("webdriver.chrome.driver","D://jar//chromedriver.exe"); //windows中jar包的位置
//		System.setProperty("webdriver.chrome.driver","/home/rdpuser/AmazonCrawlerNode/chromdriver"); //linux中jar包的位置/home/rdpuser/AmazonCrawlerNode
//	    System.setProperty("webdriver.chrome.driver","/home/ubuntu/AmazonCrawlerNode/chromedriver_2_25_64bit"); //linux中jar包的位置/home/rdpuser/AmazonCrawlerNode
		
	    ChromeDriver driver = new ChromeDriver();
		driver.get(url);
		String response = driver.findElement(By.id("su")).getAttribute("value");
		driver.close();
		return response;
	}
	
	public static void main(String[] args) {
		System.setProperty("webdriver.chrome.driver","D://jar//chromedriver.exe"); //windows中jar包的位置
		ChromeDriver driver = new ChromeDriver();
		driver.get("https://www.baidu.com");
		String response = driver.findElement(By.id("su")).getAttribute("value");
		driver.close();
		System.out.println(response);
	}
}
