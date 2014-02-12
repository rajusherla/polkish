package com.polaris.batch.SWInsertTrainers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.NamingException;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;

import com.ibm.batch.api.CIWork;
import com.polaris.batch.commons.DataSourceConnection;
import com.polaris.batch.constants.IConstants;

public class SWInsertTrainersJobAction implements CIWork,IConstants {
	java.util.Map properties = null;
	boolean release = false;
	private static Connection sqlconn;
	private static Statement sqlStmnt;
	private static PreparedStatement sqlPstmnt;

	static Logger logger = Logger.getLogger(SWInsertTrainersJobAction.class
			.getName());
	Properties prop = new Properties();
	InputStream input = null;

	@Override
	public Map getProperties() {
		return this.properties;
	}

	@Override
	public void setProperties(Map properties) {

		this.properties = properties;
	}

	@Override
	public boolean isDaemon() {
		return true;
	}

	@Override
	public void release() {
		release = true;

	}

	@Override
	public void run() {
		logger.info("Job started");
		boolean wr529f_SizeCheck = false;
		try {
			input = getClass().getClassLoader().getResourceAsStream(
					(String) properties.get(RESOURCE_FILE));
			prop.load(input);
			//step:1
			String wr529ffFile = (String) prop
					.getProperty(SWINSERTTRAINERS_FILEPATH);
			String processedwr529fFile = (String) prop
					.get(SWINSERTTRAINERS_UPLOADDEST1);
			wr529f_SizeCheck = validateFile(wr529ffFile, processedwr529fFile);
			if(wr529f_SizeCheck==true){
				DataSourceConnection dataSourceConnection = new DataSourceConnection();
				sqlconn = dataSourceConnection.getConnecttion(prop
						.getProperty(SWINSERTTRAINERS_SQLDS));
				sqlconn.setAutoCommit(false);
				sqlStmnt = sqlconn.createStatement();
				//step:2
				excecuteStep2();
				//step:3
				excecuteStep3();
				//step:4
				excecuteStep4();
				sqlconn.commit();
			}else{
				sendMail((String) prop.getProperty(SWINSERTTRAINERS_FROM),(String) prop.getProperty(SWINSERTTRAINERS_TO),(String) prop.getProperty(SWINSERTTRAINERS_HOST),(String) prop.getProperty(SWINSERTTRAINERS_SUB1),"");
			}
			logger.info("Job ended");
		} catch (IOException e) {
			logger.error(e, e);
		} catch (AddressException e) {
			logger.error(e, e);
		} catch (EmailException e) {
			logger.error(e, e);
		} catch (NamingException e) {
			logger.error(e, e);
		} catch (SQLException e) {
			try {
				sqlconn.rollback();
			} catch (SQLException e1) {
				logger.error(e1, e1);
			}
			logger.error(e, e);
		} catch (Exception e) {
			logger.error(e, e);
		}finally {

			if (sqlStmnt != null) {
				try {
					if (!sqlStmnt.isClosed()) {
						sqlStmnt.close();
					}
				} catch (SQLException e) {
					logger.error(e, e);
				}
			}
			if (sqlPstmnt != null) {
				try {
					if (!sqlPstmnt.isClosed()) {
						sqlPstmnt.close();
					}
				} catch (SQLException e) {
					logger.error(e, e);
				}
			}
			if (sqlconn != null) {
				try {
					if (!sqlconn.isClosed()) {
						sqlconn.close();
					}
				} catch (SQLException e) {
					logger.error(e, e);
				}
			}
			
		}	
	}
	private boolean validateFile(String file1, String file2) {
		boolean check = false;
		File f1 = new File(file1);
		File f2 = new File(file2);
		if (f1.exists() && f2.exists()) {
			double f1Size = f1.length();
			double f2Size = f2.length();
			if (f1Size < 0.8 * f2Size || f1Size > 1.2 * f2Size) {
				check = false;
			} else {
				check = true;
			}
		} else if (f1.exists()) {
			check = true;
		} else {
			logger.info(file1 + " file not exist");
		}
		return check;
	}
	private void sendMail(String from,String to,String host,String subject,String content) throws AddressException, EmailException {
		MultiPartEmail email = new HtmlEmail();
		email.setFrom(from);
		email.setHostName(host);
		List<InternetAddress> rec = new ArrayList<InternetAddress>();
		String[] toaddress = to.split(",");
		for (int i = 0; i < toaddress.length; i++) {
			rec.add(new InternetAddress(toaddress[i]));
		}
		email.setTo((Collection<InternetAddress>) rec);
		email.setSubject(subject);
		email.setMsg(content);
		email.send();
	}
	//step:2
	private void excecuteStep2() throws Exception {
		try {
			sqlStmnt.executeUpdate((String)prop.getProperty(SWINSERTTRAINERS_QUERY1));
		} catch (Exception e) {
			sendMail((String) prop.getProperty(SWINSERTTRAINERS_FROM),(String) prop.getProperty(SWINSERTTRAINERS_TO),(String) prop.getProperty(SWINSERTTRAINERS_HOST),(String) prop.getProperty(SWINSERTTRAINERS_SUB2),(String) prop.getProperty(SWINSERTTRAINERS_MESG2));
			throw e;
		}

	}
	//step:3
	private void excecuteStep3() throws Exception {
		try {
			FileInputStream fstream = new FileInputStream(
					(String) prop.getProperty(SWINSERTTRAINERS_FILEPATH));
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			PreparedStatement sqlPstmnt = null;
			final int batchSize = 1000;
			int count = 0;
			sqlPstmnt = sqlconn.prepareStatement((String) prop
					.getProperty(SWINSERTTRAINERS_QUERY2));
			while ((strLine = br.readLine()) != null) {
				sqlPstmnt.setString(1, strLine.substring(2, 11));
				sqlPstmnt.setString(2, strLine.substring(12,24));
				sqlPstmnt.addBatch();
				if (++count % batchSize == 0) {
					sqlPstmnt.executeBatch();
				}
			}
			sqlPstmnt.executeBatch();
			in.close();
		} catch (Exception e) {
			sendMail((String) prop.getProperty(SWINSERTTRAINERS_FROM),(String) prop.getProperty(SWINSERTTRAINERS_TO),(String) prop.getProperty(SWINSERTTRAINERS_HOST),(String) prop.getProperty(SWINSERTTRAINERS_SUB3),"");
			throw e;
		}
	}
	private void moveFile(String source, String dest) {
		File file = new File(dest);
		if (file.exists()) {
			file.delete();
		}
		File afile = new File(source);
		afile.renameTo(new File(dest));
	}
	//step:4
	private void excecuteStep4() throws Exception {
		try {
			moveFile((String) prop.getProperty(SWINSERTTRAINERS_UPLOADDEST1),
					(String) prop.getProperty(SWINSERTTRAINERS_UPLOADDEST2));
			moveFile((String) prop.getProperty(SWINSERTTRAINERS_FILEPATH),
					(String) prop.getProperty(SWINSERTTRAINERS_UPLOADDEST1));
		} catch (Exception e) {
			sendMail((String) prop.getProperty(SWINSERTTRAINERS_FROM),(String) prop.getProperty(SWINSERTTRAINERS_TO),(String) prop.getProperty(SWINSERTTRAINERS_HOST),(String) prop.getProperty(SWINSERTTRAINERS_SUB4),"");
			throw e;
		}

	}
}
