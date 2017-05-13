import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 
//import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.*;

public class FTP {
	FTPClient ftpClient = null;

	public FTP() {

	}

	public boolean connect(String ip, String user, String pass)
			throws Exception {
		if (ftpClient != null) {
			if (ftpClient.isConnected()) {
				ftpClient.logout();
				ftpClient.disconnect();
				ftpClient = null;
			}
		}

		ftpClient = new FTPClient();
		ftpClient.connect(ip, 21);
		boolean res = ftpClient.login(user, pass);
		System.out.println("Login res: " + res);
		// ftpClient.enterLocalPassiveMode();
		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

		System.out.println("Connected: " + ftpClient.isConnected());
		return res;
	}

	// note this method will overwrite the remote file
	public boolean upload(String localPath, String remotePath) throws Exception {
		if (ftpClient == null || !ftpClient.isConnected()) {
			System.out.println("ftpClient is null or not connected");
			return false;
		}

		File localFile = new File(localPath);
		if (!localFile.exists() || localFile.isDirectory()) {
			System.out.println("local file does not exist or is a directory");
			return false;
		}

		InputStream inputStream = new FileInputStream(localFile);
		System.out.println("Uploading");
		boolean done = ftpClient.storeFile(remotePath, inputStream);
		ftpClient.sendSiteCommand("chmod " + "666 " + remotePath);

		System.out.println("Upload succeeded");
		inputStream.close();

		return done;
	}

	public FTPFile[] list(String path) throws Exception {
		// System.out.println("Connected: "+ftpClient.isConnected());

		if (ftpClient == null || !ftpClient.isConnected()) {
			System.out.println("Client is null or not connected");
			return null;
		}

		FTPFile[] files = ftpClient.listFiles(path);

		System.out.println("files number:" + files.length);
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i]);
		}

		return files;
	}

	public boolean checkDirectoryExists(String dirPath) throws IOException {
		if (ftpClient == null || !ftpClient.isConnected()) {
			System.out.println("Client is null or not connected");
			return false;
		}

		ftpClient.changeWorkingDirectory(dirPath);
		int returnCode = ftpClient.getReplyCode();
		if (returnCode == 550) {
			return false;
		}

		return true;
	}

	public boolean checkFileExists(String filePath) throws IOException {
		if (ftpClient == null || !ftpClient.isConnected()) {
			System.out.println("Client is null or not connected");
			return false;
		}

		InputStream inputStream = ftpClient.retrieveFileStream(filePath);
		int returnCode = ftpClient.getReplyCode();
		if (inputStream == null || returnCode == 550) {
			return false;
		}
		return true;
	}

	public void disconnect() throws Exception {
		if (ftpClient != null) {
			if (ftpClient.isConnected()) {
				ftpClient.logout();
				ftpClient.disconnect();
				ftpClient = null;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		FTP test = new FTP();

		test.connect("52.87.235.182", "rdpuser", "rdpuser");
//		FTPFile[] files = test.list("/home/rdpuser/AmazonCrawlerNode");
//		for(FTPFile file : files)
//			System.out.println(file.getName());
		
		System.out.println(test.checkDirectoryExists("/home/rdpuser"));
		System.out.println(test.checkDirectoryExists("/home/ubuntu"));
		System.out.println(test.checkDirectoryExists("/home/uhahaha"));

		//test.upload("/home/ywx/WebdriverTest.java","/home/rdpuser/hahaha"); //hahaha是把本地文件以二进制的形式传过去之后保存在了hahaha这个文件中。
//		test.upload("D:\\EclipseProject\\RabbmitMQ\\bin\\CrawlerURL.class", "/home/rdpuser/AmazonCrawlerNode/CrawlerURL.class");
//		test.upload("D:\\EclipseProject\\RabbmitMQ\\bin\\CustomClassLoader.class", "/home/rdpuser/AmazonCrawlerNode/CustomClassLoader.class");
//		test.upload("D:\\EclipseProject\\RabbmitMQ\\bin\\DynamoAPI.class", "/home/rdpuser/AmazonCrawlerNode/DynamoAPI.class");
//		test.upload("D:\\EclipseProject\\RabbmitMQ\\bin\\Kernel.class", "/home/rdpuser/AmazonCrawlerNode/Kernel.class");
//		test.upload("D:\\EclipseProject\\RabbmitMQ\\bin\\ThreadsManage.class", "/home/rdpuser/AmazonCrawlerNode/ThreadsManage.class");
		test.upload("D:\\Reqv.class", "/home/rdpuser/AmazonCrawlerNode/Reqv.class");


		test.disconnect();
	}

}
