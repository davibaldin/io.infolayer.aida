package io.infolayer.aida.executor.handler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexExtractor {

	private Map<String, IFieldExtractor> postExtractors = null;
	
	private Pattern regex = null;
	private Map<Integer, String> groupName = null;
	
	public RegexExtractor(String pattern) {
		this.postExtractors = new HashMap<String, IFieldExtractor>();
		
		if (pattern == null) {
			throw new IllegalArgumentException("Pattern string must be set.");
		}
		
		//System.out.println("pattern is = '" + this.pattern + "'");

		this.regex = Pattern.compile(pattern, Pattern.DOTALL);
		this.groupName = new HashMap<Integer, String>();
		
		// FIXME
		// https://stackoverflow.com/questions/15588903/get-group-names-in-java-regex
		try {
			Field namedGroups = regex.getClass().getDeclaredField("namedGroups");
			namedGroups.setAccessible(true);
			@SuppressWarnings("unchecked")
			final Map<String, Integer> nameToGroupIndex = (Map<String, Integer>) namedGroups.get(regex);
			for (Map.Entry<String, Integer> entry : nameToGroupIndex.entrySet()) {
				this.groupName.put(entry.getValue(), entry.getKey());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addPostExtractor(String fieldName, IFieldExtractor instance) {
		if (fieldName != null && instance != null) {
			this.postExtractors.put(fieldName, instance);
		}
	}
	
	private Object postProcess(String fieldName, Object content) {
		IFieldExtractor extractor = this.postExtractors.get(fieldName);
		if (extractor == null) {
			return content;
		}else {
			return extractor.extract(content.toString());
		}
	}
	
	public Map<String, Object> extract(String content) {
		
		//System.err.println("D content " + content);

		if (content == null) {
			return null;
		}
		
		//System.err.println("D content len " + content.length());

		Map<String, Object> fields = new HashMap<String, Object>();
		Matcher matcher = regex.matcher(content);
		int groups = matcher.groupCount();

		//System.out.println("groups = " + groups);
		
		if (matcher.find()) {
			for (int i = 0; i < groups; i++) {
				String groupName = null;
				if (this.groupName.containsKey(i+1)) {
					groupName = this.groupName.get(i+1);
				}else {
					groupName = "g_" + i+1;
				}
				//System.out.println(groupName + " -> " + matcher.group(i+1));
				fields.put(groupName, 
						this.postProcess(groupName, matcher.group(i+1)));
			}
		}else {
			//System.out.println("Match not found.");
		}
		
		
		return fields;
	}

}
