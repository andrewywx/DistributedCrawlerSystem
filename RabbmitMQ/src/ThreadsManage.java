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
	DynamoAPI dynamo; //����DynamoDB��ʵ��
	
	boolean status = false; //ͨ�����״̬λ�������̵߳�run�����н����жϣ������maseter����һ������ȡһ��url�б�Ĳ����������slave�ͽ�������
	ArrayList<String> urlsToCrawl = new ArrayList<String>(); //��Ŵ�DynamoDb�ж�ȡ���Ĵ���ȡurl
//	ArrayList<String> urlsToCrawl = (ArrayList<String>) Collections.synchronizedList(new ArrayList<String>()); //��Ŵ�DynamoDb�ж�ȡ���Ĵ���ȡurl
	ArrayList<ThreadsManage> threads = new ArrayList<ThreadsManage>();

	int urlIndex = 0;
	ThreadsManage master = null;
	String nodeID = null; //��Clientͨ��SET_inner��������nodeID
	String urlstable = null; //��Clientͨ��SET_inner�����������url�ı���
	String resulttable = null; //��Clientͨ��SET_inner��������������Ҫ��ı���
	
	String outputFile = "outputFile";
	String errorFile = "errorFile";
	String url = "";
	String uuid = "";

	int successCount = 0;
	int failureCount = 0;

	static CountDownLatch latch = null;  //ʹ��CountDownLatch���ö���̲߳���ִ����֮����ִ�������Ĳ���
	
	public ThreadsManage() {
		// outputFile ��������������ļ�
		// errorFile ��������������ļ�
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
	
	// ��һ�������ļ�ȥ������Ҫ����URL,������ļ��ÿһ�о���һ��Ҫ����URL
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

	//��Dynamo���ݿ��и���ָ����������ȡ���еĴ���url��������ArrayList��
	public synchronized void readURLsFromDynamo() throws IOException{
		try{
			dynamo = new DynamoAPI();
			dynamo.connectToDB();
			dynamo.getItemsToArrayListByRequest2(urlsToCrawl, urlstable, nodeID);
		}
		catch(Exception e){
			ThreadsManage.logStackTrace(e);
		}

		System.out.println("�ýڵ�������ȡ��url����Ϊ:" + urlsToCrawl.size());
		System.out.println("--------------------------------------------");
		for (String url : urlsToCrawl) {
			System.out.println(url);
		}
		System.out.println("--------------------------------------------");
	}
	
	public void createThreads(int num) // ���� slave
	{
		System.out.println("�����̵߳�����Ϊ:" + num);

		latch = new CountDownLatch(num);

		if (threads != null)
			threads.clear();

		// ���� num ��Crawlerʵ��
		for (int i = 0; i < num; i++)
			threads.add(new ThreadsManage());
		// ����Ҫע�⣬��Ϊ��� ThreadsManager ���÷��䶨��ģ���Ҳ����������ǲ��ǻ��ǵ��÷��䣬���˾�֪����
		
		System.out.println("threads.size():" + threads.size());
		for (int i = 0; i < threads.size(); i++){
			threads.get(i).setMaster(this);
			threads.get(i).status = true;
		}
	}
	
//	public void setMaster() // ����Ǹ�master�õ�
//	{
//		System.out.println("threads.size():" + threads.size());
//		for (int i = 0; i < threads.size(); i++)
//			threads.get(i).setMaster(this);
//	}

	public void setMaster(ThreadsManage master) // ����Ǹ�slave�õ�
	{
		this.master = master;
	}
	
	public void runMasterThread(){
		this.start();
	}
	
	public void runThreads() //����Ǹ�master�õķ������������е�slave
	{
		for(int i=0;i<threads.size();i++)
			threads.get(i).start();	
	}
	
	public void stopThreads(){ //����Ǹ�master�õķ�����ֹͣslave��Threads
		for(int i=0;i<threads.size();i++)
			threads.get(i).stop();	
	}
	
	public synchronized void increaseSuccess() {
		successCount++;
	}

	public synchronized void increaseFailure() {
		failureCount++;
	}
	
	public synchronized String getURL() // ���Ǹ�synchronized�ķ���������slave����urlʱ��ͬ���ģ������ظ���һ��
	{
		if (urlIndex < urlsToCrawl.size()) {
			url = urlsToCrawl.get(urlIndex);
			urlIndex++;
			return url;
		}
		return null; // ������
	}
	
	public void setOutputFile(String result) // ����slave���������
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

	public void setErrorFile(String result) // ����slaveʧ�ܵ�url������
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
	
	public String getStatus() throws InterruptedException  //������Ĺ����оͿ��Բ鿴�����״��
	{
		return "Total: " + urlsToCrawl.size() + " Success:" + successCount + " Failure:" + failureCount;
	}
	
	public String finalgetStatus() throws InterruptedException  //�������������֮�����������
	{
		latch.await();
		return "Total: " + urlsToCrawl.size() + " Success:" + successCount + " Failure:" + failureCount;
	}
	
	public void run() // ����Ǹ�slave�õķ�������������ҳ�Ĵ���
	{
		if(status == true){
			if (master == null)
				return; // û��master,������
			
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
					break; // ������
				}			
				try
				{
//					System.out.println(currentThread().getName());
//					crawlerurl = new CrawlerURL();
//					response = crawlerurl.crawler(url);
//					master.increaseSuccess();
//					
//					//������Ľ��д��DynamoDB���ݿ���
//					dynamo.insertItemsIntoResultTable2(master.resulttable, master.nodeID, uuid, response, error);
//					//�������url�ɹ���ȡ����status��false�޸�Ϊcomplete
//					dynamo.updateItem(master.urlstable, master.nodeID, uuid, "complete");
//					
//					System.out.println("response:" + response);
//					System.out.println("Total: " + master.urlsToCrawl.size() + "  Success:" + master.successCount + "  Failure:" + master.failureCount);
//					
//					// ��url�����Ľ�� append �� outputFile
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
						
						//���GoogleScholarCheckTable����û�����author�����ֵĻ����ͽ��µĴ���ȡ��url����URLtable2��
						if(dynamo.checkCoauthorName((String)entry.getKey()) == false){
							dynamo.insertOneItemIntoUrlTable2(master.urlstable, master.nodeID, (String)entry.getValue(), "false"); 
							dynamo.insertOneItemIntoGoogleScholarCheckTable((String)entry.getKey());
						}
						//������Ľ��д��DynamoDB���ݿ���
						response = entry.getKey() + "\t" + entry.getValue();
						dynamo.insertItemsIntoResultTable2(master.resulttable, master.nodeID, UUID.randomUUID().toString(), response, error);
						//�������url�ɹ���ȡ����status��false�޸�Ϊcomplete
						dynamo.updateItem(master.urlstable, master.nodeID, uuid, "complete");
						
						System.out.println("response:" + response);
						System.out.println("Total: " + master.urlsToCrawl.size() + "  Success:" + master.successCount + "  Failure:" + master.failureCount);
						
						// ��url�����Ľ�� append �� outputFile
						setOutputFile(response);
						response = "No response";	
					}
				} 
				catch (Exception e) {
					master.increaseFailure();
					error = e.toString();
					ThreadsManage.logStackTrace(e);
					
					// ������Ĵ�����Ϣд��DynamoDB���ݿ���
					dynamo.insertItemsIntoResultTable2(master.resulttable, master.nodeID, UUID.randomUUID().toString(), response, error);
					//�������url�ɹ���ȡ����status��false�޸�Ϊerror
					dynamo.updateItem(master.urlstable, master.nodeID, uuid, "error");
					System.out.println(master.urlstable);
					System.out.println(master.nodeID);
					System.out.println(uuid);

					error = "No error";
					
					// �ѳ���� url �ʹ�����Ϣ append �� errorFile
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

					Thread.sleep(30000); //ÿ��һ�������¶�ȡһ���¼����url�����������ArrayList��
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

	// ���е�exception��д�뱾�ص�һ���ļ����Ե��������������ÿ��Ĵ���д��һ���ļ������������ʱ�䣬���յ���ָ����ʲô��ͬʱ����һ��ArrayList�����ƽṹ�У�����ʱ�䣬����Զ�̶�ȡ
	public static void logStackTrace(Exception e) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // �Ե�ǰ������Ϊ���յ�log�ļ����ļ���
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

	// ���ر��ػ����Exception trace�ж��٣������Exception traceָ��������������ݣ�����ʱ�䣬�յ���ָ���������stackTrace
	public static int getNumberOfExceptions() throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // �Ե�ǰ������Ϊ���յ�log�ļ����ļ���
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

	// ���ر������л����Exception trace
	public static List<String> getAllExceptions() throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // �Ե�ǰ������Ϊ���յ�log�ļ����ļ���
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

	// ����ĳһ��Exception trace
	public static String getException(int index) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // �Ե�ǰ������Ϊ���յ�log�ļ����ļ���
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
