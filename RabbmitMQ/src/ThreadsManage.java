import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;


public class ThreadsManage extends Thread {

	public int crawlertestint = 0;
	DynamoAPI dynamo; //操作DynamoDB的实例
	
	boolean status = false; //通过这个状态位，来在线程的run方法中进行判断，如果是maseter就做一分钟爬取一次url列表的操作，如果是slave就进行爬虫
	ArrayList<String> urlsToCrawl = new ArrayList<String>(); //存放从DynamoDb中读取到的带爬取url
//	ArrayList<String> urlsToCrawl = (ArrayList<String>) Collections.synchronizedList(new ArrayList<String>()); //存放从DynamoDb中读取到的带爬取url
	ArrayList<ThreadsManage> threads = new ArrayList<ThreadsManage>();

	int urlIndex = 0;
	ThreadsManage master = null;
	String nodeID = null; //由Client通过SET_inner操作传入nodeID
	String urlstable = null; //由Client通过SET_inner操作传入待爬url的表明
	String resulttable = null; //由Client通过SET_inner操作传入爬虫结果要存的表明
	
	String outputFile = "outputFile";
	String errorFile = "errorFile";
	String url = "";
	String uuid = "";

	int successCount = 0;
	int failureCount = 0;

	static CountDownLatch latch = null;  //使用CountDownLatch来让多个线程并发执行完之后，在执行其他的操作
	
