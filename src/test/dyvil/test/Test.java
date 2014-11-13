package test;

import java.util.Scanner;

public class Test
{
	public static void main(String[] args)
	{
		System.out.println("Dyvil BinaryConverter 1.0");
		System.out.println("-------------------------");
		
		Scanner scanner = new Scanner(System.in);
		scan(scanner);
	};
	
	public static void scan(Scanner scanner)
	{
		System.out.println("Please enter your number to convert to binary.");
		int i = scanner.nextInt();
		System.out.println("= 0b" + Integer.toBinaryString(i));
		System.out.println();
		
		// Recursion
		scan(scanner);
	}
}
