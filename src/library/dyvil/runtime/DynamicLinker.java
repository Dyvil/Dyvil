package dyvil.runtime;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

public class DynamicLinker
{
	static class InliningCacheCallSite extends MutableCallSite
	{
		/**
		 * Max supported class inheritance depth
		 */
		private static final int MAX_DEPTH = 256;

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

	private static final MethodHandle CHECK_ISCLASS;
	private static final MethodHandle INVOKE_CLASSMETHOD;

	static
	{
		Lookup lookup = MethodHandles.lookup();
		try
		{
			CHECK_CLASS = lookup.findStatic(DynamicLinker.class, "checkClass",
			                                MethodType.methodType(boolean.class, Class.class, Object.class));

			INVOKE_DYNAMIC = lookup.findStatic(DynamicLinker.class, "invokeDynamic", //
			                                   MethodType.methodType(Object.class, InliningCacheCallSite.class,
			                                                         Object[].class));

			INVOKE_CLASSMETHOD = lookup.findStatic(DynamicLinker.class, "invokeClassMethod", MethodType.methodType(
				Object.class, InliningCacheCallSite.class, Object[].class));
			CHECK_ISCLASS = lookup.findStatic(DynamicLinker.class, "checkIsClass",
			                                  MethodType.methodType(boolean.class, Class.class, Class.class));
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static CallSite linkMethod(Lookup lookup, String name, MethodType type)
	{
		return link(lookup, name, type, null, INVOKE_DYNAMIC);
	}

	public static CallSite linkExtension(Lookup lookup, String name, MethodType type,
		                                    MethodHandle fallbackImplementation)
	{
		return link(lookup, name, type, fallbackImplementation, INVOKE_DYNAMIC);
	}

	public static CallSite linkClassMethod(Lookup lookup, String name, MethodType type)
	{
		return link(lookup, name, type, null, INVOKE_CLASSMETHOD);
	}

	private static CallSite link(Lookup lookup, String name, MethodType type, MethodHandle fallbackImplementation,
		                            MethodHandle dynamicTarget)
	{
		final InliningCacheCallSite callSite = new InliningCacheCallSite(lookup, name, type, fallbackImplementation);

		// Creates a MethodHandle that, when called, passes the parameters to the 'invokeDynamic' method, with the above
		// callSite as the first argument
		final MethodHandle invokeDynamic = dynamicTarget.bindTo(callSite)
		                                                .asCollector(Object[].class, type.parameterCount())
		                                                .asType(type);
		callSite.setTarget(invokeDynamic);
		return callSite;
	}

	public static boolean checkClass(Class<?> clazz, Object receiver)
	{
		return receiver.getClass() == clazz;
	}

	public static boolean checkIsClass(Class<?> clazz, Class<?> receiver)
	{
		return receiver == clazz;
	}

	public static Method findMethod(Class<?> receiver, String name, Class[] parameterTypes) throws Throwable
	{
		try
		{
			return receiver.getMethod(name, parameterTypes);
		}
		catch (NoSuchMethodException ex)
		{
			return null;
		}
	}

	public static Object invokeDynamic(InliningCacheCallSite callSite, Object[] args) throws Throwable
	{
		final MethodType type = callSite.type();
		if (callSite.depth >= InliningCacheCallSite.MAX_DEPTH)
		{
			// revert to a vtable call
			return invokeVirtual(callSite, args, type);
		}

		final Object receiver = args[0];
		if (receiver == null)
		{
			throw new NullPointerException();
		}
		final Class<?> receiverClass = receiver.getClass();

		final Method implementationMethod = findMethod(receiverClass, callSite.name,
		                                               type.dropParameterTypes(0, 1).parameterArray());

		if (implementationMethod != null)
		{
			// Convert the implementation to a MethodHandle with the desired type
			final MethodHandle target = callSite.lookup.unreflect(implementationMethod).asType(type);
			return invokeWith(callSite, args, type, receiverClass, target, CHECK_CLASS);
		}

		final MethodHandle fallbackMethod = callSite.fallback;
		if (fallbackMethod != null)
		{
			return invokeWith(callSite, args, type, receiverClass, fallbackMethod, CHECK_CLASS);
		}

		throw new NoSuchMethodError(callSite.name);
	}

	public static Object invokeClassMethod(InliningCacheCallSite callSite, Object[] args) throws Throwable
	{
		final MethodType type = callSite.type();
		if (callSite.depth >= InliningCacheCallSite.MAX_DEPTH)
		{
			// revert to a vtable call
			return invokeVirtual(callSite, args, type);
		}

		final Class<?> receiver = (Class<?>) args[0];

		final Method implementationMethod = findMethod(receiver, callSite.name,
		                                               type.dropParameterTypes(0, 1).parameterArray());

		if (implementationMethod != null)
		{
			// Convert the implementation to a MethodHandle with the desired type
			final MethodHandle target = callSite.lookup.unreflect(implementationMethod).asType(type);
			return invokeWith(callSite, args, type, receiver, target, CHECK_ISCLASS);
		}

		throw new NoSuchMethodError(callSite.name);
	}

	private static Object invokeVirtual(InliningCacheCallSite callSite, Object[] args, MethodType type) throws Throwable
	{
		final MethodHandle virtualTarget = callSite.lookup.findVirtual(type.parameterType(0), callSite.name,
		                                                               type.dropParameterTypes(0, 1));
		callSite.setTarget(virtualTarget);
		return virtualTarget.invokeWithArguments(args);
	}

	private static Object invokeWith(InliningCacheCallSite callSite, Object[] args, MethodType type,
		                                Class<?> receiverClass, MethodHandle target, MethodHandle receiverCheck)
		throws Throwable
	{
		MethodHandle test = receiverCheck.bindTo(receiverClass);
		test = test.asType(test.type().changeParameterType(0, type.parameterType(0)));

		// Creates a method that, when called, first tests the type of the first argument (the receiver) to be the
		// required receiver type. If that succeeds, the target is called. Otherwise, this invokeDynamic method is
		// called again
		final MethodHandle guard = MethodHandles.guardWithTest(test, target, callSite.getTarget());

		// Set the guard method as the new callsite target to speed up further invocations
		callSite.depth++;
		callSite.setTarget(guard);

		// Invoke the target and return the result
		return target.invokeWithArguments(args);
	}
}