	public ThreadsManage() {
		// outputFile 如果不存在生成文件
		// errorFile 如果不存在生成文件
		File opf = new File(outputFile);
		File ef = new File(errorFile);
		if (!opf.exists())
			try {
				opf.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (!ef.exists())
			try {
				ef.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	// 从一个本地文件去读所有要爬的URL,在这个文件里，每一行就是一个要爬的URL
	public void readURLs(String path) throws IOException
	{
		FileReader reader = new FileReader(path);
		BufferedReader br = new BufferedReader(reader);
		String content = null;
		while ((content = br.readLine()) != null)
			urlsToCrawl.add(content);
		br.close();
		System.out.println("urlsToCrawl.size:" + urlsToCrawl.size());
	}

	//从Dynamo数据库中根据指定的条件获取所有的待爬url，并存入ArrayList中
	public synchronized void readURLsFromDynamo() throws IOException{
		try{
			dynamo = new DynamoAPI();
			dynamo.connectToDB();
			dynamo.getItemsToArrayListByRequest2(urlsToCrawl, urlstable, nodeID);
		}
		catch(Exception e){
			ThreadsManage.logStackTrace(e);
		}

		System.out.println("该节点所需爬取的url数量为:" + urlsToCrawl.size());
		System.out.println("--------------------------------------------");
		for (String url : urlsToCrawl) {
			System.out.println(url);
		}
		System.out.println("--------------------------------------------");
	}
	
	public void createThreads(int num) // 生成 slave
	{
		System.out.println("创建线程的数量为:" + num);

		latch = new CountDownLatch(num);

		if (threads != null)
			threads.clear();

		// 生成 num 个Crawler实例
		for (int i = 0; i < num; i++)
			threads.add(new ThreadsManage());
		// 这里要注意，因为这个 ThreadsManager 是用反射定义的，我也不清楚这里是不是还是得用反射，试了就知道了
		
		System.out.println("threads.size():" + threads.size());
		for (int i = 0; i < threads.size(); i++){
			threads.get(i).setMaster(this);
			threads.get(i).status = true;
		}
	}
	
//	public void setMaster() // 这个是给master用的
//	{
//		System.out.println("threads.size():" + threads.size());
//		for (int i = 0; i < threads.size(); i++)
//			threads.get(i).setMaster(this);
//	}

	public void setMaster(ThreadsManage master) // 这个是给slave用的
	{
		this.master = master;
	}
	
	public void runMasterThread(){
		this.start();
	}
	
	public void runThreads() //这个是给master用的方法，运行所有的slave
	{
		for(int i=0;i<threads.size();i++)
			threads.get(i).start();	
	}
	
	public void stopThreads(){ //这个是给master用的方法，停止slave的Threads
		for(int i=0;i<threads.size();i++)
			threads.get(i).stop();	
	}
	
	public synchronized void increaseSuccess() {
		successCount++;
	}

	public synchronized void increaseFailure() {
		failureCount++;
	}
	
	public synchronized String getURL() // 这是个synchronized的方法，所以slave来拿url时是同步的，不会重复爬一个
	{
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
			ThreadsManage.logStackTrace(e);
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
			ThreadsManage.logStackTrace(e);
		}
	}
	
	public String getStatus() throws InterruptedException  //在爬虫的过程中就可以查看爬虫的状况
	{
		return "Total: " + urlsToCrawl.size() + " Success:" + successCount + " Failure:" + failureCount;
	}
	
	public String finalgetStatus() throws InterruptedException  //在所有爬虫完成之后输出输出结果
	{
		latch.await();
		return "Total: " + urlsToCrawl.size() + " Success:" + successCount + " Failure:" + failureCount;
	}
	
	public void run() // 这个是给slave用的方法，真正爬网页的代码
	{
		if(status == true){
			if (master == null)
				return; // 没有master,爬不了
			
			String response = "No response";
			String error = "No error";
			CrawlerURL crawlerurl = null;
			GoogleScholarCrawler googlecrawler = null;
			JSScholarCrawler jsscholarcrawler = null;
			dynamo = new DynamoAPI();
			dynamo.connectToDB();

			while(true)
			{
				String[] getURLAndUUID = master.getURL().split(" ");
				url = getURLAndUUID[2];
				uuid = getURLAndUUID[0];
				
				if (url == null) {
					latch.countDown();
					break; // 爬完了
				}			
				try
				{
//					System.out.println(currentThread().getName());
//					crawlerurl = new CrawlerURL();
//					response = crawlerurl.crawler(url);
//					master.increaseSuccess();
//					
//					//将爬虫的结果写入DynamoDB数据库中
//					dynamo.insertItemsIntoResultTable2(master.resulttable, master.nodeID, uuid, response, error);
//					//如果这条url成功获取，将status从false修改为complete
//					dynamo.updateItem(master.urlstable, master.nodeID, uuid, "complete");
//					
//					System.out.println("response:" + response);
//					System.out.println("Total: " + master.urlsToCrawl.size() + "  Success:" + master.successCount + "  Failure:" + master.failureCount);
//					
//					// 把url和爬的结果 append 到 outputFile
//					setOutputFile(response);
//					response = "No response";
//					
//					Thread.sleep(10000);
					
					
					HashMap<String, String> map = new HashMap<>();
					System.out.println(currentThread().getName());
//					googlecrawler = new GoogleScholarCrawler();
//					googlecrawler.init();
//					googlecrawler.waitForPageToLoad();
//					map = googlecrawler.getCoauthors(url);
					jsscholarcrawler = new JSScholarCrawler();
					map = jsscholarcrawler.getCoauthors(url);
					master.increaseSuccess();
					
					for (Map.Entry<String, String> entry : map.entrySet()) {
//						System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
						
						//如果GoogleScholarCheckTable表中没有这个author的名字的话，就将新的待爬取的url存入URLtable2中
						if(dynamo.checkCoauthorName((String)entry.getKey()) == false){
							dynamo.insertOneItemIntoUrlTable2(master.urlstable, master.nodeID, (String)entry.getValue(), "false"); 
							dynamo.insertOneItemIntoGoogleScholarCheckTable((String)entry.getKey());
						}
						//将爬虫的结果写入DynamoDB数据库中
						response = entry.getKey() + "\t" + entry.getValue();
						dynamo.insertItemsIntoResultTable2(master.resulttable, master.nodeID, UUID.randomUUID().toString(), response, error);
						//如果这条url成功获取，将status从false修改为complete
						dynamo.updateItem(master.urlstable, master.nodeID, uuid, "complete");
						
						System.out.println("response:" + response);
						System.out.println("Total: " + master.urlsToCrawl.size() + "  Success:" + master.successCount + "  Failure:" + master.failureCount);
						
						// 把url和爬的结果 append 到 outputFile
						setOutputFile(response);
						response = "No response";	
					}
				} 
				catch (Exception e) {
					master.increaseFailure();
					error = e.toString();
					ThreadsManage.logStackTrace(e);
					
					// 将爬虫的错误信息写入DynamoDB数据库中
					dynamo.insertItemsIntoResultTable2(master.resulttable, master.nodeID, UUID.randomUUID().toString(), response, error);
					//如果这条url成功获取，将status从false修改为error
					dynamo.updateItem(master.urlstable, master.nodeID, uuid, "error");
					System.out.println(master.urlstable);
					System.out.println(master.nodeID);
					System.out.println(uuid);

					error = "No error";
					
					// 把出错的 url 和错误信息 append 到 errorFile
					setErrorFile(new Date() + "--" + url + "--" + e.toString());
					
				}			
			}
		}else if(status == false){
			dynamo = new DynamoAPI();
			dynamo.connectToDB();

			while(true)
			{
				try {
					System.out.println("-------------------------------------------------");

					Thread.sleep(30000); //每过一分钟重新读取一次新加入的url，并将其加入ArrayList中
					readURLsFromDynamo();
					System.out.println("Total url sizes: " + urlsToCrawl.size());

					System.out.println("-------------------------------------------------");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ThreadsManage.logStackTrace(e);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ThreadsManage.logStackTrace(e);
				}
				
			}
		}
		

	}

	// 所有的exception该写入本地的一个文件，以当天的日期命名，每天的错误写在一个文件里，标明错误发生时间，和收到的指令是什么，同时存入一个ArrayList或类似结构中，标明时间，方便远程读取
	public static void logStackTrace(Exception e) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 以当前日期作为当日的log文件的文件名
		String logFileName = "ThreadManage-" + sdf.format(new Date()) + ".log";

		StackTraceElement[] ste = e.getStackTrace();
		int stelength = ste.length;
		String x = null;
		for (int i = 0; i < stelength; i++) {
			x = "   " + ste[i].toString();
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(logFileName, true));
			bw.write(new Date() + "--" + e.toString());
			bw.write(x.toString());
			bw.newLine();
			bw.close();
			// throw new IOException();
		} catch (IOException ioe) {
			ioe.printStackTrace(); // System.out.println (e);
			System.out.println("failed to write " + x + " into file:" + logFileName);// print out the runtime variables
			System.exit(0); // terminate the program or Runtime.getRuntime().exit(0);
		}
	}

