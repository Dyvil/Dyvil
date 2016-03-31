package dyvil.runtime.reference;

import dyvil.reflect.Opcodes;
import dyvil.runtime.BytecodeDump;
import dyvil.runtime.TypeConverter;
import dyvil.tools.asm.ASMConstants;
import dyvil.tools.asm.ClassWriter;
import dyvil.tools.asm.MethodVisitor;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

import static dyvil.reflect.MethodReflection.LOOKUP;
import static dyvil.reflect.Modifiers.*;
import static dyvil.reflect.Opcodes.*;
import static dyvil.reflect.ReflectUtils.UNSAFE;
import static dyvil.runtime.TypeConverter.invocationOpcode;

public class PropertyReferenceMetafactory
{
	private static final int    CLASS_VERSION = 52;
	private static final String FACTORY_NAME  = "create$Ref";
	private static final String RECEIVER_NAME = "$receiver";

	/**
	 * Used to ensure that each spun class name is unique
	 */
	private static final AtomicInteger counter = new AtomicInteger(0);

	private final Class<?> targetClass;

	private final MethodHandleInfo getterInfo;
	private final MethodHandleInfo setterInfo;

	private final String     className;
	private final Class<?>   refClass;
	private final Class<?>   refTargetClass;
	private final String     refTargetType;
	private final String     receiverType;
	private final MethodType factoryType;
	private final MethodType constructorType;

	public PropertyReferenceMetafactory(MethodHandles.Lookup caller, MethodType invokedType, MethodHandle getter, MethodHandle setter)
	{
		this.targetClass = caller.lookupClass();
		this.factoryType = invokedType;
		this.constructorType = invokedType.changeReturnType(void.class);
		this.refClass = invokedType.returnType();

		this.getterInfo = caller.revealDirect(getter);
		this.setterInfo = caller.revealDirect(setter);

		if (invokedType.parameterCount() == 0)
		{
			this.receiverType = null;
		}
		else
		{
			this.receiverType = 'L' + TypeConverter.getInternalName(invokedType.parameterType(0)) + ';';
		}

		switch (this.refClass.getSimpleName())
		{
		case "BooleanRef":
			this.refTargetType = "Z";
			this.refTargetClass = boolean.class;
			break;
		case "ByteRef":
			this.refTargetType = "B";
			this.refTargetClass = byte.class;
			break;
		case "ShortRef":
			this.refTargetType = "S";
			this.refTargetClass = short.class;
			break;
		case "CharRef":
			this.refTargetType = "C";
			this.refTargetClass = char.class;
			break;
		case "IntRef":
			this.refTargetType = "I";
			this.refTargetClass = int.class;
			break;
		case "LongRef":
			this.refTargetType = "J";
			this.refTargetClass = long.class;
			break;
		case "FloatRef":
			this.refTargetType = "F";
			this.refTargetClass = float.class;
			break;
		case "DoubleRef":
			this.refTargetType = "D";
			this.refTargetClass = double.class;
			break;
		default:
			this.refTargetType = "Ljava/lang/Object;";
			this.refTargetClass = Object.class;
		}

		this.className = TypeConverter.getInternalName(this.targetClass) + "$PropertyRef$" + counter.incrementAndGet();
	}

	public CallSite buildCallSite() throws Exception
	{
		final Class<?> innerClass = this.spinInnerClass();

		if (this.receiverType == null)
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
				throw new Exception("Expected one constructor for " + innerClass.getCanonicalName() + ", got "
					                    + ctrs.length);
			}

