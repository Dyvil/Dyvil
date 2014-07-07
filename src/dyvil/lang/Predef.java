package dyvil.lang;

import dyvil.lang.annotation.implicit;
import dyvil.lang.tuple.Tuple2;

public class Predef
{
	// ->
	public static @implicit <A, B> Tuple2<A, B> $arr(A a, B b)
	{
		return new Tuple2(a, b);
	}
	
	// <-
	public static @implicit <A, B> Tuple2<B, A> $arl(A a, B b)
	{
		return new Tuple2(b, a);
	}
}
