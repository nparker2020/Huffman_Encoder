package com.noahparker.algorithms;

public class Node {
	private String character;
	private int value; //frequency
	private Node left;
	private Node right;
	private boolean included = false;
	
	public Node getLeft() {
		return left;
	}
	
	public boolean getIncluded() {
		return included;
	}
	
	public void setIncluded(boolean included) {
		this.included = included;
	}
	
	public Node getRight() {
		return right;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setRight(Node right) {
		this.right = right;
	}
	
	public void setLeft(Node left) {
		this.left = left;
	}
	
	public void setChar(String character) {
		this.character = character;
	}
	
	public String getChar() {
		return character;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	

}
