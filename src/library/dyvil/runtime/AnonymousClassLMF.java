package dyvil.runtime;

import java.io.File;
import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

import dyvil.io.FileUtils;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.asm.ClassWriter;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.asm.Type;

import sun.misc.Unsafe;

import static dyvil.reflect.Modifiers.*;
import static dyvil.reflect.Opcodes.*;

public final class AnonymousClassLMF extends AbstractLMF
{
	private static final Unsafe					UNSAFE					= ReflectUtils.unsafe;
	
	private static final MethodHandles.Lookup	LOOKUP;
	
	static
	{
		Lookup lookup;
		try
		{
			Field f = Lookup.class.getDeclaredField("IMPL_LOOKUP");
			f.setAccessible(true);
			lookup = (Lookup) f.get(null);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			lookup = null;
		}
		LOOKUP = lookup;
	}
	
	private static final int					CLASSFILE_VERSION		= 52;
	private static final String					METHOD_DESCRIPTOR_VOID	= "()V";
	private static final String					JAVA_LANG_OBJECT		= "java/lang/Object";
	private static final String					NAME_CTOR				= "<init>";
	private static final String					NAME_FACTORY			= "get$Lambda";
	
	private static final String[]				EMPTY_STRING_ARRAY		= new String[0];
	
	/**
	 * Directory to dump generated class file bytecode.
	 */
	private static final File					dumpDirectory			= null;
	
	/** Used to ensure that each spun class name is unique */
	private static final AtomicInteger			counter					= new AtomicInteger(0);
	
	/* See context values in AbstractValidatingLambdaMetafactory * */
	
	/**
	 * Name of type containing implementation "CC"
	 */
	private final String						implMethodClassName;
	
	/**
	 * Name of implementation method "impl"
	 */
	private final String						implMethodName;
	
	/**
	 * Type descriptor for implementation methods "(I)Ljava/lang/String;"
	 */
	private final String						implMethodDesc;
	
	/**
	 * Class for implementation method return type "Ljava/lang/String;"
	 */
	private final Class<?>						implMethodReturnClass;
	
	/**
	 * Generated class constructor type "(CC)void"
	 */
	private final MethodType					constructorType;
	
	/**
	 * ASM class writer
	 */
	private final ClassWriter					cw;
	
	/**
	 * Generated names for the constructor arguments
	 */
	private final String[]						argNames;
	
	/**
	 * Type descriptors for the constructor arguments
	 */
	private final String[]						argDescs;
	
	/**
	 * Generated name for the generated class "X$Lambda$1"
	 */
	private final String						lambdaClassName;
	
	/**
	 * Field that represents the return value of the {@code toString()} method
	 * of this object. Supplied by the compiler or computed from the invoked
	 * type, i.e. the functional interface this lambda object represents.
	 */
	private String								toString;
	
	public AnonymousClassLMF(MethodHandles.Lookup caller, MethodType invokedType, String samMethodName, MethodType samMethodType, MethodHandle implMethod,
			MethodType instantiatedMethodType, String toString) throws LambdaConversionException
	{
		super(caller, invokedType, samMethodName, samMethodType, implMethod, instantiatedMethodType);
		this.implMethodClassName = this.implDefiningClass.getName().replace('.', '/');
		this.implMethodName = this.implInfo.getName();
		this.implMethodDesc = this.implMethodType.toMethodDescriptorString();
		this.implMethodReturnClass = this.implKind == MethodHandleInfo.REF_newInvokeSpecial ? this.implDefiningClass : this.implMethodType.returnType();
		this.constructorType = invokedType.changeReturnType(Void.TYPE);
		this.lambdaClassName = this.targetClass.getName().replace('.', '/') + "$Lambda$" + counter.incrementAndGet();
		this.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		int parameterCount = invokedType.parameterCount();
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
			this.argNames = this.argDescs = EMPTY_STRING_ARRAY;
		}
		
