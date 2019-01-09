package com.noahparker.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Main {

	
	public static void main(String args[]) {
		//String stepArg = args[0];
		//String inputPath = args[1];
		//String outputPath = args[2];
		
		CompressionManager manager = new CompressionManager();
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Program Initialized! Please enter an Encode command.");
		System.out.println("Syntax: Encode <input-file-path> <output-file-path>");
		System.out.println("After Encoding, the Decode command syntax is: ");
		System.out.println("Syntax: Decode <input-file-path> <output-file-path>");
		while(true) {
			System.out.println("Please enter a command:");
			
			String arg = scanner.nextLine();
			String[] arguments = arg.split(" ");
			String inputPath = arguments[1];
			String outputPath = arguments[2];
			if(arg.contains("Encode")) {
				try {
					scanner.close();
					manager.encodeFile(inputPath, outputPath);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if(arg.contains("Decode")) {
				try {
					scanner.close();
					manager.decodeFile(outputPath);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				System.out.println("Command did not contain a valid keyword. Please try again.");
			}
		}
		
	}
	
}
