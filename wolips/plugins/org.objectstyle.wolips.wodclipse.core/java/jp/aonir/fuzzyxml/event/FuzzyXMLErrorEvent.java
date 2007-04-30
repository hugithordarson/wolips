package jp.aonir.fuzzyxml.event;

import jp.aonir.fuzzyxml.FuzzyXMLNode;

public class FuzzyXMLErrorEvent {
	
	private int offset;
	private int length;
	private String message;
	private FuzzyXMLNode node;
	
	public FuzzyXMLErrorEvent(int offset,int length,String message,FuzzyXMLNode node){
		this.offset  = offset;
		this.length  = length;
		this.message = message;
		this.node    = node;
	}
	
	/**
	 * @return length ��߂��܂��B
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * @return message ��߂��܂��B
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @return offset ��߂��܂��B
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * @return node ��߂��܂��B
	 */
	public FuzzyXMLNode getNode() {
		return node;
	}
}
