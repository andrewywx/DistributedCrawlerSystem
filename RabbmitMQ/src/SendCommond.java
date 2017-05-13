import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;

public class SendCommond {
	private static final String RPC_QUEUE_NAME = "rpc_queue";
	private Connection connection;
	private Channel channel;
	private String replyQueueName;
	private QueueingConsumer consumer;

	public SendCommond() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		// ����MabbitMQ��������ip����������
//		factory.setHost("localhost");
		factory.setHost("54.165.234.165");
		factory.setUsername("admin");
		factory.setPassword("admin");
		factory.setPort(5672);

		// ����һ������
		connection = factory.newConnection();
		// ����һ��Ƶ��
		channel = connection.createChannel();
		// ��������
		channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
		// Ϊÿһ���ͻ��˻�ȡһ������Ļص�����
		replyQueueName = channel.queueDeclare().getQueue();
		// Ϊÿһ���ͻ��˴���һ�������ߣ����ڼ����ص����У���ȡ�����,������������������ѻص����еģ�Ŀ���ǻ�ȡ�������˻ظ�����Ϣ
		consumer = new QueueingConsumer(channel);
		// ����������й���
		channel.basicConsume(replyQueueName, true, consumer);
	}

	public String call(String[] args) throws IOException,
			ShutdownSignalException, ConsumerCancelledException,
			InterruptedException {
		String response = null;
		// UUID(Universally Unique
		// Identifier)ȫ��Ψһ��ʶ��,��ָ��һ̨���������ɵ����֣�����֤����ͬһʱ���е����л�������Ψһ�ġ�
		String corrID = java.util.UUID.randomUUID().toString();
		// ����replyTo��correlationId����ֵ
		BasicProperties props = new BasicProperties.Builder()
				.correlationId(corrID).replyTo(replyQueueName).build();
		// ������Ϣ��rpc_queue����
		String argsinfo = "";
		for (String arg : args) {
			argsinfo = argsinfo + arg + " ";
		}

		channel.basicPublish("", RPC_QUEUE_NAME, props, argsinfo.getBytes());
		System.out.println("client already sends the url");

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery.getProperties().getCorrelationId().equals(corrID)) {
				response = new String(delivery.getBody(), "utf-8");
				System.out.println("response" + response);
				break;
			}
		}

		return response;
	}

	public String sendCommand(String command) throws IOException,
			ShutdownSignalException, ConsumerCancelledException,
			InterruptedException {
		String response = null;
		// UUID(Universally Unique
		// Identifier)ȫ��Ψһ��ʶ��,��ָ��һ̨���������ɵ����֣�����֤����ͬһʱ���е����л�������Ψһ�ġ�
		String corrID = java.util.UUID.randomUUID().toString();

		// ����replyTo��correlationId����ֵ
		BasicProperties props = new BasicProperties.Builder()
				.correlationId(corrID).replyTo(replyQueueName).build();

		// ������Ϣ��rpc_queue����
		channel.basicPublish("", RPC_QUEUE_NAME, props, command.getBytes());
		System.out.println("�ͻ����ѷ�������:" + command);

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery.getProperties().getCorrelationId().equals(corrID)) {
				response = new String(delivery.getBody(), "utf-8");
				if (response.equals("java.lang.NullPointerException"))
					System.out.println(command + "�������\n");
				else
					System.out.println(command + "�������,����˴��ͻ�����response��: "
							+ response + "\n");
				break;
			}
		}

		return response;
	}

	/**
	 * @param args
	 * @throws TimeoutException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ConsumerCancelledException
	 * @throws ShutdownSignalException
	 */
	public static void main(String[] args) throws IOException,
			TimeoutException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException {
		// TODO Auto-generated method stub
		SendCommond client = new SendCommond();
		String result = "";
		// String result = sendurl.call(args);

//		 result = client.sendCommand("GET testint");
		// result = client.sendCommand("SET testint 12");
		// result = client.sendCommand("CALL isAlive");
		// client.sendCommand("CALL loadclass Crawler /home/rdpuser/AmazonCrawlerNode/Crawler.class");

		// result =
		// client.sendCommand("CALL loadclass Crawler /home/ywx/workspace/RabbitMQ/bin/Crawler.class");
		// result = client.sendCommand("CALL instanceClass");
		// result = client.sendCommand("CALL runclass http://www.baidu.com");
		// result = client.sendCommand("SET_inner crawlertestint 100");
		// result = client.sendCommand("GET_inner crawlertestint");

		// �ӱ����ļ��ж�ȡurl��Ȼ����������һϵ������
		// String result1 =
		// client.sendCommand("CALL loadtwoclass ThreadsManage D:\\EclipseProject\\RabbmitMQ\\bin\\ThreadsManage.class CrawlerURL D:\\EclipseProject\\RabbmitMQ\\bin\\CrawlerURL.class");
		// System.out.println("Result is " + result1);
		// String result2 = client.sendCommand("CALL instanceClass");
		// System.out.println("Result is " + result2);
		// client.sendCommand("CALL_inner readURLs D:\\urls.txt");
		// client.sendCommand("CALL_inner createThreads 3");
		// // client.sendCommand("CALL_inner setMaster");
		// client.sendCommand("CALL_inner runThreads");
		// String result3 = client.sendCommand("CALL_inner finalgetStatus");
		// System.out.println("Result is " + result3);

//		 �ɱ���ָ������ȥ�ı���������ڵ�,Ȼ���Dynamo���ݿ��ж�ȡ���ݽ�������,�������Ǳ���
////		 String result1 = client.sendCommand("CALL loadtwoclass ThreadsManage D:\\EclipseProject\\RabbmitMQ\\bin\\ThreadsManage.class CrawlerURL D:\\EclipseProject\\RabbmitMQ\\bin\\CrawlerURL.class");
//		 String result1 = client.sendCommand("CALL loadtwoclass ThreadsManage D:\\EclipseProject\\RabbmitMQ\\bin\\ThreadsManage.class JSScholarCrawler D:\\EclipseProject\\RabbmitMQ\\bin\\JSScholarCrawler.class");
//		 System.out.println("Result is " + result1);
////		 String result1 = client.sendCommand("CALL loadThreeclass ThreadsManage D:\\EclipseProject\\RabbmitMQ\\bin\\ThreadsManage.class CrawlerURL D:\\EclipseProject\\RabbmitMQ\\bin\\CrawlerURL.class DynamoAPI D:\\EclipseProject\\RabbmitMQ\\bin\\DynamoAPI.class");
//		 String result2 = client.sendCommand("CALL instanceClass");
//		 System.out.println("Result is " + result2);
//		 client.sendCommand("SET_inner nodeID node1");
//		 client.sendCommand("SET_inner urlstable urltable2");
//		 client.sendCommand("SET_inner resulttable resulttable2");
//		 client.sendCommand("CALL_inner readURLsFromDynamo");
//		 client.sendCommand("CALL_inner runMasterThread"); //����master��run������ÿһ����ȥȡһ���µ�url����ArrayList��
//		 client.sendCommand("CALL_inner createThreads 3");
//		 client.sendCommand("CALL_inner runThreads");
////		 String result3 = client.sendCommand("CALL_inner finalgetStatus");
////		 System.out.println("Result is " + result3);
		 
		 

		// �ɱ���ָ������ȥ�ı���������ڵ�,Ȼ���Dynamo���ݿ��ж�ȡ���ݽ�������,��������Amazon�Ľڵ�
		String result1 = client.sendCommand("CALL loadtwoclass ThreadsManage /home/ubuntu/AmazonCrawlerNode/ThreadsManage.class JSScholarCrawler /home/ubuntu/AmazonCrawlerNode/JSScholarCrawler.class");
//		String result1 = client.sendCommand("CALL loadThreeclass ThreadsManage /home/ubuntu/AmazonCrawlerNode/ThreadsManage.class JSScholarCrawler /home/ubuntu/AmazonCrawlerNode/JSScholarCrawler.class DynamoAPI /home/ubuntu/AmazonCrawlerNode/DynamoAPI.class");
		System.out.println("Result is " + result1);
		String result2 = client.sendCommand("CALL instanceClass");
		System.out.println("Result is " + result2);
		client.sendCommand("SET_inner nodeID node1");
		client.sendCommand("SET_inner urlstable urltable2");
		client.sendCommand("SET_inner resulttable resulttable2");
		client.sendCommand("CALL_inner readURLsFromDynamo");
		client.sendCommand("CALL_inner runMasterThread"); //����master��run������ÿһ����ȥȡһ���µ�url����ArrayList��
		client.sendCommand("CALL_inner createThreads 3");
		client.sendCommand("CALL_inner runThreads");
		// String result3 = client.sendCommand("CALL_inner finalgetStatus");
		// System.out.println("Result is " + result3);
		
		
		
		
		
		
//		 �ɱ���ָ������ȥ�ı���������ڵ�,Ȼ���Dynamo���ݿ��ж�ȡ���ݽ�������,�������Ǳ���
//		 String result1 = client.sendCommand("CALL loadtwoclass ThreadsManage D:\\EclipseProject\\RabbmitMQ\\bin\\ThreadsManage.class CrawlerURL D:\\EclipseProject\\RabbmitMQ\\bin\\CrawlerURL.class");
//		 System.out.println("Result is " + result1);
//		 String result2 = client.sendCommand("CALL instanceClass");
//		 System.out.println("Result is " + result2);
//		 client.sendCommand("CALL_inner readURLs D:\\urls.txt");
//		 client.sendCommand("CALL_inner createThreads 3");
////		 client.sendCommand("CALL_inner runThreads");
//		 String result3 = client.sendCommand("CALL_inner finalgetStatus");
//		 System.out.println("Result is " + result3);
//		 client.sendCommand("CALL getNumberOfExceptions");
//		 client.sendCommand("CALL getAllExceptions");
//		 client.sendCommand("CALL getException 2");

	}

}
