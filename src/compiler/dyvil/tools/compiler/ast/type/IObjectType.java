package dyvil.tools.compiler.ast.type;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.constant.NullValue;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
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
	default ITypeVariable getTypeVariable()
	{
		return null;
	}
	
	@Override
	default IType getObjectType()
	{
		return this;
	}
	
	@Override
	default IType getSimpleRefType()
	{
		return Types.getObjectSimpleRef(this);
	}

	@Override
	default IClass getRefClass()
	{
		return Types.getObjectRefClass();
	}

	@Override
	default boolean isArrayType()
	{
		return false;
	}
	
	@Override
	default int getArrayDimensions()
	{
		return 0;
	}
	
	@Override
	default IType getElementType()
	{
		return this;
	}
	
	@Override
	default IClass getArrayClass()
	{
		return Types.getObjectArray();
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
	default boolean classEquals(IType type)
	{
		return this.getTheClass() == type.getTheClass() && !type.isPrimitive();
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
	default String getExtendedName()
	{
		final StringBuilder stringBuilder = new StringBuilder();
		this.appendExtendedName(stringBuilder);
		return stringBuilder.toString();
	}

	@Override
	default void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.getInternalName()).append(';');
	}

	@Override
	default void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException
	{
		if (target == this)
		{
			return;
		}
		
		if (!target.isSuperClassOf(this))
		{
			writer.writeLineNumber(lineNumber);
			writer.writeTypeInsn(Opcodes.CHECKCAST, target.getInternalName());
		}
		if (target.isPrimitive())
		{
			target.getUnboxMethod().writeInvoke(writer, null, EmptyArguments.INSTANCE, lineNumber);
		}
	}
	
	@Override
	default void writeDefaultValue(MethodWriter writer) throws BytecodeException
	{
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	default IConstantValue getDefaultValue()
	{
		return NullValue.getNull();
	}
}
