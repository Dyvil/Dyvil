package dyvil.tools.compiler.ast.type;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.constant.NullValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IObjectType extends IType
{
	@Override
	public default IType getObjectType()
	{
		return this;
	}
	
	@Override
	public default IType getSimpleRefType()
	{
		return Types.getSimpleRef(this);
	}
	
	@Override
	public default ReferenceType getRefType()
	{
		return Types.getRef(this);
	}
	
	@Override
	public default boolean isArrayType()
	{
		return false;
	}
	
	@Override
	public default int getArrayDimensions()
	{
		return 0;
	}
	
	@Override
	public default IType getElementType()
	{
		return this;
	}
	
	@Override
	public default IClass getArrayClass()
	{
		return Types.getObjectArray();
	}
	
	@Override
	public default IMethod getBoxMethod()
	{
		return null;
	}
	
	@Override
	public default IMethod getUnboxMethod()
	{
		return null;
	}
	
	@Override
	public default int getLoadOpcode()
	{
		return Opcodes.ALOAD;
	}
	
	@Override
	public default int getArrayLoadOpcode()
	{
		return Opcodes.AALOAD;
	}
	
	@Override
	public default int getStoreOpcode()
	{
		return Opcodes.ASTORE;
	}
	
	@Override
	public default int getArrayStoreOpcode()
	{
		return Opcodes.AASTORE;
	}
	
	@Override
	public default int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}
	
	@Override
	public default Object getFrameType()
	{
		return this.getInternalName();
	}
	
	@Override
	public default void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException
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