			try
			{
				Object inst = ctrs[0].newInstance();
				return new ConstantCallSite(MethodHandles.constant(this.refClass, inst));
			}
			catch (ReflectiveOperationException e)
			{
				throw new Exception("Exception instantiating property reference", e);
			}
		}
		try
		{
			UNSAFE.ensureClassInitialized(innerClass);
			return new ConstantCallSite(LOOKUP.findStatic(innerClass, FACTORY_NAME, this.factoryType));
		}
		catch (ReflectiveOperationException e)
		{
			throw new Exception("Exception finding constructor", e);
		}
	}

	private Class<?> spinInnerClass() throws Exception
	{
		String refItf = TypeConverter.getInternalName(this.refClass);

		final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		classWriter
			.visit(CLASS_VERSION, PUBLIC | SYNTHETIC | ASMConstants.ACC_FINAL, this.className, null, "java/lang/Object",
			       new String[] { refItf });

		if (this.receiverType != null)
		{
			classWriter.visitField(PRIVATE, RECEIVER_NAME, this.receiverType, null, null).visitEnd();
		}

		this.generateConstructor(classWriter);
		this.generateFactory(classWriter);
		this.generateGetter(classWriter);
		this.generateSetter(classWriter);
		this.generateToString(classWriter);

		classWriter.visitEnd();

		final byte[] bytes = classWriter.toByteArray();
		BytecodeDump.dump(bytes, this.className);
		return UNSAFE.defineAnonymousClass(this.targetClass, bytes, null);
	}

	private void generateFactory(ClassWriter classWriter)
	{
		final MethodVisitor factory = classWriter.visitMethod(PRIVATE | STATIC, FACTORY_NAME,
		                                                      this.factoryType.toMethodDescriptorString(), null, null);
		factory.visitCode();

		factory.visitTypeInsn(Opcodes.NEW, this.className);
		factory.visitInsn(Opcodes.DUP);

		if (this.receiverType != null)
		{
			factory.visitVarInsn(ALOAD, 0);
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
		final MethodVisitor ctor = classWriter
			                           .visitMethod(PRIVATE, "<init>", this.constructorType.toMethodDescriptorString(),
			                                        null, null);
		ctor.visitCode();
		ctor.visitVarInsn(ALOAD, 0);
		ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

		if (this.receiverType != null)
		{
			ctor.visitVarInsn(ALOAD, 0);
			ctor.visitVarInsn(ALOAD, 1);
			ctor.visitFieldInsn(PUTFIELD, this.className, RECEIVER_NAME, this.receiverType);
		}

		ctor.visitInsn(RETURN);
		ctor.visitMaxs(-1, -1);
		ctor.visitEnd();
	}

	private void writeReceiver(MethodVisitor setter)
	{
		if (this.receiverType != null)
		{
			setter.visitVarInsn(Opcodes.ALOAD, 0);
			setter.visitFieldInsn(Opcodes.GETFIELD, this.className, RECEIVER_NAME, this.receiverType);
		}
	}

	private void writeMethod(MethodVisitor setter, MethodHandleInfo handleInfo)
	{
		setter.visitMethodInsn(invocationOpcode(handleInfo.getReferenceKind()),
		                       TypeConverter.getInternalName(handleInfo.getDeclaringClass()), handleInfo.getName(),
		                       handleInfo.getMethodType().toMethodDescriptorString(),
		                       handleInfo.getDeclaringClass().isInterface());
	}

	private void generateGetter(ClassWriter classWriter)
	{
		MethodVisitor getter = classWriter.visitMethod(PUBLIC, "get", "()" + this.refTargetType, null, null);

		getter.visitCode();

		this.writeReceiver(getter);
		this.writeMethod(getter, this.getterInfo);

		TypeConverter.convertType(getter, this.getterInfo.getMethodType().returnType(), this.refTargetClass,
		                          this.refTargetClass);

		getter.visitInsn(TypeConverter.getReturnOpcode(this.refTargetClass));
		getter.visitMaxs(-1, -1);
		getter.visitEnd();
	}

	private void generateSetter(ClassWriter classWriter)
	{
		final MethodType setterType = this.setterInfo.getMethodType();
		final Class<?> setterParamType = setterType.parameterType(0);
		final Class<?> setterReturnType = setterType.returnType();

		MethodVisitor setter = classWriter.visitMethod(PUBLIC, "set", "(" + this.refTargetType + ")V", null, null);

		setter.visitCode();

		this.writeReceiver(setter);

		setter.visitVarInsn(TypeConverter.getLoadOpcode(this.refTargetClass), 1);
		TypeConverter.convertType(setter, this.refTargetClass, setterParamType, setterParamType);

		this.writeMethod(setter, this.setterInfo);

		if (setterReturnType != void.class)
		{
			if (setterReturnType == long.class || setterReturnType == double.class)
			{
				setter.visitInsn(Opcodes.POP2);
			}
			else
			{
				setter.visitInsn(Opcodes.POP);
			}
		}

		setter.visitInsn(Opcodes.RETURN);
		setter.visitMaxs(-1, -1);
		setter.visitEnd();
	}

	private void generateToString(ClassWriter classWriter)
	{
		MethodVisitor toString = classWriter.visitMethod(PUBLIC, "toString", "()Ljava/lang/String;", null, null);
		toString.visitCode();
		toString.visitLdcInsn('<' + this.refClass.getName() + ">");
		toString.visitInsn(Opcodes.ARETURN);
		toString.visitMaxs(-1, -1);
		toString.visitEnd();
	}
}
