package dyvil.runtime.lambda;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.runtime.BytecodeDump;
import dyvil.runtime.TypeConverter;
import dyvilx.tools.asm.ClassWriter;
import dyvilx.tools.asm.FieldVisitor;
import dyvilx.tools.asm.MethodVisitor;
import dyvilx.tools.asm.Type;
import dyvilx.tools.asm.ASMConstants;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicInteger;

import static dyvil.reflect.LookupAccess.LOOKUP;
import static dyvil.reflect.Modifiers.*;
import static dyvil.reflect.Opcodes.*;
import static dyvil.reflect.UnsafeAccess.UNSAFE;
import static dyvil.runtime.TypeConverter.*;

public final class AnonymousClassLMF extends AbstractLMF
{
	private static final int    CLASSFILE_VERSION = 52;
	private static final String NAME_FACTORY      = "get$Lambda";

	/**
	 * Used to ensure that each spun class name is unique
	 */
	private static final AtomicInteger counter = new AtomicInteger(0);

	/* See context values in AbstractValidatingLambdaMetafactory * */

	/**
	 * Name of type containing implementation "CC"
	 */
	@NonNull
	private final String implMethodClassName;

	/**
	 * Name of implementation method "impl"
	 */
	private final String implMethodName;

	/**
	 * Type descriptor for implementation methods "(I)Ljava/lang/String;"
	 */
	private final String implMethodDesc;

	/**
	 * Class for implementation method return type "Ljava/lang/String;"
	 */
	private final Class<?> implMethodReturnClass;

	/**
	 * Generated class constructor type "(CC)void"
	 */
	private final MethodType constructorType;

	/**
	 * ASM class writer
	 */
	@NonNull
	private final ClassWriter cw;

	/**
	 * Generated names for the constructor arguments
	 */
	@NonNull
	private final String[] argNames;

	/**
	 * Type descriptors for the constructor arguments
	 */
	@NonNull
	private final String[] argDescs;

	/**
	 * Generated name for the generated class "X$Lambda$1"
	 */
	@NonNull
	private final String lambdaClassName;

	/**
	 * Field that represents the return value of the {@code toString()} method of this object. Supplied by the compiler
	 * or computed from the invoked type, i.e. the functional interface this lambda object represents.
	 */
	private String toString;

	public AnonymousClassLMF(MethodHandles.@NonNull Lookup caller, @NonNull MethodType invokedType,
		                        String samMethodName, MethodType samMethodType, @NonNull MethodHandle implMethod,
		                        MethodType instantiatedMethodType, String toString) throws LambdaConversionException
	{
		super(caller, invokedType, samMethodName, samMethodType, implMethod, instantiatedMethodType);
		this.implMethodClassName = getInternalName(this.implDefiningClass);
		this.implMethodName = this.implInfo.getName();
		this.implMethodDesc = this.implMethodType.toMethodDescriptorString();
		this.implMethodReturnClass = this.implKind == MethodHandleInfo.REF_newInvokeSpecial ?
			                             this.implDefiningClass :
			                             this.implMethodType.returnType();
		this.constructorType = invokedType.changeReturnType(Void.TYPE);
		this.lambdaClassName = getInternalName(this.targetClass) + "$Lambda$" + counter.incrementAndGet();

		this.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		int parameterCount = this.parameterCount;
		if (parameterCount > 0)
		{
			this.argNames = new String[parameterCount];
			this.argDescs = new String[parameterCount];
			for (int i = 0; i < parameterCount; i++)
			{
				this.argNames[i] = "arg$" + (i + 1);
				this.argDescs[i] = Type.getDescriptor(invokedType.parameterType(i));
			}
		}
		else
		{
			this.argNames = this.argDescs = null;
		}

		this.toString = toString;
	}

	@NonNull
	@Override
	public CallSite buildCallSite() throws LambdaConversionException
	{
		final Class<?> innerClass = this.spinInnerClass();
		if (this.parameterCount == 0)
		{
			final Constructor[] ctrs = innerClass.getDeclaredConstructors();
			if (ctrs.length != 1)
			{
				final String message =
					"Expected one lambda constructor for " + innerClass.getCanonicalName() + ", got " + ctrs.length;
				throw new LambdaConversionException(message);
			}

			try
			{
				final Constructor ctr = ctrs[0];
				ctr.setAccessible(true);
				final Object inst = ctr.newInstance();
				return new ConstantCallSite(MethodHandles.constant(this.samBase, inst));
			}
			catch (ReflectiveOperationException e)
			{
				throw new LambdaConversionException("Exception instantiating lambda object", e);
			}
		}
		try
		{
			UNSAFE.ensureClassInitialized(innerClass);
			return new ConstantCallSite(LOOKUP.findStatic(innerClass, NAME_FACTORY, this.invokedType));
		}
		catch (ReflectiveOperationException e)
		{
			throw new LambdaConversionException("Exception finding constructor", e);
		}
	}

