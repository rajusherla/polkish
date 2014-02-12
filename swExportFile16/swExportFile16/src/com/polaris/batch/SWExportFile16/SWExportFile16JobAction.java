package com.polaris.batch.SWExportFile16;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.ibm.batch.api.CIWork;
import com.polaris.batch.commons.DataSourceConnection;
import com.polaris.batch.constants.IConstants;

/**
 * @author Kishore.
 * 
 */
public class SWExportFile16JobAction implements CIWork, IConstants {
	java.util.Map properties = null;
	boolean release = false;
	private static Connection sqlconn;
	private static Connection as400conn;
	private static PreparedStatement sqlpstmnt;
	private static PreparedStatement as400pstmnt;
	static Logger logger = Logger.getLogger(SWExportFile16JobAction.class
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

		try {
			input = getClass().getClassLoader().getResourceAsStream(
					(String) properties.get(RESOURCE_FILE));
			prop.load(input);
			String fileName;
			// Date format
			SimpleDateFormat filedateFormat = new SimpleDateFormat(
					prop.getProperty(SWEXPORTFILE16_FIlE_DATE_FORMAT));
			DateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD);
			Calendar cal = Calendar.getInstance();
			fileName = "File 16 " + filedateFormat.format(cal.getTime())
					+ ".txt";
			cal.add(Calendar.DATE, -1);
			String ydate = dateFormat.format(cal.getTime());
			String startDate = ydate + FIRSTHOUR;
			// String startDate ="2014-01-12T00:00:00.000";
			// String startDateINT ="2014-01-11T00:00:00.000";
			String endDate = ydate + LASTHOUR;
			// String endDate ="2014-01-12T23:59:59.000";
			cal.add(Calendar.DATE, -1);
			String dydate = dateFormat.format(cal.getTime());
			String startDateINT = dydate + FIRSTHOUR;
			String endDateINT = dydate + LASTHOUR;
			// String endDateINT ="2014-01-11T23:59:59.000";
			String strTransactionCode;
			String strWarrantyID;
			String strSerialNumber;
			String strUserName;
			String dtCancelledTime;
			String dtDateCompleted;

			DataSourceConnection dataSourceConnection = new DataSourceConnection();

			sqlconn = dataSourceConnection.getConnecttion(prop
					.getProperty(SWEXPORTFILE16_SQLDS));
			as400conn = dataSourceConnection.getConnecttion(prop
					.getProperty(SWEXPORTFILE16_AS400DS));
			// Step 1.
			sqlpstmnt = sqlconn.prepareStatement(prop
					.getProperty(SWEXPORTFILE16_QUERY1));
			sqlpstmnt.setString(1, startDate);
			sqlpstmnt.setString(2, endDate);
			sqlpstmnt.setString(3, startDateINT);
			sqlpstmnt.setString(4, endDateINT);
			ResultSet rs = sqlpstmnt.executeQuery();
			// insert query
			as400pstmnt = as400conn.prepareStatement(prop
					.getProperty(SWEXPORTFILE16_QUERY7));
			while (rs.next()) {
				strTransactionCode = rs.getString("TransactionCode");
				strWarrantyID = String
						.format("%7s", rs.getString("Warrantyid"));
				strSerialNumber = String.format("%-17s",
						rs.getString("SerialNumber").toUpperCase());
				strUserName = String.format("%-10s", rs.getString("UserName")
						.toUpperCase());
				String[] tempcDate = rs.getString("ValidatedTime").split(" ")[0]
						.split("-");
				dtCancelledTime = tempcDate[2] + tempcDate[1] + tempcDate[0];
				String[] tempcmDate = rs.getString("DateCompleted").split(" ")[0]
						.split("-");
				dtDateCompleted = tempcmDate[2] + tempcmDate[1] + tempcmDate[0];

				as400pstmnt.setString(1, strTransactionCode);
				as400pstmnt.setString(2, strWarrantyID);
				as400pstmnt.setString(3, strSerialNumber);
				as400pstmnt.setString(4, strUserName);
				as400pstmnt.setString(5, dtCancelledTime);
				as400pstmnt.setString(6, dtDateCompleted);
				as400pstmnt.executeUpdate();

			}
			// update query
			sqlpstmnt = sqlconn.prepareStatement(prop
					.getProperty(SWEXPORTFILE16_QUERY2));
			sqlpstmnt.setString(1, startDate);
			sqlpstmnt.setString(2, endDate);
			sqlpstmnt.setString(3, startDateINT);
			sqlpstmnt.setString(4, endDateINT);
			sqlpstmnt.executeUpdate();
			sqlpstmnt = sqlconn.prepareStatement(prop
					.getProperty(SWEXPORTFILE16_QUERY3));
			sqlpstmnt.setString(1, startDate);
			sqlpstmnt.setString(2, endDate);
			rs = sqlpstmnt.executeQuery();
			// insert query
			as400pstmnt = as400conn.prepareStatement(prop
					.getProperty(SWEXPORTFILE16_QUERY7));
			while (rs.next()) {
				strTransactionCode = rs.getString("TransactionCode");
				strWarrantyID = String
						.format("%7s", rs.getString("Warrantyid"));
				strSerialNumber = String.format("%-17s",
						rs.getString("SerialNumber").toUpperCase());
				strUserName = String.format("%-10s", rs.getString("UserName")
						.toUpperCase());
				String[] tempcDate = rs.getString("CancelledTime").split(" ")[0]
						.split("-");
				dtCancelledTime = tempcDate[2] + tempcDate[1] + tempcDate[0];
				String[] tempcmDate = rs.getString("DateCompleted").split(" ")[0]
						.split("-");
				dtDateCompleted = tempcmDate[2] + tempcmDate[1] + tempcmDate[0];
				as400pstmnt.setString(1, strTransactionCode);
				as400pstmnt.setString(2, strWarrantyID);
				as400pstmnt.setString(3, strSerialNumber);
				as400pstmnt.setString(4, strUserName);
				as400pstmnt.setString(5, dtCancelledTime);
				as400pstmnt.setString(6, dtDateCompleted);
				as400pstmnt.executeUpdate();
			}
			// update query
			sqlpstmnt = sqlconn.prepareStatement(prop
					.getProperty(SWEXPORTFILE16_QUERY4));
			sqlpstmnt.setString(1, startDate);
			sqlpstmnt.setString(2, endDate);
			sqlpstmnt.executeUpdate();
			// Step 2
			File file = new File(prop.getProperty(SWEXPORTFILE16_FILEARCHIVE)
					+ fileName);
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fileWritter = new FileWriter(
					prop.getProperty(SWEXPORTFILE16_FILEARCHIVE) + fileName,
					true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

			sqlpstmnt = sqlconn.prepareStatement(prop
					.getProperty(SWEXPORTFILE16_QUERY5));
			sqlpstmnt.setString(1, startDate);
			sqlpstmnt.setString(2, endDate);
			rs = sqlpstmnt.executeQuery();
			while (rs.next()) {
				StringBuffer sb = new StringBuffer();
				sb.append(rs.getString("TransactionCode"));
				sb.append(String.format("%7s", rs.getString("Warrantyid")));
				sb.append(String.format("%-17s", rs.getString("SerialNumber")
						.toUpperCase()));
				sb.append(String.format("%-10s", rs.getString("UserName")
						.toUpperCase()));

				String[] tempcDate = rs.getString("ValidatedTime").split(" ")[0]
						.split("-");
				sb.append(tempcDate[2] + tempcDate[1] + tempcDate[0]);
				String[] tempcmDate = rs.getString("DateCompleted").split(" ")[0]
						.split("-");
				sb.append(tempcmDate[2] + tempcmDate[1] + tempcmDate[0]);
				bufferWritter.write(sb.toString() + "\n");

			}

			sqlpstmnt = sqlconn.prepareStatement(prop
					.getProperty(SWEXPORTFILE16_QUERY6));
			sqlpstmnt.setString(1, startDate);
			sqlpstmnt.setString(2, endDate);
			rs = sqlpstmnt.executeQuery();
			while (rs.next()) {
				StringBuffer sb = new StringBuffer();
				sb.append(rs.getString("TransactionCode"));
				sb.append(String.format("%7s", rs.getString("Warrantyid")));
				sb.append(String.format("%-17s", rs.getString("SerialNumber")
						.toUpperCase()));
				sb.append(String.format("%-10s", rs.getString("UserName")
						.toUpperCase()));

				String[] tempcDate = rs.getString("CancelledTime").split(" ")[0]
						.split("-");
				sb.append(tempcDate[2] + tempcDate[1] + tempcDate[0]);
				String[] tempcmDate = rs.getString("DateCompleted").split(" ")[0]
						.split("-");
				sb.append(tempcmDate[2] + tempcmDate[1] + tempcmDate[0]);
				bufferWritter.write(sb.toString() + "\n");

			}
			bufferWritter.close();
			sqlpstmnt.close();
			sqlconn.close();
			as400conn.close();
			logger.info("Job ended");
		} catch (IOException e) {
			logger.error(e, e);
		} catch (SQLException e) {
			logger.error(e, e);
		} catch (NamingException e) {
			logger.error(e, e);
		}  finally {

			if (as400pstmnt != null) {
				try {
					if (!as400pstmnt.isClosed()) {
						as400pstmnt.close();
					}
				} catch (SQLException e) {
					logger.error(e, e);
				}
			}
			
			if (sqlpstmnt != null) {
				try {
					if (!sqlpstmnt.isClosed()) {
						sqlpstmnt.close();
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
			if (as400conn != null) {
				try {
					if (!as400conn.isClosed()) {
						as400conn.close();
					}
				} catch (SQLException e) {
					logger.error(e, e);
				}
			}

		}
	}
}
