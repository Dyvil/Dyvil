package dyvil.runtime;

import java.lang.invoke.*;

public class DynamicLinker
{
	private static final MethodHandle	INVOKE;
	
	static
	{
		MethodHandle invoke;
		try
		{
			invoke = MethodHandles.lookup().findStatic(DynamicLinker.class, "invoke", MethodType.methodType(Object.class, Object[].class));
			invoke = invoke.asVarargsCollector(Object[].class);
		}
		catch (NoSuchMethodException | IllegalAccessException ex)
		{
			ex.printStackTrace();
			invoke = null;
		}
		
		INVOKE = invoke;
	}
	
	public static CallSite linkMethod(MethodHandles.Lookup callerClass, String name, MethodType type) throws Throwable
	{
		return new ConstantCallSite(INVOKE);
	}
	
	protected static Object invoke(Object... args)
	{
		return null;
	}
	
	public static CallSite linkGetter(MethodHandles.Lookup callerClass, String name, Class type) throws Throwable
	{
		MethodHandle handle = callerClass.findGetter(DynamicLinker.class, name, type);
		return new ConstantCallSite(handle);
	}
	
	public static CallSite linkSetter(MethodHandles.Lookup callerClass, String name, Class type) throws Throwable
	{
		MethodHandle handle = callerClass.findSetter(DynamicLinker.class, name, type);
		return new ConstantCallSite(handle);
	}
}
