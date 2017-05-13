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
	
	//�������������
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
		
		//���ȣ��������� map ���ʵ��Ľṹд�� result ��� ������֮ǰ�趨�ı���ʽ��
		
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
