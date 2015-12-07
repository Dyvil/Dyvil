package dyvil.tests;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.tools.dpf.Parser;
import dyvil.tools.dpf.ast.RootNode;
import dyvil.tools.dpf.converter.flatmapper.FlatMapConverter;
import dyvil.tools.parsing.marker.MarkerList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DPFTests
{
	private static final String TEST_FILE = "\nnode1\n" + "{\n" + "\tintProperty = 10\n" + "\tstringProperty = \"abc\"\n"
			+ "\tlistProperty = [ 1, \"a\", true ]\n" + "\tmapProperty = {\n" + "\t\t1 : \"a\",\n" + "\t\t2 : \"b\"\n"
			+ "\t}\n" + "}\n" + "\n" + "node2.subNode\n" + "{\n" + "\tsubProperty1 = 42\n" + "\t\n" + "\tsubNode2\n"
			+ "\t{\n" + "\t\tsubProperty2 = 13\n" + "\t}\n" + "}\n";

	@Test
	public void testParser()
	{
		Parser parser = new Parser(new MarkerList(), TEST_FILE);

		RootNode rootNode = new RootNode();
		parser.accept(rootNode);

		System.out.println(rootNode);

		assertEquals(rootNode.toString(), TEST_FILE);
	}

	@Test
	public void testMap()
	{
		Parser parser = new Parser(new MarkerList(), TEST_FILE);

		Map<String, Object> map = new HashMap<>();

		parser.accept(new FlatMapConverter(map));

		map.forEach(System.out::println);
	}
}
