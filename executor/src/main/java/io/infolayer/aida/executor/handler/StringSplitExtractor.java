package io.infolayer.aida.executor.handler;

public class StringSplitExtractor implements IFieldExtractor {
	
	private String splitChar;
	private int index = 0;
	
	public StringSplitExtractor() {
		this.splitChar = " ";
		this.index = 0;
	}
	
	public StringSplitExtractor(String splitChar, int index) {
		this.splitChar = splitChar;
		this.index = index;
	}

	@Override
	public Object extract(String text) {
		if (text != null) {
			String data[] = text.split(splitChar);
			if (data != null) {
				return data[index];
			}
		}
		return null;
	}

}
