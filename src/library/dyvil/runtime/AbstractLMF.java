package dyvil.runtime;

import java.lang.invoke.*;

import sun.invoke.util.Wrapper;
import static sun.invoke.util.Wrapper.forPrimitiveType;
import static sun.invoke.util.Wrapper.forWrapperType;
import static sun.invoke.util.Wrapper.isWrapperType;

public abstract class AbstractLMF
{
	
	/**
	 * For context, the comments for the following fields are marked in quotes
	 * with their values, given this program:
	 * 
	 * <pre>
	 * interface II&lt;T&gt;
	 * {
	 * 	Object foo(T x);
	 * }
	 * 
	 * interface JJ&lt;R extends Number&gt; extends II&lt;R&gt;
	 * {
	 * }
	 * 
	 * class CC
	 * {
	 * 	String impl(int i)
	 * 	{
	 * 		return &quot;impl:&quot; + i;
	 * 	}
	 * }
	 * 
	 * class X
	 * {
	 * 	public static void main(String[] args)
	 * 	{
	 * 		JJ&lt;Integer&gt; iii = (new CC())::impl;
	 * 		System.out.printf(&quot;&gt;&gt;&gt; %s\n&quot;, iii.foo(44));
	 * 	}
	 * }
	 * </pre>
	 */
	
	/**
	 * The class calling the meta-factory via invokedynamic "class X"
	 */
	final Class<?>						targetClass;
	/** The type of the invoked method "(CC)II" */
	protected final MethodType			invokedType;
	/**
	 * The type of the returned instance "interface JJ"
	 */
	protected final Class<?>			samBase;
	/**
	 * Name of the SAM method "foo"
	 */
	protected final String				samMethodName;
	/**
	 * Type of the SAM method "(Object)Object"
	 */
	protected final MethodType			samMethodType;
	/**
	 * Raw method handle for the implementation method
	 */
	protected final MethodHandle		implMethod;
	/**
	 * Info about the implementation method handle
	 * "MethodHandleInfo[5 CC.impl(int)String]"
	 */
	protected final MethodHandleInfo	implInfo;
	/**
	 * Invocation kind for implementation "5"=invokevirtual
	 */
	protected final int					implKind;
	/**
	 * Is the implementation an instance method "true"
	 */
	protected final boolean				implIsInstanceMethod;
	/**
	 * Type defining the implementation "class CC"
	 */
	protected final Class<?>			implDefiningClass;
	/**
	 * Type of the implementation method "(int)String"
	 */
	protected final MethodType			implMethodType;
	/**
	 * Instantiated erased functional interface method type "(Integer)Object"
	 */
	protected final MethodType			instantiatedMethodType;
	
	protected AbstractLMF(MethodHandles.Lookup caller, MethodType invokedType, String samMethodName, MethodType samMethodType, MethodHandle implMethod,
			MethodType instantiatedMethodType) throws LambdaConversionException
	{
		if ((caller.lookupModes() & MethodHandles.Lookup.PRIVATE) == 0)
		{
			throw new LambdaConversionException(String.format("Invalid caller: %s", caller.lookupClass().getName()));
		}
		this.targetClass = caller.lookupClass();
		this.invokedType = invokedType;
		
		this.samBase = invokedType.returnType();
		
		this.samMethodName = samMethodName;
		this.samMethodType = samMethodType;
		
		this.implMethod = implMethod;
		this.implInfo = caller.revealDirect(implMethod);
		this.implKind = this.implInfo.getReferenceKind();
		this.implIsInstanceMethod = this.implKind == MethodHandleInfo.REF_invokeVirtual || this.implKind == MethodHandleInfo.REF_invokeSpecial
				|| this.implKind == MethodHandleInfo.REF_invokeInterface;
		this.implDefiningClass = this.implInfo.getDeclaringClass();
		this.implMethodType = this.implInfo.getMethodType();
		this.instantiatedMethodType = instantiatedMethodType;
		
		if (!this.samBase.isInterface())
		{
			throw new LambdaConversionException(String.format("Functional interface %s is not an interface", this.samBase.getName()));
		}
	}
	
	public abstract CallSite buildCallSite() throws LambdaConversionException;
	
