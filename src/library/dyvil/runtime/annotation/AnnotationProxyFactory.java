package dyvil.runtime.annotation;

import dyvil.io.FileUtils;
import dyvil.reflect.MethodReflection;
import dyvil.reflect.Opcodes;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.asm.ClassWriter;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.asm.Type;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

import static dyvil.reflect.Modifiers.*;
import static dyvil.reflect.Opcodes.*;

public final class AnnotationProxyFactory
{
	private static final Unsafe UNSAFE = ReflectUtils.UNSAFE;
	
	private static final int    CLASSFILE_VERSION = 52;
	private static final String NAME_FACTORY      = "get$Proxy";
	
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	
	/**
	 * Directory to dump generated class file bytecode.
	 */
	private static final File dumpDirectory = null;
	
	/**
	 * Used to ensure that each spun class name is unique
	 */
	private static final AtomicInteger counter = new AtomicInteger(0);
	
	/**
	 * Generated class constructor type
	 */
	private final MethodType constructorType;
	
	/**
	 * ASM class writer
	 */
	private final ClassWriter cw;
	
	private final int parameterCount;
	
	/**
	 * Generated names for the constructor arguments
	 */
	private final String[] argNames;
	
	/**
	 * Type descriptors for the constructor arguments
	 */
	private final String[] argDescs;
	
	/**
	 * Generated name for the generated class
	 */
	private final String className;
	
	private final Class targetClass;
	
	private final MethodType invokedType;
	
	private final Class annotationType;
	
	public AnnotationProxyFactory(MethodHandles.Lookup caller, MethodType invokedType, Object... argumentNames)
			throws Exception
	{
		this.invokedType = invokedType;
		this.constructorType = invokedType.changeReturnType(Void.TYPE);
		this.annotationType = invokedType.returnType();
		
		this.targetClass = caller.lookupClass();
		this.className = this.targetClass.getName().replace('.', '/') + "$Lambda$" + counter.incrementAndGet();
		
		this.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		
		int parameterCount = this.parameterCount = argumentNames.length;
		if (parameterCount > 0)
		{
			this.argNames = new String[parameterCount];
			this.argDescs = new String[parameterCount];
			for (int i = 0; i < parameterCount; i++)
			{
				this.argNames[i] = argumentNames[i].toString();
				this.argDescs[i] = Type.getDescriptor(invokedType.parameterType(i));
			}
		}
		else
		{
			this.argDescs = EMPTY_STRING_ARRAY;
			this.argNames = EMPTY_STRING_ARRAY;
		}
	}
	
	public CallSite buildCallSite() throws Exception
	{
		final Class<?> innerClass = this.spinInnerClass();
		if (this.parameterCount == 0)
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
				throw new Exception("Expected one lambda constructor for " + innerClass.getCanonicalName() + ", got "
						                    + ctrs.length);
			}
			
			try
			{
				Object inst = ctrs[0].newInstance();
				return new ConstantCallSite(MethodHandles.constant(this.annotationType, inst));
			}
			catch (ReflectiveOperationException e)
			{
				throw new Exception("Exception instantiating lambda object", e);
			}
		}
		try
		{
			UNSAFE.ensureClassInitialized(innerClass);
			return new ConstantCallSite(MethodReflection.LOOKUP.findStatic(innerClass, NAME_FACTORY, this.invokedType));
		}
		catch (ReflectiveOperationException e)
		{
			throw new Exception("Exception finding constructor", e);
		}
	}
	
	private Class<?> spinInnerClass() throws LambdaConversionException
	{
		String samIntf = this.annotationType.getName().replace('.', '/');
		
		this.cw.visit(CLASSFILE_VERSION, dyvil.tools.asm.Opcodes.ACC_SUPER | FINAL | SYNTHETIC, this.className, null,
		              "java/lang/Object", new String[] { samIntf });

		// Generate final fields to be filled in by constructor
		for (int i = 0; i < this.argDescs.length; i++)
		{
			FieldVisitor fv = this.cw.visitField(PRIVATE | FINAL, this.argNames[i], this.argDescs[i], null, null);
			fv.visitEnd();
		}
		
		this.generateConstructor();
		
		if (this.parameterCount != 0)
		{
			this.generateFactory();
		}
		
		this.generateAnnotationType();
		
		this.generateMethods();
		
		// TODO Generate toString, equals and hashCode
		
		this.cw.visitEnd();
		
		byte[] bytes = this.cw.toByteArray();
		
		if (dumpDirectory != null)
		{
			File dumpFile = new File(dumpDirectory, this.className.replace('/', File.separatorChar).concat(".class"));
			FileUtils.tryWrite(dumpFile, bytes);
		}
		
		// Define the generated class in this VM.
		
		return UNSAFE.defineAnonymousClass(this.targetClass, bytes, null);
	}
	
	private void generateFactory()
	{
		MethodVisitor m = this.cw
				.visitMethod(PRIVATE | STATIC, NAME_FACTORY, this.invokedType.toMethodDescriptorString(), null, null);
		m.visitCode();
		m.visitTypeInsn(NEW, this.className);
		m.visitInsn(Opcodes.DUP);
		int parameterCount = this.parameterCount;
		for (int typeIndex = 0, varIndex = 0; typeIndex < parameterCount; typeIndex++)
		{
			Class<?> argType = this.invokedType.parameterType(typeIndex);
			m.visitVarInsn(getLoadOpcode(argType), varIndex);
			varIndex += getParameterSize(argType);
		}
		m.visitMethodInsn(INVOKESPECIAL, this.className, "<init>", this.constructorType.toMethodDescriptorString(),
		                  false);
		m.visitInsn(ARETURN);
		m.visitMaxs(-1, -1);
		m.visitEnd();
	}
	
	private void generateConstructor()
	{
		// Generate constructor
		MethodVisitor ctor = this.cw
				.visitMethod(PRIVATE, "<init>", this.constructorType.toMethodDescriptorString(), null, null);
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
			ctor.visitFieldInsn(PUTFIELD, this.className, this.argNames[i], this.argDescs[i]);
		}
		ctor.visitInsn(RETURN);
		// Maxs computed by ClassWriter.COMPUTE_MAXS, these arguments ignored
		ctor.visitMaxs(-1, -1);
		ctor.visitEnd();
	}
	
	private void generateMethods()
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			MethodVisitor mv = this.cw.visitMethod(PUBLIC, this.argNames[i], "()" + this.argDescs[i], null, null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, this.className, this.argNames[i], this.argDescs[i]);
			mv.visitInsn(getReturnOpcode(this.invokedType.parameterType(i)));
			mv.visitMaxs(-1, -1);
			mv.visitEnd();
		}
	}
	
	private void generateAnnotationType()
	{
		MethodVisitor atype = this.cw.visitMethod(PUBLIC, "annotationType", "()Ljava/lang/Class;", null, null);
		atype.visitCode();
		atype.visitLdcInsn(dyvil.tools.asm.Type.getType(this.annotationType));
		atype.visitInsn(Opcodes.ARETURN);
		atype.visitMaxs(-1, -1);
		atype.visitEnd();
	}
	
	private static int invocationOpcode(int kind) throws InternalError
	{
		switch (kind)
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
			throw new InternalError("Unexpected invocation kind: " + kind);
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
