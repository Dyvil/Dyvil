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
		String file = FileUtils.read(new File("dists/distributions.dyp"));
		
		MarkerList markers = new MarkerList();
		DPFParser parser = new DPFParser(markers, file);
		
		Node node = new DPFFile();
		parser.accept(node);
		System.out.println(node);
	}
}
