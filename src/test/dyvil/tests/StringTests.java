package dyvil.tests;

import dyvil.string.StringConversions;
import dyvil.string.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringTests
{
	static final String[] cases     = { "Just a random sentence", "testing some stuff", "the Cake is A LIE",
			"tHIS hAs WeIrD CaSiNG" };
	static final String[] titleCase = { "Just A Random Sentence", "Testing Some Stuff", "The Cake Is A Lie",
			"This Has Weird Casing" };
	
	@Test
	public void testAcronym()
	{
		assertEquals("Acronym of 'Hello World'", "HW", StringUtils.toAcronym("Hello World"));
		assertEquals("Acronym of 'Dyvil Library'", "DL", StringUtils.toAcronym("Dyvil Library"));
		assertEquals("Acronym of 'String Test'", "ST", StringUtils.toAcronym("String Test"));
		assertEquals("Acronym of 'A Very Special Acronym'", "AVSA", StringUtils.toAcronym("A Very Special Acronym"));
		assertEquals("Acronym of 'Abstract Syntax Tree'", "AST", StringUtils.toAcronym("Abstract Syntax Tree"));
		assertEquals("Acronym of 'Half-Life 3'", "HL3", StringUtils.toAcronym("Half-Life 3"));
		assertEquals("Acronym of 'Some-Random-Test'", "SRT", StringUtils.toAcronym("Some-Random-Test"));
	}
	
	@Test
	public void testTitleCase() throws Exception
	{
		for (int i = 0; i < cases.length; i++)
		{
			assertEquals("Title Case of " + cases[i], titleCase[i], StringUtils.toTitleCase(cases[i]));
		}
	}
	
	@Test
	public void testRoman()
	{
		assertEquals(StringConversions.toRomanString(2015), "MMXV");
		assertEquals(StringConversions.toRomanString(1234), "MCCXXXIV");
		assertEquals(StringConversions.toRomanString(42), "XLII");
		assertEquals(StringConversions.toRomanString(1971), "MCMLXXI");
		assertEquals(StringConversions.toRomanString(420), "CDXX");
	}
}
