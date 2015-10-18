package dyvil.tools.dpf;

import java.io.File;

import dyvil.io.FileUtils;
import dyvil.tools.dpf.ast.DPFFile;
import dyvil.tools.dpf.ast.Node;
import dyvil.tools.parsing.marker.MarkerList;

public class DPFTest
{
	private static String[] files = { "versions/distributions.dyp", "versions/versions.dyp" };
	
	public static void main(String[] args)
	{
		for (String fileName : files) {
		String file = FileUtils.read(new File(fileName));
		
		MarkerList markers = new MarkerList();
		DPFParser parser = new DPFParser(markers, file);
		
		Node node = new DPFFile();
		parser.accept(node);
		System.out.println(node);}
	}
}
