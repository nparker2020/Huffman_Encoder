package com.noahparker.algorithms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class CompressionManager {
	File inputFile;
	File outputFile;
	String firstChar = "";
	int[] steps;
	int LEFT = 0;
	int RIGHT = 1;
	ArrayList<String> masterList;
	Node tree; //fully built/populated tree
	
	public CompressionManager() {
		masterList = new ArrayList<String>();
	}
	
	private void setup(String inputPath, String outputPath) {
		inputFile = new File(inputPath);
		outputFile = new File(outputPath);
		
		if(!outputFile.exists()) {
			try {
				outputFile.createNewFile();
			} catch (IOException e) {
				System.out.println("Error! Something went wrong when creating a new output file.");
				e.printStackTrace();
			}
		}
		
		if(!inputFile.exists()) {
			System.out.println("Input file does not exist! quitting.");
			return;
		}
	}
	
	public HashMap<String, Integer> scanFile() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String currentLine;
		HashMap<String, Integer> frequencies = new HashMap<String, Integer>();
		while((currentLine = reader.readLine()) != null) {
			masterList.add(currentLine);
			//scan line?
			for(int i = 0; i < currentLine.length(); i++) {
				char c = currentLine.charAt(i);
				Integer currentCount = frequencies.get(""+c);
				if(currentCount == null) {
					frequencies.put(""+c, 1);
				}else {
					frequencies.put(""+c, currentCount+=1);
				}
			}
			
		}
		
		//System.out.println("Frequencies contents:");
		int count = 0;
		for(String key : frequencies.keySet()) {
			//System.out.println(key+": "+frequencies.get(key));
			if(count == 0) {
				firstChar = key; //used for testing traversal, not used elsewhere
				//System.out.println("First char: ["+firstChar+"]");
			}
			count++;
		}
		reader.close();
		return frequencies;
	}
	
	
	public void encodeFile(String inputPath, String outputPath) throws FileNotFoundException, IOException {
		setup(inputPath, outputPath);
		if(!outputFile.exists()) {
			return;
		}
		
		HashMap<String, Integer> frequencies;
		
		try {
			frequencies = scanFile();
		} catch (IOException e) {
			System.out.println("Failed to scan input file!");
			e.printStackTrace();
			return;
		}
		
		Node tree = buildTree(frequencies);
		System.out.println("Tree built!");
		//System.out.println("Testing traversal for character...");
		
		DataOutputStream stream2 = new DataOutputStream(new FileOutputStream(outputFile));
		//ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(outputFile));
		ArrayList<ByteWrapper> allBytes = new ArrayList<ByteWrapper>();
		ArrayList<String> allBits = new ArrayList<String>();
		System.out.println("Encoding....");
		for(int i = 0; i < masterList.size(); i++) {
			
			String string = masterList.get(i);
			int bitCounter = 0;
			for(int a = 0; a < string.length(); a++) {
				
				//System.out.println("character to search for: "+string.charAt(a));
				ArrayList<Integer> path = new ArrayList<Integer>();
				
				//int[] traversal = traverseTree(""+string.charAt(a), tree, 0, null, -1, true);
				boolean returned = getPath(-1, tree, ""+string.charAt(a), path);
				
				int[] traversal = new int[path.size()];
				int count = 0;
				int bitCount = 0;
				for(Integer x : path) {
					if(x == LEFT) {
						//System.out.println("LEFT");
						bitCount++;
					}else if (x == RIGHT) {
						//System.out.println("RIGHT");
						bitCount++;
					}
					traversal[count] = x;
					count++;
				}
				
				if(bitCount > 7) {
					//System.out.println("THIS TRAVERSAL WILL NOT FIT INTO ONE BYTE");
				}
				
				if(traversal.length <= 8) {
					bitCounter += traversal.length;
				}else {
					//chop
					bitCounter += 8;
					//write first 8 bits of traversal into bit and write 2...
				}
				
				
				//String byteCode = testTraversal(tree, traversal);
				//ArrayList<ByteWrapper> byteEncodings = testTraversal(tree, traversal);
				String bitList = testTraversal(tree, traversal);
				allBits.add(bitList);
					

			}
		}
		
		
		byte currentByte = 0;
		int insertedBits = 0;
		ArrayList<Byte> encodedBytes = new ArrayList<Byte>();
		for(String byteCode : allBits) {
			
			for(int i = 0; i < byteCode.length(); i++) {
				int bit = 0;
				if(byteCode.charAt(i) == '1') {
					bit = 1;
				}else if(byteCode.charAt(i) == '0') {
					bit = 0;
				}
				//System.out.println(bit);
				currentByte |= bit; //or in value
				insertedBits++;
				if(insertedBits == 7) {
					//System.out.println("Byte with value: "+currentByte+" added to encoded stream.");
					encodedBytes.add(currentByte);
					currentByte = 0;
					insertedBits = 0;
				}else {
					currentByte <<= 1; //shift over 1
					
				}
				//or in bit to currentByte and shift 1
			}
		}
		
		if(insertedBits > 0) {
			//current currentByte needs to be added or bits will be lost
			encodedBytes.add(currentByte);
		}
		
		
		for(Byte b : encodedBytes) {
			stream2.writeByte(b);
		}
		stream2.flush();
		stream2.close();
		//stream.flush();
		//stream.close();
	}
	
	
	public void decodeFile(String outputPath) throws ClassNotFoundException, IOException {
		
		FileInputStream inputStream = new FileInputStream(outputFile);
	
		//ObjectInputStream stream = new ObjectInputStream(inputStream);
		DataInputStream stream2 = new DataInputStream(inputStream);
		ArrayList<Byte> masterList = new ArrayList<Byte>();
		//Object readIn;
		
		byte readIn = 0;
		String master = "";
		while(true) {
			try {
				readIn = stream2.readByte();
				masterList.add(readIn);
			} catch (IOException e) {
				//done
				//System.out.println("done reading file. Returning.");
				break;
				//e.printStackTrace();
			}
			
		
			//System.out.println("ReadIn: "+readIn);
		
		}
		
		String decodedString = processBitList(masterList);
		
		File file = new File(outputPath);
		if(!file.exists()) {
			file.createNewFile();
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(decodedString);
			
		writer.flush();
		writer.close();
		System.out.println("decoded text output to file.");
	}
	
	public String processBitList(ArrayList<Byte> masterList) {
		
		//
		Node currentNode = this.tree;
		String decodedString = "";
		
		boolean leaf = false;
		for(int i = 0; i < masterList.size(); i++) {
			byte byteIn = masterList.get(i);
		
			int[] bits = new int[7];
			for(int a = 0; a < 7; a++) {
				bits[a] = (byteIn >> (6 - a)) & 1;
			}
			
			
			for(int bit : bits) {
				if(bit == 0) {
					
					if(currentNode.getChar() != null/*currentNode.getLeft() == null*/) {
						//break;
						leaf = true;
					}else {
						currentNode = currentNode.getLeft();
						if(currentNode.getChar() != null) {
							leaf = true;
						}
					}
				}else if(bit == 1) {
					//System.out.println("1");
					
					if(currentNode.getChar() != null/*currentNode.getRight() == null*/) {
						//break;
						leaf = true;
					}else {
					//
						currentNode = currentNode.getRight();
						if(currentNode.getChar() != null) {
							leaf = true;
						}
					}
				}
				
				if(leaf) {
					decodedString += currentNode.getChar();
					currentNode = this.tree;
					leaf = false;
				}
			}
			
			
		}
		
		//System.out.println("decoded string length: "+decodedString.length());
		return decodedString;
	}
	
	//builds encoding tree from frequency map
	public Node buildTree(HashMap<String, Integer> frequencies) {
		
		//bubble sort
		Node[] sortedNodeArray = sortFrequencyArray(frequencies);
		
		//System.out.println("ORIGINAL state of array: ");
		String beforeArray = "[";
		for(int i = 0; i < sortedNodeArray.length; i++) {
			beforeArray+= sortedNodeArray[i].getValue()+"("+sortedNodeArray[i].getChar()+"),";
		}
		beforeArray+="]";
		//System.out.println(beforeArray);
		
		
		boolean treeBuilt = false;
		int upperBound = sortedNodeArray.length;
		int lowerBound = 0;
		Node tree = null;
		
		while(!treeBuilt) {
			int low1 = Integer.MAX_VALUE;
			int low2 = Integer.MAX_VALUE;
			Node low1Node = null;
			Node low2Node = null;
			int low1Index = 0;
			int low2Index = 0;
			
			
			boolean low1Found = false;
			for(int i = 0; i < sortedNodeArray.length; i++) {
				if(!sortedNodeArray[i].getIncluded()) {
					if(!low1Found) {
						low1Node = sortedNodeArray[i];
						low1Index = i;
						low1Found = true;
						//set
					}else {
						//set low2
						low2Node = sortedNodeArray[i];
						low2Index = i;
						//System.out.println("Low 2 found.");
						break;
					}
				}
			}
			
			
			sortedNodeArray[low1Index].setIncluded(true);
			sortedNodeArray[low2Index].setIncluded(true);
			
			Node newParent = new Node();
			newParent.setValue(low1Node.getValue() + low2Node.getValue());
			
			newParent.setLeft(low1Node);
			newParent.setRight(low2Node);
			
			
			for(int i = 0; i < sortedNodeArray.length; i++) {
				if(newParent.getValue() >= sortedNodeArray[i].getValue() && (i != sortedNodeArray.length - 1)) {
					//keep going 
				}else {
					//sortedNodeArray[i].getValue() > newParent.getValue()...
					if(i == sortedNodeArray.length -1) {
						//largest in list, special case.
						Node temp = sortedNodeArray[i];
						sortedNodeArray[i] = newParent;
						
						int index = i-1;
						while(index > 0) {
							Node temp2 = sortedNodeArray[index];
							sortedNodeArray[index] = temp;
							temp = temp2; //might work
							index--;
						}
						break;
					}
					
					if(i >= 1) {
						Node temp = sortedNodeArray[i-1];
						sortedNodeArray[i-1] = newParent;
						//lowerBound++; -this should happen after all the values are bumped down...
						int index = i-2;
						while(index > 0) {
							Node temp2 = sortedNodeArray[index];
							sortedNodeArray[index] = temp;
							temp = temp2; //might work
							index--;
						}
		
						break;
					}else {
						//System.out.println("first element in array is somehow bigger than the new parent.... ************");
					}
				}
			}
			//System.out.println("Current state of array: ");
			String stringArray = "[";
			for(int i = 0; i < sortedNodeArray.length; i++) {
				stringArray+= sortedNodeArray[i].getValue()+"("+sortedNodeArray[i].getChar()+"),";
			}
			stringArray+="]";
			//System.out.println(stringArray);
			
			int notIncludedCount = 0;
			for(int i = 0; i < sortedNodeArray.length; i++) {
				if(!sortedNodeArray[i].getIncluded()) {
					notIncludedCount++;
				}
			}
			
			
			if(notIncludedCount == 1) {
				//we should be done here!
				//System.out.println("we should be done! returning");
				treeBuilt = true;
				tree = sortedNodeArray[sortedNodeArray.length - 1];
				break;
			}
			//System.out.println(notIncludedCount+ " Nodes that still have to be included.");
		}
		
		this.tree = tree;
		return tree;
	}
	
	public int[] traverseTree(String key, Node tree, int step, int[] steps, int DIRECTION, boolean isRoot) {
		
		//initial setup to track steps, should it return a boolean denoting the direction it came from?
		if(isRoot) {
			steps = new int[tree.getValue()];
			for(int i = 0; i< steps.length; i++) {
				steps[i] = -1;
			}
		}
		
		if(tree == null) {
			return steps; //doesn't matter
		}
		
		steps[step] = DIRECTION;
		
		if(tree.getChar() == null || !tree.getChar().equals(key)) {
			
			traverseTree(key, tree.getLeft(), step+1, steps, LEFT, false);
			traverseTree(key, tree.getRight(), step+1, steps, RIGHT, false);
		}else {
	
			return steps;
		}
		//if specified char not in tree (never), OR NODE WAS NOT FOUND IN CURRENT BRANCH!!!!
		//System.out.println("Node not found in branch");
		steps[step] = -1; //erase here
		return steps;
	}
	
	boolean getPath(int DIRECTION, Node root,String targetValue, ArrayList<Integer> path)
	{
	    // base case root is null so path not available
	    if(root==null)
	        return false;
	    //add the data to the path
	    path.add(DIRECTION);
	    //if the root has data return true,path already saved
	    if(root.getChar() != null) {
	    	if(root.getChar().equals(targetValue))
		        return true;
	    }
	    //find the value in all the children
	    if(getPath(LEFT, root.getLeft(), targetValue, path) ||
	    		getPath(RIGHT, root.getRight(), targetValue, path)) {
	    	return true;
	    }
	    
	    //if this node does not exist in path remove it
	    path.remove(path.size()-1);
	    return false;
	}
	
	
	public String testTraversal(Node tree, int[] traversal) {
		Node currentNode = tree;
		String byteCode = "";
		//byte b = 0;
		byte one = 1;
		ArrayList<ByteWrapper> byteWrappers = new ArrayList<ByteWrapper>();
		byte[] byteArray = new byte[5];
		int byteArrayIndex = 0;
		//System.out.println("Byte array size: "+byteArray.length);
		int bitCount = 0;
		for(int i = 0; i < traversal.length; i++) {
			int step = traversal[i];
			
			if(step != -1) {
				if(step == LEFT) {
					//System.out.println("Moving LEFT");
					
					byteArray[byteArrayIndex] = (byte) (byteArray[byteArrayIndex] << 1); //shift over one effectively adding a zero.
					
					byteCode += 0;
					bitCount++;
					currentNode = currentNode.getLeft(); 
				
				}else if(step == RIGHT) {
					//shift over one and or in a one to register right turn.
					byteArray[byteArrayIndex] = (byte) (byteArray[byteArrayIndex] << 1);
					byteArray[byteArrayIndex] = (byte) (byteArray[byteArrayIndex] | one);
					bitCount++;
					byteCode += 1; 
					currentNode = currentNode.getRight();
					
				}
			}
			if(bitCount >= 7) {
				//we're full.
				ByteWrapper wrapper = new ByteWrapper();
				wrapper.data = byteArray[byteArrayIndex];
				wrapper.length = 7;
				byteWrappers.add(wrapper);
				byteArrayIndex++;
				
				bitCount = 0;
			}
		}
		
		//byteArray is properly populated, need to somehow return the length of the traversal and also the byte array...
		ByteWrapper wrapper = new ByteWrapper();
		wrapper.data = byteArray[byteArrayIndex];
		wrapper.length = bitCount;
		byteWrappers.add(wrapper);
		//special case if byteArrayIndex = 0???	
		
		BitSet bitset = new BitSet(byteCode.length());
		
		//int bitCount = 0;
		for(int i = 0; i < byteCode.length(); i++) {
			if(byteCode.charAt(i) == '1') {
				bitset.set(i);
			}	
		}
		
		
		return byteCode;
	}
	
	public String getCharacter(Node tree, int[] traversal) {
		
		Node currentNode = tree;
		for(int i = 0; i < traversal.length; i++) {
			if(traversal[i] == 0) {
				currentNode = currentNode.getLeft();
			}else if(traversal[i] == 1){
				currentNode = currentNode.getRight();
			}
		}
		
		return currentNode.getChar();
	}

	public Node[] sortFrequencyArray(HashMap<String, Integer> frequencies) {
		
		Node[] nodeArray = new Node[frequencies.size()];
		int count = 0;
		for(String key: frequencies.keySet()) {
			Node n = new Node();
			n.setValue(frequencies.get(key));
			n.setChar(key);
			nodeArray[count] = n;
			count++;
		}
			
		boolean sorted = false;
		while(!sorted) {
			boolean swapped = false;
			for(int i = 0; i < nodeArray.length; i++) {
				Node current = nodeArray[i];
				
				if(i < nodeArray.length-1) {
					if(current.getValue() > nodeArray[i+1].getValue()) {
						//swap
						Node temp = nodeArray[i+1];
						nodeArray[i] = temp;
						nodeArray[i+1] = current;
						swapped = true;
					}
				}
				
			}
			
			if(!swapped) {
				//System.out.println("Array sorted! returning");
				sorted = true;	
			}
		
		}
		
		return nodeArray;
	}
	
}
