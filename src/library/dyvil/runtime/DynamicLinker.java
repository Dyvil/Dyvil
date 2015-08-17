package dyvil.runtime;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

public class DynamicLinker
{
	static class InliningCacheCallSite extends MutableCallSite
	{
		private static final int MAX_DEPTH = 3;
		
		final Lookup		lookup;
		final String		name;
		final MethodHandle	fallback;
		int					depth;
		
		InliningCacheCallSite(Lookup lookup, String name, MethodType type, MethodHandle fallback)
		{
			super(type);
			this.lookup = lookup;
			this.name = name;
			this.fallback = fallback;
		}
	}
	
	private static final MethodHandle	CHECK_CLASS;
	private static final MethodHandle	FALLBACK;
	
	static
	{
		Lookup lookup = MethodHandles.lookup();
		try
		{
			CHECK_CLASS = lookup.findStatic(DynamicLinker.class, "checkClass", MethodType.methodType(boolean.class, Class.class, Object.class));
			FALLBACK = lookup.findStatic(DynamicLinker.class, "fallback", MethodType.methodType(Object.class, InliningCacheCallSite.class, Object[].class));
		}
		catch (ReflectiveOperationException e)
		{
			throw new AssertionError("", e);
		}
	}
	
	public static CallSite linkMethod(Lookup lookup, String name, MethodType type)
	{
		return linkExtension(lookup, name, type, null);
	}
	
	public static boolean checkClass(Class<?> clazz, Object receiver)
	{
		return receiver.getClass() == clazz;
	}
	
	public static Method findMethod(Class<?> receiver, String name, Class[] parameterTypes) throws Throwable
	{
		do
		{
			try
			{
				return receiver.getDeclaredMethod(name, parameterTypes);
			}
			catch (NoSuchMethodException ex)
			{
			}
			
			receiver = receiver.getSuperclass();
		}
		while (receiver != null);
		return null;
	}
	
	public static Object fallback(InliningCacheCallSite callSite, Object[] args) throws Throwable
	{
		MethodType type = callSite.type();
		if (callSite.depth >= InliningCacheCallSite.MAX_DEPTH)
		{
			// revert to a vtable call
			MethodHandle target = callSite.lookup.findVirtual(type.parameterType(0), callSite.name, type.dropParameterTypes(0, 1));
			callSite.setTarget(target);
			return target.invokeWithArguments(args);
		}
		
		Object receiver = args[0];
		if (receiver == null)
		{
			return null;
		}
		
		Class<?> receiverClass = receiver.getClass();
		Method m = findMethod(receiverClass, callSite.name, type.dropParameterTypes(0, 1).parameterArray());
		
		if (m == null)
		{
			MethodHandle fallback = callSite.fallback;
			if (fallback == null)
			{
				throw new NoSuchMethodError(callSite.name);
			}
			return fallback.invokeWithArguments(args);
		}
		
		MethodHandle target = callSite.lookup.unreflect(m);
		target = target.asType(type);
		
		MethodHandle test = CHECK_CLASS.bindTo(receiverClass);
		test = test.asType(test.type().changeParameterType(0, type.parameterType(0)));
		
		MethodHandle guard = MethodHandles.guardWithTest(test, target, callSite.getTarget());
		callSite.depth++;
		
		callSite.setTarget(guard);
		return target.invokeWithArguments(args);
	}
	
	public static CallSite linkExtension(Lookup lookup, String name, MethodType type, MethodHandle fallback)
	{
		InliningCacheCallSite callSite = new InliningCacheCallSite(lookup, name, type, fallback);
		MethodHandle fb = FALLBACK.bindTo(callSite).asCollector(Object[].class, type.parameterCount()).asType(type);
		callSite.setTarget(fb);
		return callSite;
	}
}
