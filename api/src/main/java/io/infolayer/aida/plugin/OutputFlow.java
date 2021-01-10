package io.infolayer.aida.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.exception.OutputFlowException;
import io.infolayer.aida.exception.OutputHandlerException;
import io.infolayer.aida.utils.PlatformUtils;

/**
 * OutputFlow holds data and messages through plugin execution flow.
 * @author davi@infolayer.io
 *
 */
public class OutputFlow {
	
	/**
	 * In case of object being passed into flow, this property holds attribute name.
	 */
	public static final String FLOW_ITEM_NAME = "_flow_item_name";
	
	private static Logger log = LoggerFactory.getLogger(OutputFlow.class);
	private final String uuid;
	private final String instanceId;
	private final Map<String, Object> properties;
	private final Map<String, String> environment;
	private final List<File> content;
	private final Vector<IRunnablePlugin> threads;
	private long start;
	private long files;
	private long instances;
	private long props;
	private long envs;
	
	//Lazy by default
	private final String parent;
	private File working = null;
	
	/**
	 * Create a new instance.
	 * @param parentDirectory
	 * @return
	 * @throws OutputFlowException
	 */
	public static OutputFlow newInstance(String parentDirectory) throws OutputFlowException {	
		return new OutputFlow(parentDirectory);
	}

	/**
	 * Create a new instance.
	 * @param parentDirectory
	 * @throws OutputFlowException
	 */
	private OutputFlow(String parentDirectory) throws OutputFlowException {
		
		this.start = System.currentTimeMillis();
		this.files = 0l;
		this.instances = 0l;
		this.props = 0l;
		this.envs = 0l;
		
		this.uuid = UUID.randomUUID().toString();
		this.instanceId = newRandomString();
		
		//Lazy loading
		//this.working = createNewWorkingDirectory(parentDirectory);
		this.parent = parentDirectory;
		
		this.properties = new HashMap<String, Object>();
		this.environment = new HashMap<String, String>();
		this.content = new LinkedList<File>();
		this.threads = new Vector<IRunnablePlugin>();
		
		log.info(MessageFormat.format("New OutputFlow UUID: {0}, Instance: {1}", uuid, instanceId));
		
	}
	
	public static String newRandomString() {
		return PlatformUtils.getAlphaNumericString(3) + (System.currentTimeMillis() - 1576359159231l); //14/12/2019 18:32:39
	}
	
	public void attach(IRunnablePlugin instance) {
		if (instance != null) {
			this.threads.add(instance);
			this.instances++;
			log.debug("Attached instance {}, count = {}", instance, instances);
		}
	}
	
	public void detach(IRunnablePlugin instance) {
		if (instance != null) {
			this.threads.remove(instance);
			this.instances--;
			log.debug("Attached instance {}, count = {}", instance, instances);
		}
		
		if (this.threads.isEmpty()) {
			try {
				this.finish();
			} catch (OutputFlowException e) {
				log.error(MessageFormat.format("Exception while finishing OutputFlow in detach: {0}", e.getMessage()));
			}
		}
	}

	public int loadContent(InputStream input) throws OutputFlowException {
		
		File outputFile = new File(getWorkingDirectory(), "content-" + this.getNextContentIndex());
		
		if (log.isDebugEnabled()) {
			log.debug(MessageFormat.format("OutputFlow loading content {0}.", outputFile.getAbsolutePath()));
		}
		
		try {
			Files.copy(input, outputFile.toPath());
			this.content.add(outputFile);
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			throw new OutputFlowException(e);
		}
		
		this.files++;
		
		return this.content.size();
	}

	public void addProperty(String key, Object value) {
		this.properties.put(key, value);
		this.props++;
	}

	public void addAllProperties(Map<String, Object> props) {
		if (props != null) {
			this.properties.putAll(props);
			this.props = this.props + props.size();
		}
	}
	
	public Map<String, Object> getPropoerties() {
		return this.properties;
	}
	
	public Map<String, String> getEnvironment() {
		return this.environment;
	}
	
	public void addEnvironment(String key, String value) {
		this.environment.put(key, value);
		this.envs++;
	}

