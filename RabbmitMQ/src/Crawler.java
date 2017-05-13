import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;

//public class Crawler {
public class Crawler extends Thread {
	public int crawlertestint = 0;
	
	ArrayList<String> urlsToCrawl = new ArrayList<String>();
	ArrayList<Crawler> threads = new ArrayList<Crawler>();
	int urlIndex = 0;
	Crawler master = null;
	
	String outputFile = "outputFile";
	String errorFile = "errorFile";
	String url = "";
	
	int successCount = 0;
	int failureCount = 0;

	static CountDownLatch latch = null;  //使用CountDownLatch来让多个线程并发执行完之后，在执行其他的操作
	
	public Crawler()
	{	
		//outputFile 如果不存在生成文件
		//errorFile 如果不存在生成文件		
		File opf = new File(outputFile);
		File ef = new File(errorFile);
		if(!opf.exists())
			try {
				opf.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(!ef.exists())
			try {
				ef.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void readURLs(String path) throws IOException//从一个本地文件去读所有要爬的URL,在这个文件里，每一行就是一个要爬的URL
	{
		FileReader reader = new FileReader(path);
		BufferedReader br = new BufferedReader(reader);
		String content = null;
		while((content = br.readLine()) != null)
			urlsToCrawl.add(content);
		br.close();
		System.out.println("urlsToCrawl.size:" + urlsToCrawl.size());
	}

	public void createThreads(int num) //生成 slave
	{
		System.out.println("创建线程的数量为:" + num);
		
		latch = new CountDownLatch(num);
		
		if(threads!=null)
			threads.clear();
		
		//生成 num 个Crawler实例
		for (int i = 0; i < num; i++)
			threads.add(new Crawler());
		// 这里要注意，因为这个 Crawler 是用反射定义的，我也不清楚这里是不是还是得用反射，试了就知道了
	}
	
	public void setMaster()               // 这个是给master用的
	{
		for (int i = 0; i < threads.size(); i++)
			threads.get(i).setMaster(this);
	}

	public void setMaster(Crawler master) // 这个是给slave用的
	{
		this.master = master;
	}
	
	public void runThreads() //这个是给master用的方法，运行所有的slave
	{
		for(int i=0;i<threads.size();i++)
			threads.get(i).start();	
	}
	
	public synchronized void increaseSuccess() {
		successCount++;
	}

	public synchronized void increaseFailure() {
		failureCount++;
	}
	
	public synchronized String getURL() //这是个 synchronized 的方法，所以slave来拿url时是同步的，不会重复爬一个
	{
		String url;
		if (urlIndex < urlsToCrawl.size()) {
			url = urlsToCrawl.get(urlIndex);
			urlIndex++;
			return url;
		}
		return null; // 爬完了
	}
	
	public void setOutputFile(String result) // 告诉slave结果存在哪
	{
		try {
			FileOutputStream fos = new FileOutputStream(outputFile, true);
			PrintStream ps = new PrintStream(fos);
			ps.println(result);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public void setErrorFile(String result) // 告诉slave失败的url存在哪
	{
		try {
			FileOutputStream fos = new FileOutputStream(errorFile, true);
			PrintStream ps = new PrintStream(fos);
			ps.println(result);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getStatus() throws InterruptedException  //在爬虫的过程中就可以查看爬虫的状况
	{
		return "Total: "+urlsToCrawl.size()+"  Success:"+successCount+"  Failure:"+failureCount;
	}
	
	public String finalgetStatus() throws InterruptedException  //在所有爬虫完成之后输出输出结果
	{
		latch.await();
		return "Total: "+urlsToCrawl.size()+"  Success:"+successCount+"  Failure:"+failureCount;
	}
	
	public void run() //这个是给slave用的方法，真正爬网页的代码
	{
		if(master==null) 
			return; //没有master,爬不了

		while(true)
		{
			url = master.getURL();	
			if(url == null) {
//				System.out.println("----------------------All urls finished!!--------------------");
				latch.countDown();
				break; //爬完了
			}			
			try
			{
				System.setProperty("webdriver.chrome.driver", "D://jar//chromedriver.exe");
				ChromeDriver driver = new ChromeDriver();
				driver.get(url);
				String response = driver.findElement(By.id("su")).getAttribute("value");
				driver.close();
				master.increaseSuccess();
				//把url和爬的结果 append 到 outputFile
				setOutputFile(response);
				System.out.println("response:" + response);
				System.out.println("Total: " + master.urlsToCrawl.size() + "  Success:" + master.successCount + "  Failure:" + master.failureCount);

//				System.setProperty("webdriver.chrome.driver", "D://jar//chromedriver.exe");
//				ChromeDriver driver = new ChromeDriver();
//			    driver.get(url);
//			    Thread.sleep(30000);
//			    String response = driver.findElement(By.className("com-google-gerrit-client-change-CommitBox_BinderImpl_GenCss_style-text")).getText();
//			    driver.close();
//			    master.increaseSuccess();
//			    System.out.println(response);
//			    System.out.println("Total: " + master.urlsToCrawl.size() + "  Success:" + master.successCount + "  Failure:" + master.failureCount);
				
			} catch (Exception e) {
				master.increaseFailure();
				// 把出错的 url 和错误信息 append 到 errorFile
				setErrorFile(url);
				setErrorFile(e.toString());
			}
		}	
		
		
	}
	
}
