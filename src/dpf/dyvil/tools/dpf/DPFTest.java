package dyvil.tools.dpf;

import java.io.File;

import dyvil.io.FileUtils;
import dyvil.tools.dpf.ast.DPFFile;
import dyvil.tools.dpf.ast.Node;
import dyvil.tools.parsing.marker.MarkerList;

public class DPFTest
{
	public static void main(String[] args)
	{
		String file = "name = \"super\"\n"
				+ "package.type = type\n"
				+ "package.name.override = \" \\(package) \\(name) \"\n"
				+ "package.node\n"
				+ "{\n"
				+ "name = \"name\"\n"
				+ "}\n"
				+ ""
				+ "package {\n"
				+ "name = test\n"
				+ "node {\n"
				+ "int = 10\n"
				+ "long = 10L\n"
				+ "double = 1.5D\n"
				+ "float = 1.2F\n"
				+ "string = \"test\"\n"
				+ "list = [ 1, 2, 3 ]\n"
				+ "emptyList = [ ]\n"
				+ "}\n"
				+ "}\n";
		
		MarkerList markers = new MarkerList();
		DPFParser parser = new DPFParser(markers, file);
		
		Node node = new DPFFile();
		parser.accept(node);
		System.out.println(node);
		
		System.out.println("-------------");
		
		markers.clear();
		
		file = FileUtils.read(new File("dists/distributions.dyp"));
		parser = new DPFParser(markers, file);
		
		node = new DPFFile();
		parser.accept(node);
		System.out.println(node);
	}
}
