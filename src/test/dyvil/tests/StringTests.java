package dyvil.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dyvil.strings.StringUtils;

public class StringTests
{
	static final String[]	cases		= { "Just a random sentence", "testing some stuff", "the Cake is A LIE", "tHIS hAs WeIrD CaSiNG" };
	static final String[]	titleCase	= { "Just A Random Sentence", "Testing Some Stuff", "The Cake Is A Lie", "This Has Weird Casing" };
	
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
}
