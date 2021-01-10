package io.infolayer.aida.executor.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.utils.PlatformUtils;


public class JarFileUtil {
	
	private static Logger log = LoggerFactory.getLogger(JarFileUtil.class);
	
	public static void deleteDirectoryRecursive(File file) {
		
		if (file != null && file.exists() && file.isDirectory()) {

			// https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
			try {
				Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	
	//Thanks to: https://stackoverflow.com/questions/1529611/how-to-write-a-java-program-which-can-extract-a-jar-file-and-store-its-data-in-s
	public static void extractResourcesToFolder(File targetFile, File destinationRoot) throws IOException {
	    try {
	    	
	    	if (log.isDebugEnabled()) {
	    		log.debug("targetFile = " + targetFile);
	    		log.debug("destinationRoot = " + destinationRoot);
	    	}

	    	if (targetFile == null || !targetFile.canRead()) {
	    		throw new IOException("Target file is null or cannot be read.");
	    	}
	    	
	    	if (destinationRoot == null || !destinationRoot.canWrite()) {
	    		throw new IOException("Destination directory cannot be written.");
	    	}

	        //JarFile jarFile = new JarFile(String.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	        JarFile jarFile = new JarFile(targetFile);
	        
	        Enumeration<JarEntry> enums = jarFile.entries();
	        while (enums.hasMoreElements()) {
	            JarEntry entry = enums.nextElement();
	            if (entry.getName().startsWith("resources")) {
	            	
	            	//System.out.println("File to write: " + entry.getName());
	            	
	                File toWrite = new File(destinationRoot.getPath(), entry.getName());
	                if (entry.isDirectory()) {
	                    toWrite.mkdirs();
	                    continue;
	                }
	                InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
	                OutputStream out = new BufferedOutputStream(new FileOutputStream(toWrite));
	                byte[] buffer = new byte[2048];
	                for (;;) {
	                    int nBytes = in.read(buffer);
	                    if (nBytes <= 0) {
	                        break;
	                    }
	                    out.write(buffer, 0, nBytes);
	                }
	                out.flush();
	                out.close();
	                in.close();
	            }
	            
	        }
	        
	        jarFile.close();
	    } catch (IOException ex) {
	    	log.error(ex.getMessage());
	    	throw new IOException(ex);
	    }
	}
	
	public static void extractJarFile(String bundleName, File bundleFile, String workingDirectory)
			throws IOException {

		if (workingDirectory == null) {
			throw new IOException("Working directory cannot be null.");
		}

		File destination = new File(workingDirectory, bundleName);
		if (destination.exists()) {
			JarFileUtil.deleteDirectoryRecursive(destination);
		}

		destination.mkdirs();
		JarFileUtil.extractResourcesToFolder(bundleFile, destination);

	}
	
	
	public static File getBundleResourcesDirectory(String bundleName, String workingDirectory) throws IOException {

		if (log.isDebugEnabled()) {
			log.debug("getBundleResourcesDirectory() bundleName = " + bundleName); 
			log.debug("getBundleResourcesDirectory() workingDirectory = " + workingDirectory); 
		}
		
		if (workingDirectory == null) {
			throw new IOException("Working directory cannot be null.");
		}

		// FIXME Is there a better way?
		String path = PlatformUtils.sanitizePath(MessageFormat.format("/{0}/{1}/resources/plugins/", workingDirectory, bundleName));
		
		File destination = new File(path);
		if (destination.canRead() && destination.isDirectory()) {
			return destination;
		} else {
			throw new IOException(path + " cannot be read or isn't a directory.");
		}
	}

}
