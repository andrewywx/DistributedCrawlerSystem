import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * The Class Methods represents synchronization of Data.
 */
public class DynamoAPI
{
	/** The dynamoDB object. */
	private DynamoDB dynamoDB;

	/** The dynamoDB client. */
	private AmazonDynamoDBClient dynamoClient;

	public DynamoAPI() {

	}
	// 创建存放从coauthor的表，主键是coauthor
	public void createCoauthorTable(String tablename) {
		try{
			System.out.println("Attempting to create table; please wait...");
	        
	        //当主键只有分区键时的建表方式
			Table table = dynamoDB.createTable(tablename, 
					Arrays.asList(new KeySchemaElement("coauthor", KeyType.HASH)),
					Arrays.asList(new AttributeDefinition("coauthor",ScalarAttributeType.S)), 
					new ProvisionedThroughput(10L, 10L));

			table.waitForActive();
			System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());
		}catch(Exception e){
			System.err.println("Unable to create table: ");
            System.err.println(e.getMessage());
		}
	}
	
	//创建存放URL的表urltable,主键是nodeid，副键是uuid
	public void createURLTable(String tableName){
        try {
            System.out.println("Attempting to create table; please wait...");
            
            //当主键有两个时，即主键由分区键和排序键构成时的建表方式
            Table table = dynamoDB.createTable(tableName,
                    Arrays.asList(
                        new KeySchemaElement("nodeID", KeyType.HASH),  //Partition key
                        new KeySchemaElement("UUID", KeyType.RANGE)), //Sort key
                        Arrays.asList(
                            new AttributeDefinition("nodeID", ScalarAttributeType.S),
                            new AttributeDefinition("UUID", ScalarAttributeType.S)), 
                        new ProvisionedThroughput(10L, 10L));
            
            //当主键只有分区键时的建表方式
//			Table table = dynamoDB.createTable(tableName, 
//					Arrays.asList(new KeySchemaElement("uuid", KeyType.HASH)),
//					Arrays.asList(new AttributeDefinition("uuid",ScalarAttributeType.S)), 
//					new ProvisionedThroughput(10L, 10L));

			table.waitForActive();
			System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());

        } catch (Exception e) {
            System.err.println("Unable to create table: ");
            System.err.println(e.getMessage());
        }
	}
	
	//创建存放URL的表urltable2,主键是uuid，副键是nodeid
	//创建存放URL的表urltable2,主键是uuid，副键是nodeid
	public void createURLTable2(String tableName){
        try {
            System.out.println("Attempting to create table; please wait...");
            
            //当主键有两个时，即主键由分区键和排序键构成时的建表方式
            Table table = dynamoDB.createTable(tableName,
                    Arrays.asList(
                        new KeySchemaElement("UUID", KeyType.HASH),  //Partition key
                        new KeySchemaElement("nodeID", KeyType.RANGE)), //Sort key
                        Arrays.asList(
                            new AttributeDefinition("UUID", ScalarAttributeType.S),
                            new AttributeDefinition("nodeID", ScalarAttributeType.S)), 
                        new ProvisionedThroughput(10L, 10L));
            
            //当主键只有分区键时的建表方式
//			Table table = dynamoDB.createTable(tableName, 
//					Arrays.asList(new KeySchemaElement("uuid", KeyType.HASH)),
//					Arrays.asList(new AttributeDefinition("uuid",ScalarAttributeType.S)), 
//					new ProvisionedThroughput(10L, 10L));

			table.waitForActive();
			System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());

        } catch (Exception e) {
            System.err.println("Unable to create table: ");
            System.err.println(e.getMessage());
        }
	}
	
	// 创建存放URL的表resulttable,主键是nodeid，副键是uuid
	
	//创建存放结果的表resulttable,主键是nodeid，副键是uuid
	public void createResultTable(String tableName){
		try {
            System.out.println("Attempting to create table; please wait...");
            
            //当主键有两个时，即主键由分区键和排序键构成时的建表方式
            Table table = dynamoDB.createTable(tableName,
                    Arrays.asList(
                        new KeySchemaElement("nodeID", KeyType.HASH),  //Partition key
                        new KeySchemaElement("UUID", KeyType.RANGE)), //Sort key
                        Arrays.asList(
                            new AttributeDefinition("nodeID", ScalarAttributeType.S),
                            new AttributeDefinition("UUID", ScalarAttributeType.S)), 
                        new ProvisionedThroughput(10L, 10L));

			table.waitForActive();
			System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());

        } catch (Exception e) {
            System.err.println("Unable to create table: ");
            System.err.println(e.getMessage());
        }
	}
	
	// 创建存放URL的表resulttable2,主键是uuid，副键是nodeid

	//创建存放结果的表resulttable2，主键是uuid，副键是nodeid
	public void createResultTable2(String tableName) {
		try {
			System.out.println("Attempting to create table; please wait...");

			// 当主键有两个时，即主键由分区键和排序键构成时的建表方式
			Table table = dynamoDB.createTable(tableName,
					Arrays.asList(new KeySchemaElement("UUID", KeyType.HASH), // Partition
																				// key
							new KeySchemaElement("nodeID", KeyType.RANGE)), // Sort
																			// key
					Arrays.asList(new AttributeDefinition("UUID",
							ScalarAttributeType.S), new AttributeDefinition(
							"nodeID", ScalarAttributeType.S)),
					new ProvisionedThroughput(10L, 10L));

			table.waitForActive();
			System.out.println("Success.  Table status: "
					+ table.getDescription().getTableStatus());

		} catch (Exception e) {
			System.err.println("Unable to create table: ");
			System.err.println(e.getMessage());
		}
	}
	
	// 向urltable表中插入指定数量的记录
	
	//向主键是nodeid，副键是uuid的urltable中插入记录
    public boolean insertItemsIntoTable(int numberItems, String tablename){
		try {
			TableWriteItems tableWriteItems = new TableWriteItems(tablename);

			// put task in the writer
			for (int i = 0; i < numberItems; i++) {
				Item item = new Item()
						.withPrimaryKey("nodeID","node1", "UUID", UUID.randomUUID().toString())
						.with("URL", "https://www.baidu.com")
						.with("Attempt_time", new Date().toString())
						.with("URL_Status", "false");

				tableWriteItems.addItemToPut(item);
			}
			// write all the tasks in dynamo DB
			System.out.println("Making the request.");
			BatchWriteItemOutcome outcome = dynamoDB.batchWriteItem(tableWriteItems);

			System.out.println("Create Item Succeeded:\n" + outcome.getBatchWriteItemResult());

		} catch (Exception e) {
			System.err.println("Failed to create items: ");
			e.printStackTrace();
			return false;
		}

		return true;
	}
    
    // 向urltable2表中插入指定数量的记录
	
    //向主键是uuid，副键是nodeid的urltable2中插入记录
    public boolean insertItemsIntoTable2(int numberItems, String tablename){
		try {
			TableWriteItems tableWriteItems = new TableWriteItems(tablename);

			// put task in the writer
			for (int i = 0; i < numberItems; i++) {
				Item item = new Item()
						.withPrimaryKey("UUID", UUID.randomUUID().toString(),"nodeID","node1")
						.with("URL", "https://www.baidu.com")
						.with("Attempt_time", new Date().toString())
						.with("URL_Status", "false");

				tableWriteItems.addItemToPut(item);
			}
			// write all the tasks in dynamo DB
			System.out.println("Making the request.");
			BatchWriteItemOutcome outcome = dynamoDB.batchWriteItem(tableWriteItems);

			System.out.println("Create Item Succeeded:\n" + outcome.getBatchWriteItemResult());

		} catch (Exception e) {
			System.err.println("Failed to create items: ");
			e.printStackTrace();
			return false;
		}

		return true;
	}
    
    // 向GoogleScholarCheckTable表中插入一条记录，表示一个coauthor的名字
    
    //向 GoogleScholarCheckTable表中插入一条记录
    public boolean insertOneItemIntoGoogleScholarCheckTable(String coauthername){
    	try{
    		TableWriteItems tableWriteItems = new TableWriteItems("GoogleScholarCheckTable");
    		Item item = new Item()
    				.withPrimaryKey("coauthor", coauthername);

    		tableWriteItems.addItemToPut(item);
    		// write all the tasks in dynamo DB
    		System.out.println("Making the request.");
    		BatchWriteItemOutcome outcome = dynamoDB.batchWriteItem(tableWriteItems);

    		System.out.println("Create Item Succeeded:\n"+ outcome.getBatchWriteItemResult());
    	}catch(Exception e){
    		System.err.println("Failed to create items: ");
			e.printStackTrace();
			return false;
    	}
    		return true;
    }
    
    // 检查GoogleScholarCheckTable表中是否存在指定名字的coauthor
 	public boolean checkCoauthorName(String authorname) {
 		boolean checkStatus = false;
 		Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
 		expressionAttributeValues.put(":co_author",new AttributeValue().withS(authorname));

 		ScanRequest scanRequest = new ScanRequest().withTableName("GoogleScholarCheckTable")
 				.withFilterExpression("coauthor = :co_author")
 				.withExpressionAttributeValues(expressionAttributeValues);

 		// scan the database to read the objects
 		ScanResult result = dynamoClient.scan(scanRequest);
 		try {
 			for (Map<String, AttributeValue> item : result.getItems()) { //如果能进到这个for循环中，就表明GoogleScholarCheckTable表中存在这个名字
 				// making a list of tasks to return
// 				String coAuthorName = item.get("coauthor").getS();
// 				System.out.println(authorname + " " + coAuthorName);
 				checkStatus = true;
 			}
 		} catch (Exception e) {
 			System.err.println("Unable to query");
 			System.err.println(e.getMessage());
 		}
 		if (checkStatus == true)
 			return true;
 		else
 			return false;
 	}	
    
    // 向urltable2表中插入一天记录
    //向urltable2中插入一条记录
    public boolean insertOneItemIntoUrlTable2(String tablename, String nodeID, String crawlerurl, String urlstatus){
		try {
			TableWriteItems tableWriteItems = new TableWriteItems(tablename);

			Item item = new Item()
					.withPrimaryKey("UUID", UUID.randomUUID().toString(), "nodeID", nodeID)
					.with("URL", crawlerurl)
					.with("Attempt_time", new Date().toString())
					.with("URL_Status", urlstatus);

			tableWriteItems.addItemToPut(item);
			// write all the tasks in dynamo DB
			System.out.println("Making the request.");
			BatchWriteItemOutcome outcome = dynamoDB
					.batchWriteItem(tableWriteItems);

			System.out.println("Create Item Succeeded:\n"
					+ outcome.getBatchWriteItemResult());

		} catch (Exception e) {
			System.err.println("Failed to create items: ");
			e.printStackTrace();
			return false;
		}

		return true;
	}
    
    // 向resulttable表中插入一条记录
      
    //向resulttable中插入记录
    public boolean insertItemsIntoResultTable(String tablename, String nodeid, String uuid, String result, String error){
    	Table table = dynamoDB.getTable(tablename);
    	try {
			System.out.println("Adding a new items to resulttable");
			PutItemOutcome outcome = table.putItem(new Item()
					.withPrimaryKey("nodeID", nodeid, "UUID", uuid)
					.with("Result", result).with("Error", error));
			
			System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

		} catch (Exception e) {
			 System.err.println("Unable to add item: " + uuid + " " + nodeid);
	         System.err.println(e.getMessage());
			return false;
		}

		return true;
    }
    
	// resulttable2表中插入指定数量的记录
       
    //向resulttable2中插入记录
    public boolean insertItemsIntoResultTable2(String tablename, String nodeid, String uuid, String result, String error){
    	Table table = dynamoDB.getTable(tablename);
    	try {
			System.out.println("Adding a new items to resulttable");
			PutItemOutcome outcome = table.putItem(new Item()
			.withPrimaryKey("UUID", uuid, "nodeID",nodeid)
			.with("Result", result)
			.with("Error", error));
			
			System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

		} catch (Exception e) {
			 System.err.println("Unable to add item: " + uuid + " " + nodeid);
	         System.err.println(e.getMessage());
			return false;
		}

		return true;
    }
    
    
	// 读取表中的所有数据并打印
	public void readItems(String tablename){

		ScanRequest scanRequest = new ScanRequest().withTableName(tablename);

		ScanResult result = dynamoClient.scan(scanRequest);
		for (Map<String, AttributeValue> item : result.getItems()) {
			// making a list of tasks to return
			String uuid = item.get("UUID").getS();
			
			for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
				System.out.println(entry.getKey() + "--->" + entry.getValue());
			}
			
			System.out.println();
		}	
	
	}
	
	// 读取GoogleScholarCheckTable表中所有的数据
	public void readGoogleScholarCheckTableItems(String tablename){

		ScanRequest scanRequest = new ScanRequest().withTableName(tablename);

		ScanResult result = dynamoClient.scan(scanRequest);
		for (Map<String, AttributeValue> item : result.getItems()) {
			// making a list of tasks to return
			String coauthor = item.get("coauthor").getS();
			
			for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
				System.out.println(entry.getKey() + "--->" + entry.getValue());
			}
			
			System.out.println();
		}	
	
	}
	
	// 从urltable2中根据nodeid的值来读取记录，urltable2是主键为uuid，副键是nodeid的表
	public void readItemsByRequest2(String tablename, String nodeid) {
		Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
		expressionAttributeValues.put(":node_id", new AttributeValue().withS(nodeid));

		ScanRequest scanRequest = new ScanRequest().withTableName(tablename)
				.withFilterExpression("nodeID = :node_id")
				.withExpressionAttributeValues(expressionAttributeValues);

		// scan the database to read the objects
		ScanResult result = dynamoClient.scan(scanRequest);
		for (Map<String, AttributeValue> item : result.getItems()) {
			// making a list of tasks to return
			String UUID = item.get("UUID").getS();
			String nodeID = item.get("nodeID").getS();
			String url = item.get("URL").getS();
			String urlstatus = item.get("URL_Status").getS();

			if (urlstatus.equals("false")){
				System.out.println(UUID + " " + nodeID + " " + url);
				updateItem("urltable2", nodeID, UUID, "true");
			}
		}
	}
	
	// 从urltable2中根据nodeid的值来读取状态位为false的记录，并将结果存入ThreadsManage中的urlsToCrawl中，然后将读取出的记录的状态位修改为true
	
	//从urltable2中根据nodeid的值来读取记录，并将记录存入ThreadsManage的urlsToCrawl中
	public void getItemsToArrayListByRequest2(ArrayList<String> urlsToCrawl, String tablename, String nodeid) {
		Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
		expressionAttributeValues.put(":node_id",new AttributeValue().withS(nodeid));

		ScanRequest scanRequest = new ScanRequest().withTableName(tablename)
				.withFilterExpression("nodeID = :node_id")
				.withExpressionAttributeValues(expressionAttributeValues);

		// scan the database to read the objects
		ScanResult result = dynamoClient.scan(scanRequest);
		try {
			for (Map<String, AttributeValue> item : result.getItems()) {
				// making a list of tasks to return
				String UUID = item.get("UUID").getS();
				String nodeID = item.get("nodeID").getS();
				String url = item.get("URL").getS();
				String urlstatus = item.get("URL_Status").getS();

				if (urlstatus.equals("false")){  //状态位为false的url是还未放入ArrayList中的url，每一分钟检查一次把他们放入ArrayList中，并将状态位修改为true，表示待爬取
					urlsToCrawl.add(UUID + " " + nodeID + " " + url);
					updateItem("urltable2", nodeID, UUID, "true");
				}
			}
		} catch (Exception e) {
			System.err.println("Unable to query");
			System.err.println(e.getMessage());
		}
		System.out.println("urlsToCrawl数量:" + urlsToCrawl.size());
	}
	

	//根据条件来查询表中的记录
	public void readItemsByRequest(String tablename, String nodeid){
		Table table = dynamoDB.getTable(tablename);
		
		HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#nodeid", "nodeID");
        nameMap.put("#uuid", "UUID");
        nameMap.put("#url", "URL");

        HashMap<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put(":idvalue", nodeid);
        
		QuerySpec querySpec = new QuerySpec()
		        .withProjectionExpression("#nodeid, #uuid, #url")
				.withKeyConditionExpression("#nodeid = :idvalue")
				.withNameMap(nameMap)
				.withValueMap(valueMap);

		ItemCollection<QueryOutcome> items = null;
		Iterator<Item> iterator = null;
        Item item = null;
        int count = 0;
        
        try{
        	items = table.query(querySpec);
    		iterator = items.iterator();
    		while (iterator.hasNext()) {
    			item = iterator.next();
    			System.out.println(item.getString("nodeID") + " " + item.getString("UUID") + " " + item.getString("URL"));
    			++count;
    		}
    		System.out.println("The number of the urls: " + count);
        }catch(Exception e){
        	System.err.println("Unable to query");
            System.err.println(e.getMessage());
        }
		
	}
	
	// 修改urltable2表中的状态位，共有四种状态位，false表示表中还没有读出出的url，true表示已读出但还没有爬取的url，complete表示已经完成爬取的url，error表示爬出发生错误的url
	
	//更新urltable2表中的url的状态位，状态位分为四种，false表示未读取的，true表示读取的还未爬虫的，complete表示完成爬虫的，error表示爬虫出现错误的url
	public boolean updateItem(String tablename, String nodeid, String uuid, String newValue) {
		Table table = dynamoDB.getTable(tablename);

		UpdateItemSpec updateItemSpec = new UpdateItemSpec()
				.withPrimaryKey("UUID", uuid, "nodeID", nodeid)
				.withUpdateExpression("set URL_Status = :newValue")
				.withValueMap(new ValueMap().with(":newValue", newValue))
				.withReturnValues(ReturnValue.UPDATED_NEW);

		try {
			System.out.println("Updating the item...");
			UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
			// System.out.println("UpdateItem succeeded:\n" +
			// outcome.getItem().toJSONPretty());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	// 根据表名删除表
	
	//删除表及表中的记录
	public boolean deleteTable(String tablename){
		
        try {
        	Table table = dynamoDB.getTable(tablename);
    		table.delete();
			table.waitForDelete();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
        return true;
	}
	
	
	
	/**
	 * Establishes a connection between Client and DynamoDB.
	 *
	 * @throws AmazonClientException
	 *             when Credentials are wrong Assumes Region to be "US_WEST_2"
	 *             Setup Credentials while making a Project of AWS in Compiler
	 *             or Editer
	 */
	public void connectToDB() throws AmazonClientException {
		// getting ASW credentials stored in ~/.aws/credentials file or
		// in project setup
		AWSCredentials credentials = new BasicAWSCredentials(
				"AKIAJWFTCGBPZC6VXLFQ",
				"6ckM6Aiyk0Sn4bmTKMzBEMuqHloGsFT/EdfKT0qQ");

		// setting the client & a simple dynamo object to interact with
		// dynamoDB
		dynamoClient = new AmazonDynamoDBClient(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		dynamoClient.setRegion(usWest2);
		dynamoDB = new DynamoDB(dynamoClient);
		System.out.println("Connected to DynamoDB");

		// 获取数据库中所有的表
		// Iterator<?> it = dynamoDB.listTables().iterator();
		// while (it.hasNext()) {
		// System.out.println(it.next());
		// }

		// 根据表名称获取表
		// Table table = new Table(dynamoClient, "URL_Table");
		// System.out.println(table.getTableName());

	}
	
	
	public static void main(String[] args) {
		DynamoAPI api = new DynamoAPI();
		api.connectToDB();

//		api.readItemsByRequest2("urltable2", "node1");		
//		api.updateItem("urltable2", "node1", "165e8824-113a-496d-96e6-819bf2176d1b", "error");

//		api.deleteTable("urltable2");
//		api.createURLTable2("urltable2");
//		api.insertItemsIntoTable2(2, "urltable2");
//		api.readItems("urltable2");
		
//		api.deleteTable("resulttable2");
//		api.createResultTable2("resulttable2");
//		api.insertItemsIntoResultTable2("resulttable2", "1", "02d34fa8-d37e-4dbf-bdcc-36839c4a9581", "this is the result", "no error");
//		api.readItems("resulttable2");
		
//		api.deleteTable("urltable2");
//		api.createURLTable2("urltable2");
//		api.insertOneItemIntoUrlTable2("urltable2", "node1", "https://scholar.google.com/citations?user=nQRs24kAAAAJ&hl=en", "false");
//		api.insertOneItemIntoUrlTable2("urltable2", "node1", "https://scholar.google.com/citations?user=ge6zzwIAAAAJ&hl=en", "false");
//		api.insertOneItemIntoUrlTable2("urltable2", "node1", "https://scholar.google.com/citations?user=VGoSakQAAAAJ", "false");
//		api.readItems("urltable2");
		
//		api.deleteTable("GoogleScholarCheckTable");
//		api.createCoauthorTable("GoogleScholarCheckTable");
//		api.insertOneItemIntoGoogleScholarCheckTable("hahaha");
//		api.readGoogleScholarCheckTableItems("GoogleScholarCheckTable");
//		System.out.println(api.checkCoauthorName("hahaha"));
//		System.out.println(api.checkCoauthorName("heihei"));
		
	}
}