	public void addAllEnvironments(Map<String, String> envs) {
		if (envs != null) {
			this.environment.putAll(envs);
			this.envs = this.envs + envs.size();
		}
	}

	public InputStream getContent(int index) throws OutputFlowException {
		
		File item = this.content.get(index);
		
		if (item != null) {
			try {
				return new FileInputStream(item);
			} catch (FileNotFoundException e) {
				throw new OutputFlowException("Content not available at index " + index);
			}
		}
		
		return null;
	}
	
	public InputStream getLatestContent() throws OutputFlowException {
		
		if (!this.content.isEmpty()) {
			
			try {
				
				if (log.isDebugEnabled()) {
					log.debug("About to return FileInputStream of " + this.content.get(this.getNextContentIndex()-1));
				}
				
				return new FileInputStream(this.content.get(this.getNextContentIndex()-1));
			} catch (FileNotFoundException e) {
				throw new OutputFlowException("Content not available. Concrete file not found.");
			}
		
		}
		
		log.debug("About to return null InputStream.");
		return null;
	}
	
	public synchronized void finish() throws OutputFlowException {
		
		if (log.isDebugEnabled() && working != null) {
			log.debug("Flow finish() DEBUG IS ENABLED !!! - Keeping files. Working directory is: {}", working.getAbsolutePath());
			return;
		}
		
		try {
			
			this.content.clear();
			this.environment.clear();
			this.properties.clear();
			this.threads.clear();
			
			if (this.working != null) {
				this.deleteDirectory(getWorkingDirectory());
			}
			
		} catch (OutputFlowException e) {
			throw new OutputFlowException("Flow finish exception: " + e.getMessage());
		}
		
		log.info(MessageFormat.format("Flow finish. Instance {0}, uuid {1}, elapsed {2}, files {3}, instances {4}, properties {5}, environment {6} ", 
				instanceId,
				uuid,
				(System.currentTimeMillis() - start),
				files,
				instances,
				props,
				envs));
	}
	
	private int getNextContentIndex() {
		return this.content.size();
	}

	/**
	 * Return the working directory.
	 * @return
	 * @throws OutputFlowException if directory does not exist.
	 */
	private synchronized File getWorkingDirectory() throws OutputFlowException {
		
		if (this.working == null) {
			this.working = createNewWorkingDirectory();
			return this.working;
		} else {
			
			if (working.exists()) {
				return this.working;
			}
		}
		
		throw new OutputFlowException("OutputFlow directory not existent or other unknown state.");
	}
	
	/**
	 * Create new OutputFlow directory infrastructure.
	 * @return
	 * @throws OutputHandlerException 
	 */
	private File createNewWorkingDirectory() throws OutputFlowException {
		
		if (this.parent == null || "".equals(this.parent)) {
			throw new OutputFlowException("Unable to create new OutputFlow directory. parentDirectory cannot be null or empty.");
		}
		
		File rootFile = new File(this.parent);
		if (!rootFile.exists() || !rootFile.canWrite()) {
			throw new OutputFlowException("Root directory [" + this.parent + "] is not writeable or does not exist.");
		}
		
		if (!rootFile.isDirectory()) {
			throw new OutputFlowException("Root directory [" + this.parent + "] must be a directory.");
		}
		
		File working = new File(rootFile, this.uuid);
		
		if (!working.exists()) {
			if (!working.mkdirs()) {
				throw new OutputFlowException("Unable to create output directory: " + working.getAbsolutePath());
			}
		}
		
		return working;
	}
	
	/**
	 * Delete directory and all its content. Be careful: You can hurt yourself calling in the wrong directory.
	 * @param directoryToBeDeleted
	 * @return
	 */
	private boolean deleteDirectory(File directoryToBeDeleted) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
	    return directoryToBeDeleted.delete();
	}

	/**
	 * Get file from Working directory.
	 * @param filename Filename relative to the Working directory.
	 * @return
	 * @throws OutputFlowException 
	 */
	public File getFile(String filename) throws OutputFlowException {
		return new File(this.getWorkingDirectory(), filename);
	}

}