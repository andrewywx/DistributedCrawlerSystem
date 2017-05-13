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

	static CountDownLatch latch = null;  //ʹ��CountDownLatch���ö���̲߳���ִ����֮����ִ�������Ĳ���
	
	public Crawler()
	{	
		//outputFile ��������������ļ�
		//errorFile ��������������ļ�		
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
	
	public void readURLs(String path) throws IOException//��һ�������ļ�ȥ������Ҫ����URL,������ļ��ÿһ�о���һ��Ҫ����URL
	{
		FileReader reader = new FileReader(path);
		BufferedReader br = new BufferedReader(reader);
		String content = null;
		while((content = br.readLine()) != null)
			urlsToCrawl.add(content);
		br.close();
		System.out.println("urlsToCrawl.size:" + urlsToCrawl.size());
	}

	public void createThreads(int num) //���� slave
	{
		System.out.println("�����̵߳�����Ϊ:" + num);
		
		latch = new CountDownLatch(num);
		
		if(threads!=null)
			threads.clear();
		
		//���� num ��Crawlerʵ��
		for (int i = 0; i < num; i++)
			threads.add(new Crawler());
		// ����Ҫע�⣬��Ϊ��� Crawler ���÷��䶨��ģ���Ҳ����������ǲ��ǻ��ǵ��÷��䣬���˾�֪����
	}
	
	public void setMaster()               // ����Ǹ�master�õ�
	{
		for (int i = 0; i < threads.size(); i++)
			threads.get(i).setMaster(this);
	}

	public void setMaster(Crawler master) // ����Ǹ�slave�õ�
	{
		this.master = master;
	}
	
	public void runThreads() //����Ǹ�master�õķ������������е�slave
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
	
	public synchronized String getURL() //���Ǹ� synchronized �ķ���������slave����urlʱ��ͬ���ģ������ظ���һ��
	{
		String url;
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
		}
	}
	
	public String getStatus() throws InterruptedException  //������Ĺ����оͿ��Բ鿴�����״��
	{
		return "Total: "+urlsToCrawl.size()+"  Success:"+successCount+"  Failure:"+failureCount;
	}
	
	public String finalgetStatus() throws InterruptedException  //�������������֮�����������
	{
		latch.await();
		return "Total: "+urlsToCrawl.size()+"  Success:"+successCount+"  Failure:"+failureCount;
	}
	
	public void run() //����Ǹ�slave�õķ�������������ҳ�Ĵ���
	{
		if(master==null) 
			return; //û��master,������

		while(true)
		{
			url = master.getURL();	
			if(url == null) {
//				System.out.println("----------------------All urls finished!!--------------------");
				latch.countDown();
				break; //������
			}			
			try
			{
				System.setProperty("webdriver.chrome.driver", "D://jar//chromedriver.exe");
				ChromeDriver driver = new ChromeDriver();
				driver.get(url);
				String response = driver.findElement(By.id("su")).getAttribute("value");
				driver.close();
				master.increaseSuccess();
				//��url�����Ľ�� append �� outputFile
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
				// �ѳ���� url �ʹ�����Ϣ append �� errorFile
				setErrorFile(url);
				setErrorFile(e.toString());
			}
		}	
		
		
	}
	
}