		this.toString = toString;
	}
	
	@Override
	public CallSite buildCallSite() throws LambdaConversionException
	{
		final Class<?> innerClass = this.spinInnerClass();
		if (this.invokedType.parameterCount() == 0)
		{
			final Constructor[] ctrs = AccessController.doPrivileged((PrivilegedAction<Constructor[]>) () -> {
				Constructor<?>[] ctrs1 = innerClass.getDeclaredConstructors();
				if (ctrs1.length == 1)
				{
					// The lambda implementing inner class constructor is
					// private, set
					// it accessible (by us) before creating the constant
					// sole instance
					ctrs1[0].setAccessible(true);
				}
				return ctrs1;
			});
			if (ctrs.length != 1)
			{
				throw new LambdaConversionException("Expected one lambda constructor for " + innerClass.getCanonicalName() + ", got " + ctrs.length);
			}
			
			try
			{
				Object inst = ctrs[0].newInstance();
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
		String samIntf = this.samBase.getName().replace('.', '/');
		
		this.cw.visit(CLASSFILE_VERSION, dyvil.tools.asm.Opcodes.ACC_SUPER | FINAL | SYNTHETIC, this.lambdaClassName, null, JAVA_LANG_OBJECT,
				new String[] { samIntf });
		
		// Generate final fields to be filled in by constructor
		for (int i = 0; i < this.argDescs.length; i++)
		{
			FieldVisitor fv = this.cw.visitField(PRIVATE | FINAL, this.argNames[i], this.argDescs[i], null, null);
			fv.visitEnd();
		}
		
		this.generateConstructor();
		this.generateToString();
		
		if (this.invokedType.parameterCount() != 0)
		{
			this.generateFactory();
		}
		
		// Forward the SAM method
		MethodVisitor mv = this.cw.visitMethod(PUBLIC, this.samMethodName, this.samMethodType.toMethodDescriptorString(), null, null);
		new ForwardingMethodGenerator(mv).generate(this.samMethodType);
		
		this.cw.visitEnd();
		
		byte[] bytes = this.cw.toByteArray();
		
		if (dumpDirectory != null)
		{
			File dumpFile = new File(dumpDirectory, this.lambdaClassName.replace('/', File.separatorChar).concat(".class"));
			FileUtils.write(dumpFile, bytes);
		}
		
		// Define the generated class in this VM.
		
		return UNSAFE.defineAnonymousClass(this.targetClass, bytes, null);
	}
	
	private void generateFactory()
	{
		MethodVisitor m = this.cw.visitMethod(PRIVATE | STATIC, NAME_FACTORY, this.invokedType.toMethodDescriptorString(), null, null);
		m.visitCode();
		m.visitTypeInsn(NEW, this.lambdaClassName);
		m.visitInsn(Opcodes.DUP);
		int parameterCount = this.invokedType.parameterCount();
		for (int typeIndex = 0, varIndex = 0; typeIndex < parameterCount; typeIndex++)
		{
			Class<?> argType = this.invokedType.parameterType(typeIndex);
			m.visitVarInsn(getLoadOpcode(argType), varIndex);
			varIndex += getParameterSize(argType);
		}
		m.visitMethodInsn(INVOKESPECIAL, this.lambdaClassName, NAME_CTOR, this.constructorType.toMethodDescriptorString(), false);
		m.visitInsn(ARETURN);
		m.visitMaxs(-1, -1);
		m.visitEnd();
	}
	
	private void generateConstructor()
	{
		// Generate constructor
		MethodVisitor ctor = this.cw.visitMethod(PRIVATE, NAME_CTOR, this.constructorType.toMethodDescriptorString(), null, null);
		ctor.visitCode();
		ctor.visitVarInsn(ALOAD, 0);
		ctor.visitMethodInsn(INVOKESPECIAL, JAVA_LANG_OBJECT, NAME_CTOR, METHOD_DESCRIPTOR_VOID, false);
		int parameterCount = this.invokedType.parameterCount();
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
	
	private class ForwardingMethodGenerator
	{
		private MethodVisitor	mv;
		
		ForwardingMethodGenerator(MethodVisitor mv)
		{
			this.mv = mv;
		}
		
		void generate(MethodType methodType)
		{
			this.mv.visitCode();
			
			if (AnonymousClassLMF.this.implKind == MethodHandleInfo.REF_newInvokeSpecial)
			{
				this.mv.visitTypeInsn(NEW, AnonymousClassLMF.this.implMethodClassName);
				this.mv.visitInsn(DUP);
			}
			for (int i = 0; i < AnonymousClassLMF.this.argNames.length; i++)
			{
				this.mv.visitVarInsn(ALOAD, 0);
				this.mv.visitFieldInsn(GETFIELD, AnonymousClassLMF.this.lambdaClassName, AnonymousClassLMF.this.argNames[i], AnonymousClassLMF.this.argDescs[i]);
			}
			
			this.convertArgumentTypes(methodType);
			
			// Invoke the method we want to forward to
			this.mv.visitMethodInsn(this.invocationOpcode(), AnonymousClassLMF.this.implMethodClassName, AnonymousClassLMF.this.implMethodName,
					AnonymousClassLMF.this.implMethodDesc, AnonymousClassLMF.this.implDefiningClass.isInterface());
			
			// Convert the return value (if any) and return it
			// Note: if adapting from non-void to void, the 'return'
			// instruction will pop the unneeded result
			Class<?> samReturnClass = methodType.returnType();
			TypeConverter.convertType(this.mv, AnonymousClassLMF.this.implMethodReturnClass, samReturnClass, samReturnClass);
			this.mv.visitInsn(getReturnOpcode(samReturnClass));
			// Maxs computed by ClassWriter.COMPUTE_MAXS,these arguments ignored
			this.mv.visitMaxs(-1, -1);
			this.mv.visitEnd();
		}
		
		private void convertArgumentTypes(MethodType samType)
		{
			int lvIndex = 0;
			boolean samIncludesReceiver = AnonymousClassLMF.this.implIsInstanceMethod && AnonymousClassLMF.this.invokedType.parameterCount() == 0;
			int samReceiverLength = samIncludesReceiver ? 1 : 0;
			if (samIncludesReceiver)
			{
				// push receiver
				Class<?> rcvrType = samType.parameterType(0);
				this.mv.visitVarInsn(getLoadOpcode(rcvrType), lvIndex + 1);
				lvIndex += getParameterSize(rcvrType);
				TypeConverter.convertType(this.mv, rcvrType, AnonymousClassLMF.this.implDefiningClass,
						AnonymousClassLMF.this.instantiatedMethodType.parameterType(0));
			}
			int samParametersLength = samType.parameterCount();
			int argOffset = AnonymousClassLMF.this.implMethodType.parameterCount() - samParametersLength;
			for (int i = samReceiverLength; i < samParametersLength; i++)
			{
				Class<?> argType = samType.parameterType(i);
				this.mv.visitVarInsn(getLoadOpcode(argType), lvIndex + 1);
				lvIndex += getParameterSize(argType);
				TypeConverter.convertType(this.mv, argType, AnonymousClassLMF.this.implMethodType.parameterType(argOffset + i),
						AnonymousClassLMF.this.instantiatedMethodType.parameterType(i));
			}
		}
		
		private int invocationOpcode() throws InternalError
		{
			switch (AnonymousClassLMF.this.implKind)
			{
			case MethodHandleInfo.REF_invokeStatic:
				return INVOKESTATIC;
			case MethodHandleInfo.REF_newInvokeSpecial:
				return INVOKESPECIAL;
			case MethodHandleInfo.REF_invokeVirtual:
				return INVOKEVIRTUAL;
			case MethodHandleInfo.REF_invokeInterface:
				return INVOKEINTERFACE;
			case MethodHandleInfo.REF_invokeSpecial:
				return INVOKESPECIAL;
			default:
				throw new InternalError("Unexpected invocation kind: " + AnonymousClassLMF.this.implKind);
			}
		}
	}
	
	static int getParameterSize(Class<?> c)
	{
		if (c == Void.TYPE)
		{
			return 0;
		}
		else if (c == Long.TYPE || c == Double.TYPE)
		{
			return 2;
		}
		return 1;
	}
	
	static int getLoadOpcode(Class<?> c)
	{
		if (c == Void.TYPE)
		{
			throw new InternalError("Unexpected void type of load opcode");
		}
		return ILOAD + getOpcodeOffset(c);
	}
	
	static int getReturnOpcode(Class<?> c)
	{
		if (c == Void.TYPE)
		{
			return RETURN;
		}
		return IRETURN + getOpcodeOffset(c);
	}
	
	private static int getOpcodeOffset(Class<?> c)
	{
		if (c.isPrimitive())
		{
			if (c == Long.TYPE)
			{
				return 1;
			}
			else if (c == Float.TYPE)
			{
				return 2;
			}
			else if (c == Double.TYPE)
			{
				return 3;
			}
			return 0;
		}
		return 4;
	}
}
