//package io.infolayer.siteview.plugins.handler;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import io.infolayer.siteview.annotation.ConfiguredParameter;
//import io.infolayer.siteview.exception.OutputFlowException;
//import io.infolayer.siteview.exception.OutputHandlerException;
//import io.infolayer.siteview.plugin.IPluginOutputHandler;
//import io.infolayer.siteview.plugin.OutputFlow;
//
//public class FileReader implements IPluginOutputHandler {
//
//	private static Logger log = LoggerFactory.getLogger(FileReader.class);
//	
//	@ConfiguredParameter
//	public String filename = null;
//	
//	private boolean prepared = false;
//	
//	@Override
//	public void prepare() throws OutputHandlerException {
//		
//		if (filename == null || "".equals(filename)) {
//			log.error("Unable to read file {}", filename);
//			prepared = false;
//		}else {
//			prepared = true;
//		}
//		
//	}
//
//	@Override
//	public boolean isPrepared() {
//		return prepared;
//	}
//
//	@Override
//	public void proccess(OutputFlow flow, boolean abortNext) throws OutputHandlerException {
//		
//		if (this.isPrepared()) {
//			try {
//				flow.loadContent(new FileInputStream(this.getFile(flow)));
//			} catch (Exception e) {
//				abortNext = true;
//				throw new OutputHandlerException(e);
//			}
//		}
//
//	}
//	
//	private File getFile(OutputFlow flow) throws OutputHandlerException, IOException, OutputFlowException {
//		
//		File file = new File(flow.getWorkingDirectory(), this.filename);
//		if (!file.exists() || !file.canRead()) {
//			throw new OutputHandlerException("Unable to read file " + file.getCanonicalPath());
//		}
//		
//		return file;
//		
//	}
//
//}
