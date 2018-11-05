package dyvil.strings;

import dyvil.string.CharUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class CharTests
{
	@Test
	public void testIsDigit()
	{
		for (int i = 0; i < 256; i++)
		{
			char c = (char) i;
			assertTrue(c + " is not a digit", CharUtils.isDigit(c) == Character.isDigit(c));
		}
	}

	@Test
	public void testIsLetter()
	{
		for (int i = 0; i < 256; i++)
		{
			char c = (char) i;
			assertTrue(c + " is not a letter", CharUtils.isLetter(c) == Character.isLetter(c));
		}
	}

	@Test
	public void testIsWhitespace()
	{
		for (int i = 0; i < 256; i++)
		{
			char c = (char) i;
			boolean b1 = CharUtils.isWhitespace(c);
			boolean b2 = Character.isWhitespace(c);
			assertTrue(c + " (" + Integer.toHexString(c) + ") " + (b2 ? "is" : "is not") + " a whitespace character",
			           b1 == b2);
		}
	}

	@Test
	public void testIsLowercase()
	{
		for (int i = 0; i < 256; i++)
		{
			char c = (char) i;
			assertTrue(c + " is not a lowercase character", CharUtils.isLowerCase(c) == Character.isLowerCase(c));
		}
	}

	@Test
	public void testIsUppercase()
	{
		for (int i = 0; i < 256; i++)
		{
			char c = (char) i;
			assertTrue(c + " is not an uppercase character", CharUtils.isUpperCase(c) == Character.isUpperCase(c));
		}
	}

	@Test
	public void testToLowercase()
	{
		for (int i = 0; i < 256; i++)
		{
			char c = (char) i;
			assertEquals(CharUtils.toLowerCase(c), Character.toLowerCase(c));
		}
	}

	@Test
	public void testToUppercase()
	{
		for (int i = 0; i < 256; i++)
		{
			char c = (char) i;
			char c1 = CharUtils.toUpperCase(c);
			char c2 = Character.toUpperCase(c);
			if (c1 != c2)
			{
				fail(c + ".toUpperCase: expected " + c2 + ", was: " + c1);
			}
		}
	}
}
