import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;

public class Kernel {

	private static final String RPC_QUEUE_NAME = "rpc_queue"; 
	Channel channel = null;
	QueueingConsumer consumer = null;
	public int testint = 0;
		
	String [] argsinfo;  //存放客户端传来的参数，需要调用的方法名和方法所需要的参数
//	public static String receivedCommond = ""; //将客户端收到的命令以空格为单位拆分后在拼接成一个字符串，然后由ThreadsManage在抛出异常时获取
	CustomClassLoader customcl ;
	Class<?> clazzThreadsManage ;  //Classloader的load后的ThreadsManage的Class对象
	Object crawlerInstance = null; ////classloader的instance后的ThreadsManage的实例化对象
	Class<?> clazzCrawlerURL;
	Class<?> clazzDynamoAPI;
	WebDriver driver = null; 

	public boolean isAlive() {
		return true;
	}
	
	public String testMethod(String arg1, int arg2) {
		return "<String>" + arg1 + " <int>" + arg2;
	}
	
	public Kernel()
	{	
		// TODO Auto-generated method stub
		ConnectionFactory factory = new ConnectionFactory();  
		// 设置MabbitMQ所在主机ip或者主机名
		 factory.setHost("localhost");
//		 factory.setHost("172.31.37.238"); // 本机ip
//		 factory.setUsername("admin");
//		 factory.setPassword("admin");
//		 factory.setPort(5672);

        try
        {
			// 创建一个连接  
			Connection connection = factory.newConnection();  
			// 创建一个频道  
			channel = connection.createChannel();  
			//声明队列  
			channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);  
			//限制：每次最多给一个消费者发送1条消息  
			channel.basicQos(1);  
			//为rpc_queue队列创建消费者，用于处理请求  
			consumer = new QueueingConsumer(channel);  
			channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
			
			startListen(); 
		
		}catch(Exception e)
		{
			e.printStackTrace();
			//把 exception 写到本地的一个文件上去，标注时间
		}
	}
	
	public void startListen()
	{
		System.out.println(" [x] Awaiting RPC requests");
		while (true) 
		{  
			try
			{
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				// 获取请求中的correlationId属性值，并将其设置到结果消息的correlationId属性中
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId()).build();
				// 获取回调队列名字
				String callQueueName = props.getReplyTo();
				String message = new String(delivery.getBody(), "UTF-8");

				// 发送消息反馈,这样这条消息就在流里被清空了
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

				// 我们有了 message 了，你现在要通过分析它来确定要运行本地的哪个 method
				// CALL method arg1 arg2 … argN
				// SET field value
				// GET field
				
				// 先按照空格切断
				argsinfo = message.split(" "); // 将从主节点获取到的信息按空格进行划分存取数组
				System.out.println("\n\n");
				System.out.println("argsinfo.length:" + argsinfo.length);
				for (String arg : argsinfo){
					// 遍历输出数组中的内容
					System.out.println(arg);
//					receivedCommond += arg + " "; //为了在ThreadsMangae中抛出异常存入本地日志文件中是执行什么命令出的异常，由receivedCommond这个static变量传入ThreadsManage中
				}
				
				//判断从master传来的参数是那种数据类型，int，double，boolean，string，然后存入ArrayList<Object>中，int，double，boolean，string
				ArrayList<Object> argObjects = new ArrayList<Object>();
				for (int i = 0; i < argsinfo.length; i++) {
					if (argsinfo[i].matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$") == true) {
						if (argsinfo[i].contains(".")) {
							double field = Double.parseDouble(argsinfo[i]);
							argObjects.add(field);
						} else {
							int field = Integer.parseInt(argsinfo[i]);
							argObjects.add(field);
						}
					} else if (argsinfo[i].equals("true")) {
						Boolean field = true;
						argObjects.add(field);
					} else if (argsinfo[i].equals("false")) {
						Boolean field = false;
						argObjects.add(field);
					} else {
						argObjects.add(argsinfo[i]);
					}
				}
				
				//先分析是 CALL, SET, GET, CALL_inner, SET_inner, GET_inner 中的哪一个
				String operation = argObjects.get(0).toString().trim();  //CALL, SET, GET, CALL_inner, SET_inner, GET_inner
				System.out.println("Operation: "+operation);
				Class<?> thisClass = Kernel.class;
				
				//SET
				if(operation.equals("SET"))
				{
					String fieldname = argObjects.get(1).toString().trim();
					Object value = argObjects.get(2);
					
					System.out.println("SET <field>: " + fieldname + " with <value>" + value.toString());
				    Field field = thisClass.getDeclaredField(fieldname);
				    field.set(this, value);
				    System.out.println("set后的testint值为：" + testint);
				    System.out.println("SET successful");
				    
				    String response = "SET successful";   //将要传回客户端的结果存在response中
					//发送回调结果  
					channel.basicPublish("", callQueueName, replyProps, response.getBytes()); 
					System.out.println("SET命令执行完毕\n\n");
					continue; 
				}
				//GET
				else if(operation.equals("GET"))
				{
					String fieldname = argObjects.get(1).toString().trim();
					
					System.out.println("GET <field>: " + fieldname);
				    Field field = thisClass.getDeclaredField(fieldname);
				    String invokeResult = field.get(this).toString();
					System.out.println("GET successful");
					
					//发送回调结果  
					channel.basicPublish("", callQueueName, replyProps, invokeResult.getBytes()); 
					System.out.println("GET命令执行完毕\n\n");
					continue; 
				}
				//CALL
				else if(operation.equals("CALL"))
				{
					Method[] methods = thisClass.getDeclaredMethods();
					String methodName = argObjects.get(1).toString().trim();  //要call的方法名
					boolean methodNameFound = false;
					
					ArrayList<Object> methodArgs = new ArrayList<Object>();   //将要call的方法的所需参数存入ArrayList中
					for (int i = 2; i < argObjects.size(); i++)
						methodArgs.add(argObjects.get(i));
										
					for(int i=0;i<methods.length;i++) 
					{
						if(methods[i].getName().equals(methodName))
						{				
							methodNameFound = true;	  // we have hit the method
							methods[i].setAccessible(true);   //设置通过反射访问该成员方法时取消访问权限设置
							try
							{
								String invokeResult = "";
								if (methodArgs.size() == 0)
									invokeResult = methods[i].invoke(this).toString();
								else
									invokeResult = methods[i].invoke(this, methodArgs.toArray()).toString();
								
								//发送回调结果  
								if(invokeResult == null)
									invokeResult = "";
								channel.basicPublish("", callQueueName, replyProps, invokeResult.getBytes()); 
								System.out.println("CALL命令执行完毕\n\n");
								break; 												
							}
							catch(NullPointerException e){
								channel.basicPublish("", callQueueName, replyProps, e.toString().getBytes());
								break;
							}
							catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								// TODO Auto-generated catch block
								Kernel.logStackTrace(e, argsinfo);
								e.printStackTrace();
								channel.basicPublish("", callQueueName, replyProps, e.toString().getBytes());
								break;
							}
							catch(Exception e)
							{
								Kernel.logStackTrace(e, argsinfo);
								e.printStackTrace();
								channel.basicPublish("", callQueueName, replyProps, e.toString().getBytes());
								break;
							}				
						}
					}					
				}
				//SET_inner
				else if(operation.equals("SET_inner")){
					String fieldname = argObjects.get(1).toString().trim();
					Object value = argObjects.get(2);

					System.out.println("SET Threads.class <field>: " + fieldname + " with <value>: " + value.toString());
					Field field = clazzThreadsManage.getDeclaredField(fieldname);
					field.setAccessible(true); // 设置通过反射访问该成员变量时取消访问权限设置
					field.set(crawlerInstance, value); 
					System.out.println("set后的crawlertestint值为: "
							+ field.get(crawlerInstance).toString());
					System.out.println("SET successful");
				    
				    String response = "SET successful";   //将要传回客户端的结果存在response中
					//发送回调结果  
					channel.basicPublish("", callQueueName, replyProps, response.getBytes()); 
					System.out.println("SET_inner命令执行完毕\n\n");
					continue; 
				}
				//GET_inner
				else if(operation.equals("GET_inner")){
					String fieldname = argObjects.get(1).toString().trim();
					System.out.println("GET <field> "+fieldname);
					Field field = clazzThreadsManage.getDeclaredField(fieldname);
					field.setAccessible(true);   //设置通过反射访问该成员变量时取消访问权限设置
					String invokeResult = field.get(crawlerInstance).toString();
					System.out.println("GET successful");
					
					//发送回调结果  
					channel.basicPublish("", callQueueName, replyProps, invokeResult.getBytes()); 
					System.out.println("GET_inner命令执行完毕\n\n");
					continue; 
				}
				//CALL_inner
				else if(operation.equals("CALL_inner")){
					Method[] methods = clazzThreadsManage.getDeclaredMethods();
					String methodName = argObjects.get(1).toString().trim();
					boolean methodNameFound = false;
					
					ArrayList<Object> methodArgs = new ArrayList<Object>();
					for (int i = 2; i < argObjects.size(); i++)
						methodArgs.add(argObjects.get(i));
										
					for(int i=0;i<methods.length;i++) 
					{
						if(methods[i].getName().equals(methodName))
						{				
							methodNameFound = true;	// we have hit the method
							methods[i].setAccessible(true);   //设置通过反射访问该成员方法时取消访问权限设置
							try
							{
								String invokeResult = "";
								if (methodArgs.size() == 0)
									invokeResult = methods[i].invoke(crawlerInstance).toString();
								else
									invokeResult = methods[i].invoke(crawlerInstance, methodArgs.toArray()).toString();
								
//								receivedCommond = "";
								//发送回调结果  
								if(invokeResult == (null))
									invokeResult = "";
								channel.basicPublish("", callQueueName, replyProps, invokeResult.getBytes()); 
								System.out.println("CALL_inner命令执行完毕\n\n");
								
								break; 												
							}
							catch(NullPointerException e){
//								Kernel.logStackTrace(e, argsinfo);
								channel.basicPublish("", callQueueName, replyProps, e.toString().getBytes());
								break;
							}
							catch(Exception e)
							{
								Kernel.logStackTrace(e, argsinfo);
								e.printStackTrace();
								channel.basicPublish("", callQueueName, replyProps, e.toString().getBytes());
								break;
							}
						}			
					}					
				
				}
			}catch(Exception e)
			{e.printStackTrace();}  		               
        }  
	}
	
	//只load ThreadsManage的方法
	public Class<?> loadclass(String className, String filepath) throws IOException {
		customcl = new CustomClassLoader();
		clazzThreadsManage = customcl.defineClass(className, filepath);
		return clazzThreadsManage;
	}
	
	//load ThreadsManage和CrawlerURL的方法，必须要同时load这两个类，如果不在这里先loadCrawlerURL的话，则之后无法动态定义Crawler
	public Class<?> loadtwoclass(String ThreadsManageclassName, String ThreadsManagefilepath, String CrawlerURLclassName, String CrawlerURLfilepath) throws IOException {
		customcl = new CustomClassLoader();
		clazzThreadsManage = customcl.defineClass(ThreadsManageclassName, ThreadsManagefilepath);
		clazzCrawlerURL = customcl.defineClass(CrawlerURLclassName, CrawlerURLfilepath);
		return clazzThreadsManage;
	}
	
	//load load ThreadsManage，CrawlerURL和DynamoAPI
	public Class<?> loadThreeclass(String ThreadsManageclassName, String ThreadsManagefilepath, String CrawlerURLclassName, String CrawlerURLfilepath,String DynamoAPIclassName, String DynamoAPIfilepath) throws IOException {
		customcl = new CustomClassLoader();
		clazzThreadsManage = customcl.defineClass(ThreadsManageclassName, ThreadsManagefilepath);
		clazzCrawlerURL = customcl.defineClass(CrawlerURLclassName, CrawlerURLfilepath);
		clazzDynamoAPI = customcl.defineClass(DynamoAPIclassName, DynamoAPIfilepath);
		return clazzThreadsManage;
	}

	public boolean instanceClass() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		CustomClassLoader loader = new CustomClassLoader();
		crawlerInstance = loader.instanceClass(clazzThreadsManage);
		return true;
	}

	//所有的exception该写入本地的一个文件，以当天的日期命名，每天的错误写在一个文件里，标明错误发生时间，和收到的指令是什么，同时存入一个ArrayList或类似结构中，标明时间，方便远程读取
	public static void logStackTrace(Exception e, String[] argsinfo) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //以当前日期作为当日的log文件的文件名
		String logFileName = "Kernel-" + sdf.format(new Date()) + ".log";
		
		String commond = "Recvived Commond:";
		for(String arg : argsinfo)
			commond += arg + " ";
		StackTraceElement[] ste = e.getStackTrace();
		int stelength = ste.length;
		String x = null ;
		for (int i = 0; i < stelength; i++) {
			x = "   " + ste[i].toString();
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(logFileName, true));

			bw.write(new Date() + "--" + commond);
			bw.write(e.toString());
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
	
	//返回本地缓存的Exception trace有多少，这里的Exception trace指的整个错误的内容，包括时间，收到的指令，和完整的stackTrace
	public static int getNumberOfExceptions() throws IOException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //以当前日期作为当日的log文件的文件名
		String logFileName = "Kernel-" + sdf.format(new Date()) + ".log";
		
		int exceptiontracenumber = 0;
		String str;
		FileReader reader = new FileReader(logFileName);
		BufferedReader br = new BufferedReader(reader);
		while((str = br.readLine()) != null){
			exceptiontracenumber++;

		}
		return exceptiontracenumber;
	}

	//返回本地所有缓存的Exception trace
	public static List<String> getAllExceptions() throws IOException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //以当前日期作为当日的log文件的文件名
		String logFileName = "Kernel-" + sdf.format(new Date()) + ".log";
		List<String> listexception = new ArrayList<String>();
		String str;
		FileReader reader = new FileReader(logFileName);
		BufferedReader br = new BufferedReader(reader);
		
		while((str = br.readLine()) != null){
			listexception.add(str);

		}
		return listexception;
	}

	//返回某一个Exception trace
	public static String getException(int index) throws IOException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //以当前日期作为当日的log文件的文件名
		String logFileName = "Kernel-" + sdf.format(new Date()) + ".log";
		
		int exceptiontracenumber = 0;
		String str;
		FileReader reader = new FileReader(logFileName);
		BufferedReader br = new BufferedReader(reader);
		while((str = br.readLine()) != null){
			if(exceptiontracenumber == index-1){
				return str;
			}
			exceptiontracenumber++;
		}
		return str;
	}
	
	//这个方法来查询本地剩余磁盘空间，返回百分比
	public static void checkLocalDiskSpace(){
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Kernel();
	}

}


class CustomClassLoader extends ClassLoader {

	public CustomClassLoader() {

	}
	
	// 定义class
		public Class<?> defineClass(String className, String filepath) throws IOException {
			File file = new File(filepath);
			long len = file.length();
			byte[] classData = new byte[(int) len];
			FileInputStream fi = new FileInputStream(file);
			int r = fi.read(classData);
			if (r != len)
				System.out.println("无法读取全部文件");

			fi.close();
			System.out.println("className:" + className);
			System.out.println("filepath:" + filepath);
			System.out.println("classData:" + classData.length);
			Class<?> cl = defineClass(className, classData, 0, classData.length);
			return cl;
		}

		// 实例化class对象
		public Object instanceClass(Class<?> cl) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Constructor<?>[] cons = cl.getConstructors();
			return cons[0].newInstance();
		}

}
