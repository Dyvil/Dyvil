package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

public interface IStatement extends IValue
{
	@Override
	default boolean isResolved()
	{
		return true;
	}
	
	@Override
	default IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	default IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.VOID ? this : null;
	}
	
	@Override
	default boolean isType(IType type)
	{
		return type == Types.VOID;
	}
	
	@Override
	default float getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	default void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.writeStatement(writer);
		if (type != Types.VOID)
		{
			type.writeDefaultValue(writer);
		}
	}

	void writeStatement(MethodWriter writer) throws BytecodeException;
}
