package io.infolayer.aida.executor.runnable;

import java.util.ArrayList;
import java.util.List;

public class JdbcRunMetadata {
	
	private List<String> queries;
	private String connectionUri;
	private String conenctionDriver;
	private String beanClass;

	public JdbcRunMetadata() {
		this.queries = new ArrayList<String>();
	}
	
	public void addQuery(String query) {
		this.queries.add(query);
	}
	
	public List<String> getQueries() {
		return queries;
	}

	public String getConnectionUri() {
		return connectionUri;
	}

	public void setConnectionUri(String connectionUri) {
		this.connectionUri = connectionUri;
	}

	public String getConenctionDriver() {
		return conenctionDriver;
	}

	public void setConenctionDriver(String conenctionDriver) {
		this.conenctionDriver = conenctionDriver;
	}
	
	public String getBeanClass() {
		return beanClass;
	}
	
	public void setBeanClass(String beanClass) {
		this.beanClass = beanClass;
	}
	
	

	
}