	// 返回本地缓存的Exception trace有多少，这里的Exception trace指的整个错误的内容，包括时间，收到的指令，和完整的stackTrace
	public static int getNumberOfExceptions() throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 以当前日期作为当日的log文件的文件名
		String logFileName = "ThreadManage-" + sdf.format(new Date()) + ".log";

		int exceptiontracenumber = 0;
		String str;
		FileReader reader = new FileReader(logFileName);
		BufferedReader br = new BufferedReader(reader);
		while ((str = br.readLine()) != null) {
			exceptiontracenumber++;
		}
		return exceptiontracenumber;
	}

	// 返回本地所有缓存的Exception trace
	public static List<String> getAllExceptions() throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 以当前日期作为当日的log文件的文件名
		String logFileName = "ThreadManage-" + sdf.format(new Date()) + ".log";
		List<String> listexception = new ArrayList<String>();
		String str;
		FileReader reader = new FileReader(logFileName);
		BufferedReader br = new BufferedReader(reader);

		while ((str = br.readLine()) != null) {
			listexception.add(str);
		}
		return listexception;
	}

	// 返回某一个Exception trace
	public static String getException(int index) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 以当前日期作为当日的log文件的文件名
		String logFileName = "ThreadManage-" + sdf.format(new Date()) + ".log";

		int exceptiontracenumber = 0;
		String str;
		FileReader reader = new FileReader(logFileName);
		BufferedReader br = new BufferedReader(reader);
		while ((str = br.readLine()) != null) {
			if (exceptiontracenumber == index - 1) {
				return str;
			}
			exceptiontracenumber++;
		}
		return str;
	}
	
}
