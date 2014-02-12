package com.polaris.batch.commons;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DataSourceConnection {
	
	private static DataSource ds = null;
	private static Connection conn;
	public Connection getConnecttion(String jndi_name) throws NamingException, SQLException{
		InitialContext ic = new InitialContext();
		ds = (DataSource) ic.lookup(jndi_name);
		conn = ds.getConnection();
		return conn;
	}
}
