package dyvil.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dyvil.lang.Float;
import dyvil.lang.Int;
import dyvil.lang.Long;

import dyvil.collection.*;
import dyvil.collection.immutable.EmptyList;
import dyvil.collection.immutable.EmptySet;
import dyvil.collection.immutable.SingletonList;
import dyvil.collection.immutable.SingletonSet;
import dyvil.util.Immutable;

import org.junit.Before;
import org.junit.Test;

public class CollectionTests
{
	private static final Object[] SAMPLES = new Object[] { "abc", "def", "aA", "BB", null, Float.apply(3F), Long.apply(1L), Int.apply(10) };
	
	@Before
	public void init()
	{
		assert"aA".hashCode() == "BB".hashCode();
	}
	
	@Test
	public void testSets()
	{
		testSet(EmptySet.apply());
		testSet(SingletonSet.apply("abc"));
		testSet(this.testSetBuilder(dyvil.collection.immutable.ArraySet.builder()));
		testSet(this.testSetBuilder(dyvil.collection.immutable.HashSet.builder()));
		
		testSet(dyvil.collection.mutable.ArraySet.apply(SAMPLES));
		testSet(dyvil.collection.mutable.HashSet.apply(SAMPLES));
	}
	
	@Test
	public void testLists()
	{
		testList(EmptyList.apply());
		testList(SingletonList.apply("abc"));
		testList(this.testListBuilder(dyvil.collection.immutable.ArrayList.builder()));
		
		testList(dyvil.collection.mutable.ArrayList.apply(SAMPLES));
		testList(dyvil.collection.mutable.LinkedList.apply(SAMPLES));
	}
	
	@Test
	public void testMaps()
	{
	
	}
	
	public void testQueryable(Queryable queryable)
	{
	
	}
	
	public void testBidiQueryable(BidiQueryable queryable)
	{
	
	}
	
	public void testCollection(Collection collection)
	{
		boolean isImmutable = collection.isImmutable();
		Collection copy = collection.copy();
		Collection mutable = collection.mutable();
		Collection mutableCopy = collection.mutableCopy();
		Collection immutable = collection.immutable();
		Collection immutableCopy = collection.immutableCopy();
		
		assertTrue("Immutable Collection must be instanceof dyvil.util.Immutable", isImmutable == collection instanceof Immutable);
		
		assertEquals("Collection must be equal to it's copy: " + copy.getClass(), collection, copy);
		assertEquals("Collection must be equal to it's mutable version: " + mutable.getClass(), collection, mutable);
		assertEquals("Collection must be equal to it's mutable copy: " + mutableCopy.getClass(), collection, mutableCopy);
		assertEquals("Collection must be equal to it's immutable version: " + immutable.getClass(), collection, immutable);
		assertEquals("Collection must be equal to it's immutable copy: " + mutableCopy.getClass(), collection, immutableCopy);
		
		assertFalse("Mutable Version must be mutable: " + mutable.getClass(), mutable.isImmutable());
		assertFalse("Mutable Copy must be mutable: " + mutableCopy.getClass(), mutableCopy.isImmutable());
		assertTrue("Immutable Version must be immutable: " + immutable.getClass(), immutable.isImmutable());
		assertTrue("Immutable Copy must be immutable: " + immutableCopy.getClass(), immutableCopy.isImmutable());
		
		if (!isImmutable)
		{
			Collection newCollection = collection.$plus("newValue");
			collection.add("newValue");
			assertEquals("+ or += does not work correctly: " + collection.getClass(), collection, newCollection);
			collection.remove("newValue");
			assertEquals("- or -= does not work correctly: " + collection.getClass(), copy, collection);
			
			assertEquals(immutable.$plus("newElement"), collection.$plus("newElement"));
			assertEquals(immutable.$minus("newElement"), collection.$minus("newElement"));
		}
		else
		{
			assertEquals(mutable.$plus("newElement"), collection.$plus("newElement"));
			assertEquals(immutable.$minus("newElement"), collection.$minus("newElement"));
		}
	}
	
	public void testSet(Set set)
	{
		testQueryable(set);
		testCollection(set);
		
	}
	
	public void testList(List list)
	{
		testQueryable(list);
		testBidiQueryable(list);
		testCollection(list);
	}
	
	public void testMap(Map map)
	{
	
	}
	
	public ImmutableList testListBuilder(ImmutableList.Builder builder)
	{
		for (Object s : SAMPLES)
		{
			builder.add(s);
		}
		
		ImmutableList list = builder.build();
		assertFalse("Builder list must not be null", list == null);
		testBuilderError(builder);
		assertEquals("Builder list has incorrect size", list.size(), SAMPLES.length);
		return list;
	}
	
	public ImmutableSet testSetBuilder(ImmutableSet.Builder builder)
	{
		for (Object s : SAMPLES)
		{
			builder.add(s);
		}
		// Add the elements twice to see if it duplicates
		for (Object s : SAMPLES)
		{
			builder.add(s);
		}
		
		ImmutableSet set = builder.build();
		assertFalse("Builder set must not be null", set == null);
		testBuilderError(builder);
		assertEquals("Builder set has incorrect size", set.size(), SAMPLES.length);
		return set;
	}
	
	public ImmutableMap testMapBuilder(ImmutableMap.Builder builder)
	{
		return builder.build();
	}
	
	public void testBuilderError(ImmutableCollection.Builder builder)
	{
		assertTrue("Repeated invocation of builder.build() has to return null: " + builder.getClass(), builder.build() == null);
		
		try
		{
			builder.add(null);
			assertTrue("Builder did not cause exception", false);
		}
		catch (IllegalStateException ex)
		{
			// No problem, expected behavior
		}
	}
}
