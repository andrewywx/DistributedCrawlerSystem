import java.util.HashMap;
import java.util.List;
import java.time.LocalTime;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;

public class GoogleScholarCrawler 
{
	public ChromeDriver driver; // this is the selenium webdriver
	public String chromePath = "D://jar//chromedriver.exe"; // this is the path to the chrome driver
//	public String chromePath = "/home/rdpuser/AmazonCrawlerNode/chromdriver"; // this is the path to the chrome driver
//	public String chromePath = "/home/ubuntu/AmazonCrawlerNode/chromedriver_2_25_64bit"; // this is the path to the chrome driver
	
	public GoogleScholarCrawler()
	{
		
	}
	
	//用这个来当种子
	//https://scholar.google.com/citations?user=nQRs24kAAAAJ&hl=en	
	public HashMap<String, String> getCoauthors(String url)
	{
		HashMap<String, String> map = new HashMap<>();
		driver = (ChromeDriver)init();
		if (url != null && !url.isEmpty())
		{
			try
			{
				driver.navigate().to(url);
				WebElement coauthorsContainer = driver
						.findElementById("gsc_rsb_co")
						.findElement(By.tagName("ul"));
				List<WebElement> coauthors = coauthorsContainer
						.findElements(By.tagName("li"));
				System.out.println(coauthors.size());
				for (WebElement coauthor : coauthors)
				{
					try
					{
						WebElement link = new WebDriverWait(
								driver, 10)
								.until(ExpectedConditions
										.visibilityOf(coauthor
												.findElement(By.tagName("a"))));
						String linkURL = link
								.getAttribute("href");
						String name = coauthor
								.getText();
						map.put(name, linkURL);
					} catch (Exception e)
					{
						System.err.println("A list item with class name \'"
								+ coauthor.getAttribute("class")
								+ "\' couldn't be fetched.");
						driver.close();
					}
				}
				driver.close();
				
			} catch (Exception e)
			{
				System.err.println("Coudln't fetch the coauthors");
				driver.close();
				return null;
			}
		} else
		{
			System.err.println("Cannot fetch this url " + url);
			driver.close();
			return null;
		}
		
		//首先，把这个结果 map 以适当的结构写进 result 表格 （按照之前设定的表格格式）
		
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
	}
	
	public WebDriver init()
	{
		if (null == driver)
			createWebDriver();
		return driver;
	}
	
	private void createWebDriver()
	{
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(	CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,UnexpectedAlertBehaviour.IGNORE);
		
		HashMap<String, Object> chromePrefs = new HashMap<>();
		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePrefs);
		capabilities.setCapability(
				CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setCapability(ChromeOptions.CAPABILITY,
				options);
		File chromeDriver = new File(chromePath);
		System.setProperty("webdriver.chrome.driver",
				chromeDriver.getAbsolutePath());
		capabilities.setBrowserName("chrome");
		driver = new ChromeDriver(capabilities);
		
		
		driver.manage().timeouts().pageLoadTimeout(160, TimeUnit.SECONDS);
		driver.manage().window().maximize();
	}
		
	public void waitForPageToLoad(boolean... stopRecursion)
	{
		LocalTime timeOutWait = LocalTime.now().plusNanos(120000);
		while (timeOutWait.getNano() > LocalTime.now().getNano())
		{
			try
			{
				if (((JavascriptExecutor) driver)
						.executeScript("return document.readyState")
						.equals("complete"))
					return;
			} catch (Exception | AssertionError e)
			{
				System.err.println("Page doesn't become to ready state. Error message = "
						+ e.getMessage());
				driver.navigate().refresh();
				System.err.println("Page refreshed.");
				if ((stopRecursion.length == 0)
						|| (stopRecursion.length > 0 && !stopRecursion[0]))
					waitForPageToLoad(true);
			}
		}
	}

}
