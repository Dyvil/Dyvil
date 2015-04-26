package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class LDCInstruction implements IInstruction
{
	private IValue	argument;
	
	public LDCInstruction(Object object)
	{
		this.argument = IValue.fromObject(object);
	}
	
	public LDCInstruction(IValue value)
	{
		this.argument = value;
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void write(MethodWriter writer) throws BytecodeException
	{
		this.argument.writeExpression(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("LDC ").append(this.argument);
	}
}
