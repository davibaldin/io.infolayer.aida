//package io.infolayer.siteview.plugins.handler;
//
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.util.Map;
//import java.util.Set;
//
//import io.infolayer.siteview.exception.OutputHandlerException;
//import io.infolayer.siteview.plugin.IPluginOutputHandler;
//import io.infolayer.siteview.plugin.OutputFlow;
//
//public class ConsoleOutputPluginHandler implements IPluginOutputHandler {
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
//		return true;
//	}
//
//	@Override
//	public void proccess(OutputFlow flow, boolean abort) throws OutputHandlerException {
//		
//		if (flow == null) {
//			return;
//		}
//		
//		InputStream input = null;
//		int stdout = 0;
//
//		try {
//			
//			input = flow.getLatestContent();
//			if (input != null) {
//				stdout = input.available();
//			}
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		System.out.println("----------------- ConsoleOutputPluginHandler -----------------");
//		System.out.println("Content lenght: " + stdout);
//
//		if (stdout > 0) {
//			System.out.println("Content >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//			dumpImputStream(input);
//			System.out.println("Content <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//		}
//		
//		Map<String, Object> props = flow.getPropoerties();
//		if (props != null) {
//			
//			System.out.println("Properties >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//			
//			Set<String> keySet = props.keySet();
//			for (String key : keySet) {
//				Object value =  props.get(key);
//				if (value != null) {
//					System.out.println(key + " -> [" + props.get(key).toString() + "]");
//				} else {
//					System.out.println(key + " -> [NULL]");
//				}	
//			}
//			
//			System.out.println("Properties <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//		}
//		Map<String, String> env = flow.getEnvironment();
//		if (env != null) {
//			
//			System.out.println("Environment >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//			
//			Set<String> keySet = env.keySet();
//			for (String key : keySet) {
//				Object value =  env.get(key);
//				if (value != null) {
//					System.out.println(key + " -> [" + env.get(key) + "]");
//				} else {
//					System.out.println(key + " -> [NULL]");
//				}	
//			}
//			
//			System.out.println("Environment <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//		}
//
//		System.out.println("----------------- ConsoleOutputPluginHandler -----------------");
//	}
//	
//
////	@Override
////	public void processChain(OutputFlow flow, Iterator<IPluginOutputHandler> chain, boolean abortChain) throws OutputHandlerException {
////		if (abortChain) {
////			return;
////		}
////		this.proccess(flow, abortChain);
////		abortChain = true;
////	}
//	
//	
////	@Override
////	public void processOutput(InputStream isSucess, InputStream isError, int exit) {
////
////		int stdout = 0;
////		int stderr = 0;
////
////		try {
////			stdout = isSucess.available();
////			stderr = isError.available();
////		} catch (IOException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
////
////		System.out.println("----------------- DefaultOutputPluginHandler -----------------");
////		System.out.println("Process Exit: " + exit + ", stdout: " + stdout + ", stderr: " + stderr);
////
////		if (stdout > 0) {
////			System.out.println("STDOUT >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
////			dumpImputStream(isSucess);
////
////			// System.out.println(out);
////
////			// System.out.println("Parsing JSON:");
////			// JsonValue json = Json.parse(out);
////			// System.out.println(" json = " + json.toString());
////
////			// System.out.println("Creating DOCUMENT:");
////			// MongoDB Document.parse hangs on malformed JSON String. Using an external
////			// parser.
////			// String out = this.read(isSucess);
////
////			// Document doc = new Document();
////			// doc = Document.parse(out);
////
////			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
////		}
////
////		if (stderr > 0) {
////			System.out.println("STDERR >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
////			dumpImputStream(isError);
////			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
////		}
////
////		System.out.println("----------------- DefaultOutputPluginHandler -----------------");
////	}
//
//	private void dumpImputStream(InputStream is) {
//
//		// final StringBuilder out = new StringBuilder();
//
//		try {
//			Reader in = new InputStreamReader(is, "UTF-8");
//
//			final int bufferSize = 1024;
//			final char[] buffer = new char[bufferSize];
//			for (;;) {
//				int rsz = in.read(buffer, 0, buffer.length);
//				if (rsz < 0) {
//					break;
//				}
//				System.out.print(buffer);
//			}
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//}
