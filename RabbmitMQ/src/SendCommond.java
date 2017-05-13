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
		// 设置MabbitMQ所在主机ip或者主机名
//		factory.setHost("localhost");
		factory.setHost("54.165.234.165");
		factory.setUsername("admin");
		factory.setPassword("admin");
		factory.setPort(5672);

		// 创建一个连接
		connection = factory.newConnection();
		// 创建一个频道
		channel = connection.createChannel();
		// 声明队列
		channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
		// 为每一个客户端获取一个随机的回调队列
		replyQueueName = channel.queueDeclare().getQueue();
		// 为每一个客户端创建一个消费者（用于监听回调队列，获取结果）,这个消费者是用来消费回调队列的，目的是获取服务器端回复的消息
		consumer = new QueueingConsumer(channel);
		// 消费者与队列关联
		channel.basicConsume(replyQueueName, true, consumer);
	}

	public String call(String[] args) throws IOException,
			ShutdownSignalException, ConsumerCancelledException,
			InterruptedException {
		String response = null;
		// UUID(Universally Unique
		// Identifier)全局唯一标识符,是指在一台机器上生成的数字，它保证对在同一时空中的所有机器都是唯一的。
		String corrID = java.util.UUID.randomUUID().toString();
		// 设置replyTo和correlationId属性值
		BasicProperties props = new BasicProperties.Builder()
				.correlationId(corrID).replyTo(replyQueueName).build();
		// 发送消息到rpc_queue队列
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
		// Identifier)全局唯一标识符,是指在一台机器上生成的数字，它保证对在同一时空中的所有机器都是唯一的。
		String corrID = java.util.UUID.randomUUID().toString();

		// 设置replyTo和correlationId属性值
		BasicProperties props = new BasicProperties.Builder()
				.correlationId(corrID).replyTo(replyQueueName).build();

		// 发送消息到rpc_queue队列
		channel.basicPublish("", RPC_QUEUE_NAME, props, command.getBytes());
		System.out.println("客户端已发送命令:" + command);

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery.getProperties().getCorrelationId().equals(corrID)) {
				response = new String(delivery.getBody(), "utf-8");
				if (response.equals("java.lang.NullPointerException"))
					System.out.println(command + "命令完成\n");
				else
					System.out.println(command + "命令完成,服务端传送回来的response是: "
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

		// 从本地文件中读取url，然后进行爬虫的一系列命令
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

//		 由本地指定待爬去的表名和爬虫节点,然后从Dynamo数据库中读取数据进行爬虫,操作的是本机
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
//		 client.sendCommand("CALL_inner runMasterThread"); //启动master的run方法，每一分钟去取一次新的url放入ArrayList中
//		 client.sendCommand("CALL_inner createThreads 3");
//		 client.sendCommand("CALL_inner runThreads");
////		 String result3 = client.sendCommand("CALL_inner finalgetStatus");
////		 System.out.println("Result is " + result3);
		 
		 

		// 由本地指定待爬去的表名和爬虫节点,然后从Dynamo数据库中读取数据进行爬虫,操作的是Amazon的节点
		String result1 = client.sendCommand("CALL loadtwoclass ThreadsManage /home/ubuntu/AmazonCrawlerNode/ThreadsManage.class JSScholarCrawler /home/ubuntu/AmazonCrawlerNode/JSScholarCrawler.class");
//		String result1 = client.sendCommand("CALL loadThreeclass ThreadsManage /home/ubuntu/AmazonCrawlerNode/ThreadsManage.class JSScholarCrawler /home/ubuntu/AmazonCrawlerNode/JSScholarCrawler.class DynamoAPI /home/ubuntu/AmazonCrawlerNode/DynamoAPI.class");
		System.out.println("Result is " + result1);
		String result2 = client.sendCommand("CALL instanceClass");
		System.out.println("Result is " + result2);
		client.sendCommand("SET_inner nodeID node1");
		client.sendCommand("SET_inner urlstable urltable2");
		client.sendCommand("SET_inner resulttable resulttable2");
		client.sendCommand("CALL_inner readURLsFromDynamo");
		client.sendCommand("CALL_inner runMasterThread"); //启动master的run方法，每一分钟去取一次新的url放入ArrayList中
		client.sendCommand("CALL_inner createThreads 3");
		client.sendCommand("CALL_inner runThreads");
		// String result3 = client.sendCommand("CALL_inner finalgetStatus");
		// System.out.println("Result is " + result3);
		
		
		
		
		
		
//		 由本地指定待爬去的表名和爬虫节点,然后从Dynamo数据库中读取数据进行爬虫,操作的是本机
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
