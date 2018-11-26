package dyvil.runtime;

import dyvil.annotation.internal.NonNull;

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

		InliningCacheCallSite(Lookup lookup, String name, @NonNull MethodType type, MethodHandle fallback)
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

	public static @NonNull CallSite linkMethod(@NonNull Lookup lookup, @NonNull String name, @NonNull MethodType type)
	{
		return link(lookup, name, type, null, INVOKE_DYNAMIC);
	}

	public static @NonNull CallSite linkExtension(@NonNull Lookup lookup, @NonNull String name,
		@NonNull MethodType type, @NonNull MethodHandle fallback)
	{
		return link(lookup, name, type, fallback, INVOKE_DYNAMIC);
	}

	public static @NonNull CallSite linkClassMethod(@NonNull Lookup lookup, @NonNull String name,
		@NonNull MethodType type)
	{
		return link(lookup, name, type, null, INVOKE_CLASSMETHOD);
	}

	private static @NonNull CallSite link(@NonNull Lookup lookup, @NonNull String name, @NonNull MethodType type,
		MethodHandle fallback, @NonNull MethodHandle dynamicTarget)
	{
		final InliningCacheCallSite callSite = new InliningCacheCallSite(lookup, name, type, fallback);

		// Creates a MethodHandle that, when called, passes the parameters to the 'invokeDynamic' method, with the above
		// callSite as the first argument
		final MethodHandle invokeDynamic = dynamicTarget.bindTo(callSite)
		                                                .asCollector(Object[].class, type.parameterCount())
		                                                .asType(type);
		callSite.setTarget(invokeDynamic);
		return callSite;
	}

	public static boolean checkClass(@NonNull Class<?> clazz, @NonNull Object receiver)
	{
		return receiver.getClass() == clazz;
	}

	public static boolean checkIsClass(@NonNull Class<?> clazz, @NonNull Class<?> receiver)
	{
		return receiver == clazz;
	}

	public static Object invokeDynamic(@NonNull InliningCacheCallSite callSite, Object @NonNull [] args)
		throws Throwable
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

		try
		{
			final Method implementationMethod = receiverClass.getMethod(callSite.name,
			                                                            type.dropParameterTypes(0, 1).parameterArray());
			// Convert the implementation to a MethodHandle with the desired type
			final MethodHandle target = callSite.lookup.unreflect(implementationMethod).asType(type);
			return invokeWith(callSite, args, type, receiverClass, target, CHECK_CLASS);
		}
		catch (NoSuchMethodException ex)
		{
			final MethodHandle fallbackMethod = callSite.fallback;
			if (fallbackMethod != null)
			{
				return invokeWith(callSite, args, type, receiverClass, fallbackMethod, CHECK_CLASS);
			}

			throw ex;
		}
	}

	public static Object invokeClassMethod(@NonNull InliningCacheCallSite callSite, Object @NonNull [] args)
		throws Throwable
	{
		final MethodType type = callSite.type();
		if (callSite.depth >= InliningCacheCallSite.MAX_DEPTH)
		{
			// revert to a vtable call
			return invokeVirtual(callSite, args, type);
		}

		final Class<?> receiver = (Class<?>) args[0];
		final MethodType targetType = type.dropParameterTypes(0, 1);
		final MethodHandle target = callSite.lookup.findStatic(receiver, callSite.name, targetType).asType(targetType);

		return invokeWith(callSite, args, type, receiver, MethodHandles.dropArguments(target, 0, Class.class),
		                  CHECK_ISCLASS);
	}

	private static Object invokeVirtual(@NonNull InliningCacheCallSite callSite, Object @NonNull [] args,
		@NonNull MethodType type) throws Throwable
	{
		final MethodHandle virtualTarget = callSite.lookup.findVirtual(type.parameterType(0), callSite.name,
		                                                               type.dropParameterTypes(0, 1));
		callSite.setTarget(virtualTarget);
		return virtualTarget.invokeWithArguments(args);
	}

	private static Object invokeWith(@NonNull InliningCacheCallSite callSite, Object @NonNull [] args,
		@NonNull MethodType type, @NonNull Class<?> receiverClass, @NonNull MethodHandle target,
		@NonNull MethodHandle receiverCheck) throws Throwable
	{
		MethodHandle test = receiverCheck.bindTo(receiverClass);
		test = test.asType(test.type().changeParameterType(0, type.parameterType(0)));

		target = target.asFixedArity();

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
