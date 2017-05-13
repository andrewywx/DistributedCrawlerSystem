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
		
	String [] argsinfo;  //��ſͻ��˴����Ĳ�������Ҫ���õķ������ͷ�������Ҫ�Ĳ���
//	public static String receivedCommond = ""; //���ͻ����յ��������Կո�Ϊ��λ��ֺ���ƴ�ӳ�һ���ַ�����Ȼ����ThreadsManage���׳��쳣ʱ��ȡ
	CustomClassLoader customcl ;
	Class<?> clazzThreadsManage ;  //Classloader��load���ThreadsManage��Class����
	Object crawlerInstance = null; ////classloader��instance���ThreadsManage��ʵ��������
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
		// ����MabbitMQ��������ip����������
		 factory.setHost("localhost");
//		 factory.setHost("172.31.37.238"); // ����ip
//		 factory.setUsername("admin");
//		 factory.setPassword("admin");
//		 factory.setPort(5672);

        try
        {
			// ����һ������  
			Connection connection = factory.newConnection();  
			// ����һ��Ƶ��  
			channel = connection.createChannel();  
			//��������  
			channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);  
			//���ƣ�ÿ������һ�������߷���1����Ϣ  
			channel.basicQos(1);  
			//Ϊrpc_queue���д��������ߣ����ڴ�������  
			consumer = new QueueingConsumer(channel);  
			channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
			
			startListen(); 
		
		}catch(Exception e)
		{
			e.printStackTrace();
			//�� exception д�����ص�һ���ļ���ȥ����עʱ��
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
				// ��ȡ�����е�correlationId����ֵ�����������õ������Ϣ��correlationId������
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId()).build();
				// ��ȡ�ص���������
				String callQueueName = props.getReplyTo();
				String message = new String(delivery.getBody(), "UTF-8");

				// ������Ϣ����,����������Ϣ�������ﱻ�����
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

				// �������� message �ˣ�������Ҫͨ����������ȷ��Ҫ���б��ص��ĸ� method
				// CALL method arg1 arg2 �� argN
				// SET field value
				// GET field
				
				// �Ȱ��տո��ж�
				argsinfo = message.split(" "); // �������ڵ��ȡ������Ϣ���ո���л��ִ�ȡ����
				System.out.println("\n\n");
				System.out.println("argsinfo.length:" + argsinfo.length);
				for (String arg : argsinfo){
					// ������������е�����
					System.out.println(arg);
//					receivedCommond += arg + " "; //Ϊ����ThreadsMangae���׳��쳣���뱾����־�ļ�����ִ��ʲô��������쳣����receivedCommond���static��������ThreadsManage��
				}
				
				//�жϴ�master�����Ĳ����������������ͣ�int��double��boolean��string��Ȼ�����ArrayList<Object>�У�int��double��boolean��string
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
				
				//�ȷ����� CALL, SET, GET, CALL_inner, SET_inner, GET_inner �е���һ��
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
				    System.out.println("set���testintֵΪ��" + testint);
				    System.out.println("SET successful");
				    
				    String response = "SET successful";   //��Ҫ���ؿͻ��˵Ľ������response��
					//���ͻص����  
					channel.basicPublish("", callQueueName, replyProps, response.getBytes()); 
					System.out.println("SET����ִ�����\n\n");
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
					
					//���ͻص����  
					channel.basicPublish("", callQueueName, replyProps, invokeResult.getBytes()); 
					System.out.println("GET����ִ�����\n\n");
					continue; 
				}
				//CALL
				else if(operation.equals("CALL"))
				{
					Method[] methods = thisClass.getDeclaredMethods();
					String methodName = argObjects.get(1).toString().trim();  //Ҫcall�ķ�����
					boolean methodNameFound = false;
					
					ArrayList<Object> methodArgs = new ArrayList<Object>();   //��Ҫcall�ķ����������������ArrayList��
					for (int i = 2; i < argObjects.size(); i++)
						methodArgs.add(argObjects.get(i));
										
					for(int i=0;i<methods.length;i++) 
					{
						if(methods[i].getName().equals(methodName))
						{				
							methodNameFound = true;	  // we have hit the method
							methods[i].setAccessible(true);   //����ͨ��������ʸó�Ա����ʱȡ������Ȩ������
							try
							{
								String invokeResult = "";
								if (methodArgs.size() == 0)
									invokeResult = methods[i].invoke(this).toString();
								else
									invokeResult = methods[i].invoke(this, methodArgs.toArray()).toString();
								
								//���ͻص����  
								if(invokeResult == null)
									invokeResult = "";
								channel.basicPublish("", callQueueName, replyProps, invokeResult.getBytes()); 
								System.out.println("CALL����ִ�����\n\n");
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
					field.setAccessible(true); // ����ͨ��������ʸó�Ա����ʱȡ������Ȩ������
					field.set(crawlerInstance, value); 
					System.out.println("set���crawlertestintֵΪ: "
							+ field.get(crawlerInstance).toString());
					System.out.println("SET successful");
				    
				    String response = "SET successful";   //��Ҫ���ؿͻ��˵Ľ������response��
					//���ͻص����  
					channel.basicPublish("", callQueueName, replyProps, response.getBytes()); 
					System.out.println("SET_inner����ִ�����\n\n");
					continue; 
				}
				//GET_inner
				else if(operation.equals("GET_inner")){
					String fieldname = argObjects.get(1).toString().trim();
					System.out.println("GET <field> "+fieldname);
					Field field = clazzThreadsManage.getDeclaredField(fieldname);
					field.setAccessible(true);   //����ͨ��������ʸó�Ա����ʱȡ������Ȩ������
					String invokeResult = field.get(crawlerInstance).toString();
					System.out.println("GET successful");
					
					//���ͻص����  
					channel.basicPublish("", callQueueName, replyProps, invokeResult.getBytes()); 
					System.out.println("GET_inner����ִ�����\n\n");
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
							methods[i].setAccessible(true);   //����ͨ��������ʸó�Ա����ʱȡ������Ȩ������
							try
							{
								String invokeResult = "";
								if (methodArgs.size() == 0)
									invokeResult = methods[i].invoke(crawlerInstance).toString();
								else
									invokeResult = methods[i].invoke(crawlerInstance, methodArgs.toArray()).toString();
								
//								receivedCommond = "";
								//���ͻص����  
								if(invokeResult == (null))
									invokeResult = "";
								channel.basicPublish("", callQueueName, replyProps, invokeResult.getBytes()); 
								System.out.println("CALL_inner����ִ�����\n\n");
								
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
	
	//ֻload ThreadsManage�ķ���
	public Class<?> loadclass(String className, String filepath) throws IOException {
		customcl = new CustomClassLoader();
		clazzThreadsManage = customcl.defineClass(className, filepath);
		return clazzThreadsManage;
	}
	
	//load ThreadsManage��CrawlerURL�ķ���������Ҫͬʱload�������࣬�������������loadCrawlerURL�Ļ�����֮���޷���̬����Crawler
	public Class<?> loadtwoclass(String ThreadsManageclassName, String ThreadsManagefilepath, String CrawlerURLclassName, String CrawlerURLfilepath) throws IOException {
		customcl = new CustomClassLoader();
		clazzThreadsManage = customcl.defineClass(ThreadsManageclassName, ThreadsManagefilepath);
		clazzCrawlerURL = customcl.defineClass(CrawlerURLclassName, CrawlerURLfilepath);
		return clazzThreadsManage;
	}
	
	//load load ThreadsManage��CrawlerURL��DynamoAPI
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

	//���е�exception��д�뱾�ص�һ���ļ����Ե��������������ÿ��Ĵ���д��һ���ļ������������ʱ�䣬���յ���ָ����ʲô��ͬʱ����һ��ArrayList�����ƽṹ�У�����ʱ�䣬����Զ�̶�ȡ
	public static void logStackTrace(Exception e, String[] argsinfo) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //�Ե�ǰ������Ϊ���յ�log�ļ����ļ���
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
	
	//���ر��ػ����Exception trace�ж��٣������Exception traceָ��������������ݣ�����ʱ�䣬�յ���ָ���������stackTrace
	public static int getNumberOfExceptions() throws IOException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //�Ե�ǰ������Ϊ���յ�log�ļ����ļ���
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

	//���ر������л����Exception trace
	public static List<String> getAllExceptions() throws IOException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //�Ե�ǰ������Ϊ���յ�log�ļ����ļ���
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

	//����ĳһ��Exception trace
	public static String getException(int index) throws IOException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //�Ե�ǰ������Ϊ���յ�log�ļ����ļ���
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
	
	//�����������ѯ����ʣ����̿ռ䣬���ذٷֱ�
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
	
	// ����class
		public Class<?> defineClass(String className, String filepath) throws IOException {
			File file = new File(filepath);
			long len = file.length();
			byte[] classData = new byte[(int) len];
			FileInputStream fi = new FileInputStream(file);
			int r = fi.read(classData);
			if (r != len)
				System.out.println("�޷���ȡȫ���ļ�");

			fi.close();
			System.out.println("className:" + className);
			System.out.println("filepath:" + filepath);
			System.out.println("classData:" + classData.length);
			Class<?> cl = defineClass(className, classData, 0, classData.length);
			return cl;
		}

		// ʵ����class����
		public Object instanceClass(Class<?> cl) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Constructor<?>[] cons = cl.getConstructors();
			return cons[0].newInstance();
		}

}
