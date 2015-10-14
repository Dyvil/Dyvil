package dyvil.tools.dpf;

import dyvil.tools.dpf.ast.DPFFile;
import dyvil.tools.dpf.ast.Node;
import dyvil.tools.parsing.marker.MarkerList;

public class DPFTest
{
	public static void main(String[] args)
	{
		String file = "name = \"super\"\n"
				+ "package.type = default\n"
				+ "package.name.override = override\n"
				+ ""
				+ "package {\n"
				+ "name = test\n"
				+ "node {\n"
				+ "int = 10\n"
				+ "long = 10L\n"
				+ "double = 1.5D\n"
				+ "float = 1.2F\n"
				+ "string = \"test\"\n"
				+ "}\n"
				+ "}\n";
		
		MarkerList markers = new MarkerList();
		DPFParser parser = new DPFParser(markers, file);
		
		Node node = new DPFFile();
		parser.accept(node);
		
		System.out.println(node);
	}
}
