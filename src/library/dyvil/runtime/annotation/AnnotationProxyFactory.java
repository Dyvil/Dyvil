package dyvil.runtime.annotation;

import dyvil.reflect.MethodReflection;
import dyvil.reflect.Opcodes;
import dyvil.runtime.BytecodeDump;
import dyvil.tools.asm.*;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

import static dyvil.reflect.Modifiers.*;
import static dyvil.reflect.Opcodes.*;
import static dyvil.reflect.ReflectUtils.UNSAFE;
import static dyvil.runtime.TypeConverter.*;
import static dyvil.string.StringUtils.EMPTY_STRING_ARRAY;

public final class AnnotationProxyFactory
{
	private static final int    CLASSFILE_VERSION = 52;
	private static final String NAME_FACTORY      = "get$Proxy";

	/**
	 * Used to ensure that each spun class name is unique
	 */
	private static final AtomicInteger counter = new AtomicInteger(0);

	/**
	 * Generated class constructor type
	 */
	private final MethodType constructorType;

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
		this.className = this.targetClass.getName().replace('.', '/') + "$Annotation$" + counter.incrementAndGet();

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
					// The annotation implementing inner class constructor is
					// private, set
					// it accessible (by us) before creating the constant
					// sole instance
					ctrs1[0].setAccessible(true);
				}
				return ctrs1;
			});
			if (ctrs.length != 1)
			{
				throw new Exception("Expected one annotation constructor for " + innerClass.getCanonicalName()
					                    + ", got " + ctrs.length);
			}

			try
			{
				Object inst = ctrs[0].newInstance();
				return new ConstantCallSite(MethodHandles.constant(this.annotationType, inst));
			}
			catch (ReflectiveOperationException e)
			{
				throw new Exception("Exception instantiating annotation proxy", e);
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

	private Class<?> spinInnerClass()
	{
		final String annotationItf = this.annotationType.getName().replace('.', '/');

		final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		classWriter.visit(CLASSFILE_VERSION, ASMConstants.ACC_SUPER | FINAL | SYNTHETIC, this.className, null,
		                  "java/lang/Object", new String[] { annotationItf });

		// Generate final fields to be filled in by constructor
		for (int i = 0; i < this.argDescs.length; i++)
		{
			FieldVisitor fv = classWriter.visitField(PRIVATE | FINAL, this.argNames[i], this.argDescs[i], null, null);
			fv.visitEnd();
		}

		if (this.parameterCount != 0)
		{
			this.generateFactory(classWriter);
		}

		this.generateConstructor(classWriter);
		this.generateAnnotationType(classWriter);
		this.generateMethods(classWriter);
		this.generateToString(classWriter);

		classWriter.visitEnd();

		final byte[] bytes = classWriter.toByteArray();

		BytecodeDump.dump(bytes, this.className);

		// Define the generated class in this VM.
		return UNSAFE.defineAnonymousClass(this.targetClass, bytes, null);
	}

	private void generateFactory(ClassWriter classWriter)
	{
		final MethodVisitor factory = classWriter.visitMethod(PRIVATE | STATIC, NAME_FACTORY,
		                                                      this.invokedType.toMethodDescriptorString(), null, null);
		factory.visitCode();
		factory.visitTypeInsn(NEW, this.className);
		factory.visitInsn(Opcodes.DUP);

		for (int typeIndex = 0, localIndex = 0; typeIndex < this.parameterCount; typeIndex++)
		{
			Class<?> argType = this.invokedType.parameterType(typeIndex);
			factory.visitVarInsn(getLoadOpcode(argType), localIndex);
			localIndex += getParameterSize(argType);
		}

		factory
			.visitMethodInsn(INVOKESPECIAL, this.className, "<init>", this.constructorType.toMethodDescriptorString(),
			                 false);
		factory.visitInsn(ARETURN);
		factory.visitMaxs(-1, -1);
		factory.visitEnd();
	}

	private void generateConstructor(ClassWriter classWriter)
	{
		// Generate constructor
		final MethodVisitor ctor = classWriter
			                           .visitMethod(PRIVATE, "<init>", this.constructorType.toMethodDescriptorString(),
			                                        null, null);
		ctor.visitCode();
		ctor.visitVarInsn(ALOAD, 0);
		ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

		for (int i = 0, localIndex = 0; i < this.parameterCount; i++)
		{
			ctor.visitVarInsn(ALOAD, 0);
			Class<?> argType = this.invokedType.parameterType(i);
			ctor.visitVarInsn(getLoadOpcode(argType), localIndex + 1);
			localIndex += getParameterSize(argType);
			ctor.visitFieldInsn(PUTFIELD, this.className, this.argNames[i], this.argDescs[i]);
		}

		ctor.visitInsn(RETURN);
		// Maxs computed by ClassWriter.COMPUTE_MAXS, these arguments ignored
		ctor.visitMaxs(-1, -1);
		ctor.visitEnd();
	}

	private void generateMethods(ClassWriter classWriter)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			final MethodVisitor methodVisitor = classWriter
				                                    .visitMethod(PUBLIC, this.argNames[i], "()" + this.argDescs[i],
				                                                 null, null);
			methodVisitor.visitCode();
			methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
			methodVisitor.visitFieldInsn(Opcodes.GETFIELD, this.className, this.argNames[i], this.argDescs[i]);
			methodVisitor.visitInsn(getReturnOpcode(this.invokedType.parameterType(i)));
			methodVisitor.visitMaxs(-1, -1);
			methodVisitor.visitEnd();
		}
	}

	private void generateAnnotationType(ClassWriter classWriter)
	{
		final MethodVisitor annotationType = classWriter
			                                     .visitMethod(PUBLIC, "annotationType", "()Ljava/lang/Class;", null,
			                                                  null);
		annotationType.visitCode();
		annotationType.visitLdcInsn(dyvil.tools.asm.Type.getType(this.annotationType));
		annotationType.visitInsn(Opcodes.ARETURN);
		annotationType.visitMaxs(-1, -1);
		annotationType.visitEnd();
	}

	private void generateToString(ClassWriter classWriter)
	{
		final MethodVisitor toString = classWriter.visitMethod(PUBLIC, "toString", "()Ljava/lang/String;", null, null);

		toString.visitCode();
		toString.visitLdcInsn('<' + this.annotationType.getName() + ">");
		toString.visitInsn(Opcodes.ARETURN);
		toString.visitMaxs(-1, -1);
		toString.visitEnd();
	}
}
