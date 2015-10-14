package dyvil.tools.dpf;

import dyvil.tools.dpf.ast.Node;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class DPFTest
{
	public static void main(String[] args)
	{
		String file = "package {"
				+ "name = \"test\""
				+ "node {"
				+ "i = 10"
				+ "l = 10L"
				+ "d = 1.5D"
				+ "}"
				+ "node {"
				+ "list = [ 1, 2, 3 ]"
				+ "map = [ 1 : 'a', 2 : 'b' ]"
				+ "}";
		
		MarkerList markers = new MarkerList();
		DPFParser parser = new DPFParser(markers, file);
		
		Node node = new Node(Name.get("root"));
		parser.accept(node);
		
		System.out.println(node);
	}
	
}
