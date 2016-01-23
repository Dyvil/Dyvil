package dyvil.tests;

import dyvil.collection.Map;
import dyvil.collection.mutable.TreeMap;
import dyvil.tools.dpf.Parser;
import dyvil.tools.dpf.ast.Expandable;
import dyvil.tools.dpf.ast.RootNode;
import dyvil.tools.dpf.converter.binary.BinaryReader;
import dyvil.tools.dpf.converter.binary.BinaryWriter;
import dyvil.tools.dpf.converter.flatmapper.FlatMapConverter;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class DPFTests
{
	private static final String TEST_FILE =
			"\n" + "node1\n" + "{\n" + "\tintProperty = 10\n" + "\tstringProperty = \"abc\"\n"
					+ "\tstringProperty2 = \"String Interpolation: \\(node1.stringProperty) where intProperty = \\(node1.intProperty)\"\n"
					+ "\tlistProperty = [ 1, \"a\", true ]\n" + "\tmapProperty = {\n" + "\t\t1 : \"a\",\n"
					+ "\t\t2 : \"b\"\n" + "\t}\n" + "}\n" + "\n" + "node2.subNode\n" + "{\n" + "\tsubProperty1 = 42\n"
					+ "\t\n" + "\tsubNode2\n" + "\t{\n" + "\t\tsubProperty2 = 13\n" + "\t}\n" + "}\n";

	private RootNode rootNode;

	@Before
	public void init()
	{
		this.rootNode = Parser.parse(TEST_FILE);
	}

	@Test
	public void testParser()
	{
		assertEquals(this.rootNode.toString(), TEST_FILE);
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

		// Expand the Node structure
		RootNode expandedNode = this.rootNode.expand(baseMap, false);

		// Convert the Node structure to a Map
		Map<String, Object> nodeMap = new TreeMap<>();
		expandedNode.accept(new FlatMapConverter(nodeMap));

		Map<String, Object> expandedMap = (Map<String, Object>) Expandable.expandMap(baseMap, baseMap, false);

		assertEquals(nodeMap, expandedMap);
	}

	@Test
	public void testBinary() throws Throwable
	{
		final byte[] bytes;
		try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		     final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream))
		{
			this.rootNode.accept(new BinaryWriter(dataOutputStream));
			bytes = byteArrayOutputStream.toByteArray();
		}

		try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		     final DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream))
		{
			final BinaryReader binaryReader = new BinaryReader(dataInputStream);
			final RootNode rootNode = new RootNode();
			binaryReader.readNodes(rootNode);

			assertEquals(this.rootNode.toString(), rootNode.toString());
		}
		catch (RuntimeException ex)
		{
			throw ex.getCause();
		}
	}
}
