package dyvil.tests;

import dyvil.string.StringConversions;
import dyvil.string.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class StringTests
{
	static final String[] cases     = { "Just a random sentence", "testing some stuff", "the Cake is A LIE",
			"tHIS hAs WeIrD CaSiNG", "AnotherTest" };
	static final String[] titleCase = { "Just A Random Sentence", "Testing Some Stuff", "The Cake Is A Lie",
			"This Has Weird Casing", "Anothertest" };
	
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

	@Test
	public void testSplitChar()
	{
		for (String testString : cases)
		{
			String[] charSplit = StringUtils.split(testString, ' ');
			String[] stringSplit = testString.split(" ");

			assertArrayEquals(stringSplit, charSplit);
		}
	}
}
