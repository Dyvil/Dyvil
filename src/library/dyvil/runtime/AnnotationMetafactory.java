package dyvil.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import dyvil.runtime.annotation.AnnotationProxyFactory;

public class AnnotationMetafactory
{
	public static CallSite metafactory(MethodHandles.Lookup caller, String invokedName, MethodType invokedType, Object... values) throws Exception
	{
		AnnotationProxyFactory factory = new AnnotationProxyFactory(caller, invokedType, values);
		return factory.buildCallSite();
	}
}
