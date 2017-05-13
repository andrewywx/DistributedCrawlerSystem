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
	// ������Ŵ�coauthor�ı�������coauthor
	public void createCoauthorTable(String tablename) {
		try{
			System.out.println("Attempting to create table; please wait...");
	        
	        //������ֻ�з�����ʱ�Ľ���ʽ
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
	
	//�������URL�ı�urltable,������nodeid��������uuid
	public void createURLTable(String tableName){
        try {
            System.out.println("Attempting to create table; please wait...");
            
            //������������ʱ���������ɷ����������������ʱ�Ľ���ʽ
            Table table = dynamoDB.createTable(tableName,
                    Arrays.asList(
                        new KeySchemaElement("nodeID", KeyType.HASH),  //Partition key
                        new KeySchemaElement("UUID", KeyType.RANGE)), //Sort key
                        Arrays.asList(
                            new AttributeDefinition("nodeID", ScalarAttributeType.S),
                            new AttributeDefinition("UUID", ScalarAttributeType.S)), 
                        new ProvisionedThroughput(10L, 10L));
            
            //������ֻ�з�����ʱ�Ľ���ʽ
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
	
	//�������URL�ı�urltable2,������uuid��������nodeid
	//�������URL�ı�urltable2,������uuid��������nodeid
	public void createURLTable2(String tableName){
        try {
            System.out.println("Attempting to create table; please wait...");
            
            //������������ʱ���������ɷ����������������ʱ�Ľ���ʽ
            Table table = dynamoDB.createTable(tableName,
                    Arrays.asList(
                        new KeySchemaElement("UUID", KeyType.HASH),  //Partition key
                        new KeySchemaElement("nodeID", KeyType.RANGE)), //Sort key
                        Arrays.asList(
                            new AttributeDefinition("UUID", ScalarAttributeType.S),
                            new AttributeDefinition("nodeID", ScalarAttributeType.S)), 
                        new ProvisionedThroughput(10L, 10L));
            
            //������ֻ�з�����ʱ�Ľ���ʽ
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
	
	// �������URL�ı�resulttable,������nodeid��������uuid
	
	//������Ž���ı�resulttable,������nodeid��������uuid
	public void createResultTable(String tableName){
		try {
            System.out.println("Attempting to create table; please wait...");
            
            //������������ʱ���������ɷ����������������ʱ�Ľ���ʽ
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
	
	// �������URL�ı�resulttable2,������uuid��������nodeid

	//������Ž���ı�resulttable2��������uuid��������nodeid
	public void createResultTable2(String tableName) {
		try {
			System.out.println("Attempting to create table; please wait...");

			// ������������ʱ���������ɷ����������������ʱ�Ľ���ʽ
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
	
	// ��urltable���в���ָ�������ļ�¼
	
	//��������nodeid��������uuid��urltable�в����¼
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
    
    // ��urltable2���в���ָ�������ļ�¼
	
    //��������uuid��������nodeid��urltable2�в����¼
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
    
    // ��GoogleScholarCheckTable���в���һ����¼����ʾһ��coauthor������
    
    //�� GoogleScholarCheckTable���в���һ����¼
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
    
    // ���GoogleScholarCheckTable�����Ƿ����ָ�����ֵ�coauthor
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
 			for (Map<String, AttributeValue> item : result.getItems()) { //����ܽ������forѭ���У��ͱ���GoogleScholarCheckTable���д����������
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
    
    // ��urltable2���в���һ���¼
    //��urltable2�в���һ����¼
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
    
    // ��resulttable���в���һ����¼
      
    //��resulttable�в����¼
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
    
	// resulttable2���в���ָ�������ļ�¼
       
    //��resulttable2�в����¼
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
    
    
	// ��ȡ���е��������ݲ���ӡ
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
	
	// ��ȡGoogleScholarCheckTable�������е�����
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
	
	// ��urltable2�и���nodeid��ֵ����ȡ��¼��urltable2������Ϊuuid��������nodeid�ı�
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
	
	// ��urltable2�и���nodeid��ֵ����ȡ״̬λΪfalse�ļ�¼�������������ThreadsManage�е�urlsToCrawl�У�Ȼ�󽫶�ȡ���ļ�¼��״̬λ�޸�Ϊtrue
	
	//��urltable2�и���nodeid��ֵ����ȡ��¼��������¼����ThreadsManage��urlsToCrawl��
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

				if (urlstatus.equals("false")){  //״̬λΪfalse��url�ǻ�δ����ArrayList�е�url��ÿһ���Ӽ��һ�ΰ����Ƿ���ArrayList�У�����״̬λ�޸�Ϊtrue����ʾ����ȡ
					urlsToCrawl.add(UUID + " " + nodeID + " " + url);
					updateItem("urltable2", nodeID, UUID, "true");
				}
			}
		} catch (Exception e) {
			System.err.println("Unable to query");
			System.err.println(e.getMessage());
		}
		System.out.println("urlsToCrawl����:" + urlsToCrawl.size());
	}
	

	//������������ѯ���еļ�¼
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
	
	// �޸�urltable2���е�״̬λ����������״̬λ��false��ʾ���л�û�ж�������url��true��ʾ�Ѷ�������û����ȡ��url��complete��ʾ�Ѿ������ȡ��url��error��ʾ�������������url
	
	//����urltable2���е�url��״̬λ��״̬λ��Ϊ���֣�false��ʾδ��ȡ�ģ�true��ʾ��ȡ�Ļ�δ����ģ�complete��ʾ�������ģ�error��ʾ������ִ����url
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
	
	// ���ݱ���ɾ����
	
	//ɾ�������еļ�¼
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

		// ��ȡ���ݿ������еı�
		// Iterator<?> it = dynamoDB.listTables().iterator();
		// while (it.hasNext()) {
		// System.out.println(it.next());
		// }

		// ���ݱ����ƻ�ȡ��
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
