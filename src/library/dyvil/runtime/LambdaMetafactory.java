package dyvil.runtime;

import dyvil.annotation.internal.NonNull;
import dyvil.runtime.lambda.AbstractLMF;
import dyvil.runtime.lambda.AnonymousClassLMF;

import java.lang.invoke.*;

public class LambdaMetafactory
{
	private static final Class<?>[]   EMPTY_CLASS_ARRAY = new Class<?>[0];
	private static final MethodType[] EMPTY_MT_ARRAY    = new MethodType[0];

	public static CallSite metafactory(MethodHandles.@NonNull Lookup caller, String invokedName,
		                                  @NonNull MethodType invokedType, MethodType samMethodType,
		                                  @NonNull MethodHandle implMethod, MethodType instantiatedMethodType)
		throws LambdaConversionException
	{
		String type = '<' + invokedType.returnType().getName() + "::" + invokedName + '>';
		return metafactory(caller, invokedName, invokedType, samMethodType, implMethod, instantiatedMethodType, type);
	}

	public static CallSite metafactory(MethodHandles.@NonNull Lookup caller, String invokedName,
		                                  @NonNull MethodType invokedType, MethodType samMethodType,
		                                  @NonNull MethodHandle implMethod, MethodType instantiatedMethodType,
		                                  String toString) throws LambdaConversionException
	{

		AbstractLMF mf = new AnonymousClassLMF(caller, invokedType, invokedName, samMethodType, implMethod,
		                                       instantiatedMethodType, toString);
		mf.validateMetafactoryArgs();
		return mf.buildCallSite();
	}
}