	protected void validateMetafactoryArgs() throws LambdaConversionException
	{
		switch (this.implKind)
		{
		case MethodHandleInfo.REF_invokeInterface:
		case MethodHandleInfo.REF_invokeVirtual:
		case MethodHandleInfo.REF_invokeStatic:
		case MethodHandleInfo.REF_newInvokeSpecial:
		case MethodHandleInfo.REF_invokeSpecial:
			break;
		default:
			throw new LambdaConversionException(String.format("Unsupported MethodHandle kind: %s", this.implInfo));
		}
		
		// Check arity: optional-receiver + captured + SAM == impl
		final int implArity = this.implMethodType.parameterCount();
		final int receiverArity = this.implIsInstanceMethod ? 1 : 0;
		final int capturedArity = this.invokedType.parameterCount();
		final int samArity = this.samMethodType.parameterCount();
		final int instantiatedArity = this.instantiatedMethodType.parameterCount();
		if (implArity + receiverArity != capturedArity + samArity)
		{
			throw new LambdaConversionException(
					String.format(
							"Incorrect number of parameters for %s method %s; %d captured parameters, %d functional interface method parameters, %d implementation parameters",
							this.implIsInstanceMethod ? "instance" : "static", this.implInfo, capturedArity, samArity, implArity));
		}
		if (instantiatedArity != samArity)
		{
			throw new LambdaConversionException(String.format(
					"Incorrect number of parameters for %s method %s; %d instantiated parameters, %d functional interface method parameters",
					this.implIsInstanceMethod ? "instance" : "static", this.implInfo, instantiatedArity, samArity));
		}
		
		// If instance: first captured arg (receiver) must be subtype of class
		// where impl method is defined
		final int capturedStart;
		final int samStart;
		if (this.implIsInstanceMethod)
		{
			final Class<?> receiverClass;
			
			// implementation is an instance method, adjust for receiver in
			// captured variables / SAM arguments
			if (capturedArity == 0)
			{
				// receiver is function parameter
				capturedStart = 0;
				samStart = 1;
				receiverClass = this.instantiatedMethodType.parameterType(0);
			}
			else
			{
				// receiver is a captured variable
				capturedStart = 1;
				samStart = 0;
				receiverClass = this.invokedType.parameterType(0);
			}
			
			// check receiver type
			if (!this.implDefiningClass.isAssignableFrom(receiverClass))
			{
				throw new LambdaConversionException(String.format("Invalid receiver type %s; not a subtype of implementation type %s", receiverClass,
						this.implDefiningClass));
			}
			
			Class<?> implReceiverClass = this.implMethod.type().parameterType(0);
			if (implReceiverClass != this.implDefiningClass && !implReceiverClass.isAssignableFrom(receiverClass))
			{
				throw new LambdaConversionException(String.format("Invalid receiver type %s; not a subtype of implementation receiver type %s", receiverClass,
						implReceiverClass));
			}
		}
		else
		{
			// no receiver
			capturedStart = 0;
			samStart = 0;
		}
		
		// Check for exact match on non-receiver captured arguments
		final int implFromCaptured = capturedArity - capturedStart;
		for (int i = 0; i < implFromCaptured; i++)
		{
			Class<?> implParamType = this.implMethodType.parameterType(i);
			Class<?> capturedParamType = this.invokedType.parameterType(i + capturedStart);
			if (!capturedParamType.equals(implParamType))
			{
				throw new LambdaConversionException(String.format("Type mismatch in captured lambda parameter %d: expecting %s, found %s", i,
						capturedParamType, implParamType));
			}
		}
		// Check for adaptation match on SAM arguments
		final int samOffset = samStart - implFromCaptured;
		for (int i = implFromCaptured; i < implArity; i++)
		{
			Class<?> implParamType = this.implMethodType.parameterType(i);
			Class<?> instantiatedParamType = this.instantiatedMethodType.parameterType(i + samOffset);
			if (!isAdaptableTo(instantiatedParamType, implParamType, true))
			{
				throw new LambdaConversionException(String.format("Type mismatch for lambda argument %d: %s is not convertible to %s", i,
						instantiatedParamType, implParamType));
			}
		}
		
		// Adaptation match: return type
		Class<?> expectedType = this.instantiatedMethodType.returnType();
		Class<?> actualReturnType = this.implKind == MethodHandleInfo.REF_newInvokeSpecial ? this.implDefiningClass : this.implMethodType.returnType();
		Class<?> samReturnType = this.samMethodType.returnType();
		if (!isAdaptableToAsReturn(actualReturnType, expectedType))
		{
			throw new LambdaConversionException(String.format("Type mismatch for lambda return: %s is not convertible to %s", actualReturnType, expectedType));
		}
		if (!isAdaptableToAsReturnStrict(expectedType, samReturnType))
		{
			throw new LambdaConversionException(String.format("Type mismatch for lambda expected return: %s is not convertible to %s", expectedType,
					samReturnType));
		}
	}
	
	private static boolean isAdaptableTo(Class<?> fromType, Class<?> toType, boolean strict)
	{
		if (fromType.equals(toType))
		{
			return true;
		}
		if (fromType.isPrimitive())
		{
			Wrapper wfrom = forPrimitiveType(fromType);
			if (toType.isPrimitive())
			{
				// both are primitive: widening
				Wrapper wto = forPrimitiveType(toType);
				return wto.isConvertibleFrom(wfrom);
			}
			// from primitive to reference: boxing
			return toType.isAssignableFrom(wfrom.wrapperType());
		}
		if (toType.isPrimitive())
		{
			// from reference to primitive: unboxing
			Wrapper wfrom;
			if (isWrapperType(fromType) && (wfrom = forWrapperType(fromType)).primitiveType().isPrimitive())
			{
				// fromType is a primitive wrapper; unbox+widen
				Wrapper wto = forPrimitiveType(toType);
				return wto.isConvertibleFrom(wfrom);
			}
			// must be convertible to primitive
			return !strict;
		}
		// both are reference types: fromType should be a superclass of toType.
		return !strict || toType.isAssignableFrom(fromType);
	}
	
	private static boolean isAdaptableToAsReturn(Class<?> fromType, Class<?> toType)
	{
		return toType.equals(void.class) || !fromType.equals(void.class) && isAdaptableTo(fromType, toType, false);
	}
	
	private static boolean isAdaptableToAsReturnStrict(Class<?> fromType, Class<?> toType)
	{
		if (fromType.equals(void.class))
		{
			return toType.equals(void.class);
		}
		return isAdaptableTo(fromType, toType, true);
	}
}
