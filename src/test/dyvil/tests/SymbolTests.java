package dyvil.tests;

import dyvil.tools.parsing.name.Qualifier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SymbolTests
{
	@Test
	public void testUnqualify() throws Exception
	{
		assertEquals("this$is$a$test", Qualifier.unqualify("this$is$a$test"));
		assertEquals("this+-$test", Qualifier.unqualify("this$plus$minus$test"));
		assertEquals("++/", Qualifier.unqualify("$plus$plus$div"));
		assertEquals("+_and/", Qualifier.unqualify("$plus_and$div"));
		assertEquals("+$_and/", Qualifier.unqualify("$plus$_and$div"));
		assertEquals("+_-_and/", Qualifier.unqualify("$plus_$minus_and$div"));
		assertEquals("+$and_/", Qualifier.unqualify("$plus$and_$div"));
	}
}
