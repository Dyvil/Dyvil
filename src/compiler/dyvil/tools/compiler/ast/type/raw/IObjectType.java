package dyvil.tools.compiler.ast.type.raw;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IObjectType extends IType
{
	@Override
	default boolean isPrimitive()
	{
		return false;
	}

	@Override
	default int getTypecode()
	{
		return -1;
	}

	@Override
	default IType getObjectType()
	{
		return this;
	}

	@Override
	default String getTypePrefix()
	{
		return "Object";
	}

	@Override
	default IType getSimpleRefType()
	{
		return ReferenceType.LazyFields.getObjectSimpleRef(this);
	}

	@Override
	default IClass getRefClass()
	{
		return ReferenceType.LazyFields.OBJECT_REF_CLASS;
	}

	@Override
	default IClass getArrayClass()
	{
		return Types.getObjectArrayClass();
	}

	@Override
	default IMethod getBoxMethod()
	{
		return null;
	}

	@Override
	default IMethod getUnboxMethod()
	{
		return null;
	}

	@Override
	default boolean isUninferred()
	{
		return false;
	}

	@Override
	default boolean isSameType(IType type)
	{
		return this == type || this.getTheClass() == type.getTheClass();
	}

	@Override
	default boolean isSameClass(IType type)
	{
		return this == type || this.getTheClass() == type.getTheClass() && !type.isPrimitive();
	}

	@Override
	default int getLoadOpcode()
	{
		return Opcodes.ALOAD;
	}

	@Override
	default int getArrayLoadOpcode()
	{
		return Opcodes.AALOAD;
	}

	@Override
	default int getStoreOpcode()
	{
		return Opcodes.ASTORE;
	}

	@Override
	default int getArrayStoreOpcode()
	{
		return Opcodes.AASTORE;
	}

	@Override
	default int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}

	@Override
	default Object getFrameType()
	{
		return this.getInternalName();
	}

	@Override
	default int getLocalSlots()
	{
		return 1;
	}

	@Override
	default void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException
	{
		if (target == this)
		{
			return;
		}
		if (Types.isVoid(target))
		{
			writer.visitInsn(Opcodes.POP);
			return;
		}

		if (!Types.isSuperClass(target, this))
		{
			writer.visitLineNumber(lineNumber);
			writer.visitTypeInsn(Opcodes.CHECKCAST, target.getInternalName());
		}
		if (target.isPrimitive())
		{
			target.getUnboxMethod()
			      .writeInvoke(writer, null, EmptyArguments.INSTANCE, ITypeContext.DEFAULT, lineNumber);
		}
	}

	@Override
	default void writeClassExpression(MethodWriter writer, boolean wrapPrimitives) throws BytecodeException
	{
		writer.visitLdcInsn(Type.getObjectType(this.getInternalName()));
	}
}
