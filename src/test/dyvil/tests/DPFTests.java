package dyvil.tests;

import dyvil.collection.Map;
import dyvil.collection.mutable.TreeMap;
import dyvil.lang.Boolean;
import dyvil.tools.dpf.ast.Expandable;
import dyvil.tools.dpf.ast.RootNode;
import dyvil.tools.dpf.converter.flatmapper.FlatMapConverter;
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
		RootNode rootNode = RootNode.parse(TEST_FILE);

		System.out.println(rootNode);

		assertEquals(rootNode.toString(), TEST_FILE);
	}

	@Test
	public void testMap()
	{
		Map<String, Object> map = FlatMapConverter.parse(TEST_FILE);

		map.forEach(System.out::println);
	}

	@Test
	public void testExpand()
	{
		// Parse as a Map
		Map<String, Object> baseMap = new TreeMap<>();
		FlatMapConverter.parse(TEST_FILE, baseMap);

		Map<String, Object> mappings = baseMap.$plus("true", Boolean.apply(true));

		// Parse as a Node structure
		RootNode testNode = RootNode.parse(TEST_FILE);

		// Expand the Node structure
		RootNode expandedNode = testNode.expand(mappings, false);

		// Convert the Node structure to a Map
		Map<String, Object> nodeMap = new TreeMap<>();
		expandedNode.accept(new FlatMapConverter(nodeMap));

		Map<String, Object> expandedMap = (Map<String, Object>) Expandable.expandMap(baseMap, mappings, false);

		assertEquals(nodeMap.toString(), expandedMap.toString());
	}
}
