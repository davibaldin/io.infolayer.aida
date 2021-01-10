//package io.infolayer.siteview.plugins.handler;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import io.infolayer.siteview.exception.OutputHandlerException;
//import io.infolayer.siteview.plugin.IPluginOutputHandler;
//import io.infolayer.siteview.plugin.OutputFlow;
//
//@Deprecated
//public class FilesystemOutputPluginHandler implements IPluginOutputHandler {
//	
//	private static Logger log = LoggerFactory.getLogger(FilesystemOutputPluginHandler.class);
//	
//	@Override
//	public void prepare() throws OutputHandlerException {
//		// TODO Auto-generated method stub
//		
//	}
//	
//	@Override
//	public boolean isPrepared() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	
//	@Override
//	public void proccess(OutputFlow flow, boolean abort) throws OutputHandlerException {
//		
//		if (flow == null) {
//			return;
//		}
//		
//		int stdout = 0;
//		InputStream input = null;
//
//		try {
//			input = flow.getLatestContent();
//			stdout = input.available();
//		} catch (Exception e) {
//			throw new OutputHandlerException(e.getMessage());
//		}
//
//		if (log.isDebugEnabled()) {
//			log.debug("----------------- FilesystemOutputPluginHandler -----------------");
//			log.debug("Bytes: " + stdout);	
//		}
//	    
//		if (stdout > 0) {
//			
//			File outputFile = new File(this.getRootDirectory(), "out");
//			if (log.isDebugEnabled()) {
//				log.debug("  Pipe file: " + outputFile.getPath());
//			}
//		    try {
//				Files.copy(input, outputFile.toPath());
//			} catch (IOException e) {
//				throw new OutputHandlerException(e.getMessage());
//			}
//		    
//		    try {
//				//return new FileInputStream(outputFile);
//			} catch (Exception e) {
//				throw new OutputHandlerException(e.getMessage());
//			}
//		}
//		
//		//return null;
//	}
//	
////	@Override
////	public void processChain(OutputFlow flow, Iterator<IPluginOutputHandler> chain, boolean abortChain) throws OutputHandlerException {
////		if (abortChain) {
////			return;
////		}
////		
////		this.proccess(flow, abortChain);
////		
////		if (chain.hasNext()) {
////			chain.next().proccess(flow, abortChain);
////		}
////	}
//
////	@Override
////	public void processOutput(InputStream isSucess, InputStream isError, int exit) throws OutputHandlerException {
////
////		int stdout = 0;
////		int stderr = 0;
////
////		try {
////			stdout = isSucess.available();
////			stderr = isError.available();
////		} catch (IOException e) {
////			throw new OutputHandlerException(e.getMessage());
////		}
////
////		if (log.isDebugEnabled()) {
////			log.debug("----------------- FilesystemOutputPluginHandler -----------------");
////			log.debug("Process Exit: " + exit + ", stdout: " + stdout + ", stderr: " + stderr);	
////		}
////	    
////		if (stdout > 0) {
////			
////			File outputFile = new File(this.getRootDirectory(), "isSucess.out");
////			if (log.isDebugEnabled()) {
////				log.debug("  Stdout file: " + outputFile.getPath());
////			}
////		    try {
////				Files.copy(isSucess, outputFile.toPath());
////			} catch (IOException e) {
////				throw new OutputHandlerException(e.getMessage());
////			}
////		}
////
////		if (stderr > 0) {
////			File outputFile = new File(this.getRootDirectory(), "isError.out");
////			if (log.isDebugEnabled()) {
////				log.debug("  Stderr file: " + outputFile.getPath());
////			}
////			try {
////				Files.copy(isError, outputFile.toPath());
////			} catch (IOException e) {
////				throw new OutputHandlerException(e.getMessage());
////			}
////		}
////
////		if (log.isDebugEnabled()) {
////			log.debug("----------------- FilesystemOutputPluginHandler -----------------");
////		}
////	}
//	
//	
//	/**
//	 * Root directory is returned by 
//	 * @return
//	 * @throws OutputHandlerException 
//	 */
//	private File getRootDirectory() throws OutputHandlerException {
//		
//		//Get property system properties
//		String root = System.getProperty("siteview.plugins.working", null);
//		String instance = System.getProperty("server.instance.uuid", null);
//		
//		if (root == null || instance == null) {
//			throw new OutputHandlerException("NULL Working directory or server instance.");
//		}
//		
//		File rootFile = new File(root);
//		if (!rootFile.exists() || !rootFile.canWrite()) {
//			throw new OutputHandlerException(" Working directory is not writeable.");
//		}
//		
//		String data[] = instance.split("-");
//		
//		File rootDirectory = new File(rootFile, "output" + File.separator + data[0]);
//		if (!rootDirectory.exists()) {
//			if (!rootDirectory.mkdirs()) {
//				throw new OutputHandlerException("Unable to create output directory: " + rootDirectory.getAbsolutePath());
//			}
//		}
//		
//		return rootDirectory;
//	}
//	
//}
