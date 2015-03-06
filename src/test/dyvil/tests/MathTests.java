package dyvil.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dyvil.math.MathUtils;

public class MathTests
{
	@Test
	public void testLog2()
	{
		for (int i = 1; i <= 0xEFFFFFFF; i <<= 1)
		{
			assertEquals("LogBaseTwo for " + i, i, 1 << MathUtils.logBaseTwo(i));
		}
	}
	
	@Test
	public void testIsPowerOf2()
	{
		for (int i = 1; i <= 0xEFFFFFFF; i <<= 1)
		{
			assertTrue(i + " is not a power of 2", MathUtils.isPowerOfTwo(i));
		}
	}
	
	@Test
	public void testByteSqrt()
	{
		for (byte i = 0; i < Byte.MAX_VALUE; i++)
		{
			assertEquals("Sqrt " + i * i, i, MathUtils.sqrt((byte) (i * i)));
		}
	}
	
	@Test
	public void testShortSqrt()
	{
		for (short i = 0; i < Short.MAX_VALUE; i++)
		{
			assertEquals("Sqrt " + i * i, i, MathUtils.sqrt((short) (i * i)));
		}
	}
	
	@Test
	public void testIntSqrt()
	{
		for (int i = 0; i < Integer.MAX_VALUE; i++)
		{
			assertEquals("Sqrt" + i * i, i, MathUtils.sqrt(i * i));
		}
	}
}
