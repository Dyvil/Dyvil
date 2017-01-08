package dyvil.runtime;

import dyvil.annotation.internal.NonNull;
import dyvil.runtime.annotation.AnnotationProxyFactory;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class AnnotationMetafactory
{
	@NonNull
	public static CallSite metafactory(MethodHandles.@NonNull Lookup caller, String invokedName,
		                                  @NonNull MethodType invokedType, Object... values) throws Exception
	{
		AnnotationProxyFactory factory = new AnnotationProxyFactory(caller, invokedType, values);
		return factory.buildCallSite();
	}
}