	private Class<?> spinInnerClass() throws LambdaConversionException
	{
		String samIntf = getInternalName(this.samBase);

		this.cw.visit(CLASSFILE_VERSION, ASMConstants.ACC_SUPER | FINAL | SYNTHETIC, this.lambdaClassName, null,
		              "java/lang/Object", new String[] { samIntf });

		// Generate final fields to be filled in by constructor

		if (this.parameterCount != 0)
		{
			for (int i = 0; i < this.parameterCount; i++)
			{
				FieldVisitor fv = this.cw.visitField(PRIVATE | FINAL, this.argNames[i], this.argDescs[i], null, null);
				fv.visitEnd();
			}
			this.generateFactory();
		}

		this.generateConstructor();
		this.generateToString();

		this.generateSAM();

		this.cw.visitEnd();

		final byte[] bytes = this.cw.toByteArray();
		BytecodeDump.dump(bytes, this.lambdaClassName);

		// Define the generated class in this VM.
		return UNSAFE.defineAnonymousClass(this.targetClass, bytes, null);
	}

	private void generateFactory()
	{
		MethodVisitor m = this.cw
			                  .visitMethod(PRIVATE | STATIC, NAME_FACTORY, this.invokedType.toMethodDescriptorString(),
			                               null, null);
		m.visitCode();
		m.visitTypeInsn(NEW, this.lambdaClassName);
		m.visitInsn(Opcodes.DUP);
		int parameterCount = this.parameterCount;
		for (int typeIndex = 0, varIndex = 0; typeIndex < parameterCount; typeIndex++)
		{
			Class<?> argType = this.invokedType.parameterType(typeIndex);
			m.visitVarInsn(getLoadOpcode(argType), varIndex);
			varIndex += getParameterSize(argType);
		}
		m.visitMethodInsn(INVOKESPECIAL, this.lambdaClassName, "<init>",
		                  this.constructorType.toMethodDescriptorString(), false);
		m.visitInsn(ARETURN);
		m.visitMaxs(-1, -1);
		m.visitEnd();
	}

	private void generateConstructor()
	{
		// Generate constructor
		MethodVisitor ctor = this.cw
			                     .visitMethod(PRIVATE, "<init>", this.constructorType.toMethodDescriptorString(), null,
			                                  null);
		ctor.visitCode();
		ctor.visitVarInsn(ALOAD, 0);
		ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		int parameterCount = this.parameterCount;
		for (int i = 0, lvIndex = 0; i < parameterCount; i++)
		{
			ctor.visitVarInsn(ALOAD, 0);
			Class<?> argType = this.invokedType.parameterType(i);
			ctor.visitVarInsn(getLoadOpcode(argType), lvIndex + 1);
			lvIndex += getParameterSize(argType);
			ctor.visitFieldInsn(PUTFIELD, this.lambdaClassName, this.argNames[i], this.argDescs[i]);
		}
		ctor.visitInsn(RETURN);
		// Maxs computed by ClassWriter.COMPUTE_MAXS, these arguments ignored
		ctor.visitMaxs(-1, -1);
		ctor.visitEnd();
	}

	private void generateToString()
	{
		MethodVisitor mv = this.cw.visitMethod(Modifiers.PUBLIC, "toString", "()Ljava/lang/String;", null, null);
		mv.visitLdcInsn(this.toString);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(-1, -1);
		mv.visitEnd();
	}

	private void generateSAM()
	{
		MethodVisitor mv = this.cw
			                   .visitMethod(PUBLIC, this.samMethodName, this.samMethodType.toMethodDescriptorString(),
			                                null, null);

		mv.visitCode();

		if (this.implKind == MethodHandleInfo.REF_newInvokeSpecial)
		{
			mv.visitTypeInsn(NEW, this.implMethodClassName);
			mv.visitInsn(DUP);
		}
		for (int i = 0; i < this.parameterCount; i++)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, this.lambdaClassName, this.argNames[i], this.argDescs[i]);
		}

		this.convertArgumentTypes(mv, this.samMethodType);

		// Invoke the method we want to forward to
		mv.visitMethodInsn(invocationOpcode(this.implKind), this.implMethodClassName, this.implMethodName,
		                   this.implMethodDesc, this.implDefiningClass.isInterface());

		// Convert the return value (if any) and return it
		// Note: if adapting from non-void to void, the 'return'
		// instruction will pop the unneeded result
		Class<?> samReturnClass = this.samMethodType.returnType();
		convertType(mv, this.implMethodReturnClass, samReturnClass, samReturnClass);
		mv.visitInsn(getReturnOpcode(samReturnClass));
		// Maxs computed by ClassWriter.COMPUTE_MAXS,these arguments ignored
		mv.visitMaxs(-1, -1);
		mv.visitEnd();
	}

	private void convertArgumentTypes(@NonNull MethodVisitor mv, @NonNull MethodType samType)
	{
		int lvIndex = 0;
		int samReceiverLength;
		if (this.implIsInstanceMethod && this.parameterCount == 0)
		{
			samReceiverLength = 1;

			// push receiver
			Class<?> rcvrType = samType.parameterType(0);
			mv.visitVarInsn(getLoadOpcode(rcvrType), lvIndex + 1);
			lvIndex += getParameterSize(rcvrType);
			convertType(mv, rcvrType, this.implDefiningClass, this.instantiatedMethodType.parameterType(0));
		}
		else
		{
			samReceiverLength = 0;
		}

		int samParametersLength = samType.parameterCount();
		int argOffset = this.implMethodType.parameterCount() - samParametersLength;
		for (int i = samReceiverLength; i < samParametersLength; i++)
		{
			Class<?> argType = samType.parameterType(i);
			mv.visitVarInsn(getLoadOpcode(argType), lvIndex + 1);
			lvIndex += getParameterSize(argType);
			convertType(mv, argType, this.implMethodType.parameterType(argOffset + i),
			            this.instantiatedMethodType.parameterType(i));
		}
	}
}
