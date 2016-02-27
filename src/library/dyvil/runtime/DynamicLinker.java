package dyvil.runtime;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

public class DynamicLinker
{
	static class InliningCacheCallSite extends MutableCallSite
	{
		private static final int MAX_DEPTH = 3;
		
		final Lookup       lookup;
		final String       name;
		final MethodHandle fallback;
		int depth;
		
		InliningCacheCallSite(Lookup lookup, String name, MethodType type, MethodHandle fallback)
		{
			super(type);
			this.lookup = lookup;
			this.name = name;
			this.fallback = fallback;
		}
	}
	
	private static final MethodHandle CHECK_CLASS;
	private static final MethodHandle INVOKE_DYNAMIC;
	
	static
	{
		Lookup lookup = MethodHandles.lookup();
		try
		{
			CHECK_CLASS = lookup.findStatic(DynamicLinker.class, "checkClass",
			                                MethodType.methodType(boolean.class, Class.class, Object.class));
			INVOKE_DYNAMIC = lookup.findStatic(DynamicLinker.class, "invokeDynamic", MethodType
					.methodType(Object.class, InliningCacheCallSite.class, Object[].class));
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
	
	public static CallSite linkExtension(Lookup lookup, String name, MethodType type, MethodHandle fallbackImplementation)
	{
		final InliningCacheCallSite callSite = new InliningCacheCallSite(lookup, name, type, fallbackImplementation);

		// Creates a MethodHandle that, when called, passes the parameters to the 'invokeDynamic' method, with the above
		// callSite as the first argument
		final MethodHandle invokeDynamic = INVOKE_DYNAMIC.bindTo(callSite)
		                                                 .asCollector(Object[].class, type.parameterCount())
		                                                 .asType(type);
		callSite.setTarget(invokeDynamic);
		return callSite;
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
			catch (NoSuchMethodException ignored)
			{
			}

			receiver = receiver.getSuperclass();
		}
		while (receiver != null);
		return null;
	}

	public static Object invokeDynamic(InliningCacheCallSite callSite, Object[] args) throws Throwable
	{
		final MethodType type = callSite.type();
		if (callSite.depth >= InliningCacheCallSite.MAX_DEPTH)
		{
			// revert to a vtable call
			final MethodHandle virtualTarget = callSite.lookup
					.findVirtual(type.parameterType(0), callSite.name, type.dropParameterTypes(0, 1));
			callSite.setTarget(virtualTarget);
			return virtualTarget.invokeWithArguments(args);
		}

		final Object receiver = args[0];
		if (receiver == null)
		{
			return null;
		}

		final Class<?> receiverClass = receiver.getClass();

		final Method implementationMethod = findMethod(receiverClass, callSite.name,
		                                               type.dropParameterTypes(0, 1).parameterArray());

		if (implementationMethod != null)
		{
			final MethodHandle target = callSite.lookup.unreflect(implementationMethod).asType(type);

			MethodHandle test = CHECK_CLASS.bindTo(receiverClass);
			test = test.asType(test.type().changeParameterType(0, type.parameterType(0)));

			final MethodHandle guard = MethodHandles.guardWithTest(test, target, callSite.getTarget());
			callSite.depth++;

			callSite.setTarget(guard);
			return target.invokeWithArguments(args);
		}

		final MethodHandle fallbackMethod = callSite.fallback;
		if (fallbackMethod != null)
		{
			return fallbackMethod.invokeWithArguments(args);
		}

		throw new NoSuchMethodError(callSite.name);
	}
}
