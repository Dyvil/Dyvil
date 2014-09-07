package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.api.IASTObject;
import dyvil.tools.compiler.ast.type.Type;

public interface IValue extends IASTObject
{
	public static IValue	NULL	= new IValue()
									{
										@Override
										public boolean isConstant()
										{
											return true;
										}
										
										@Override
										public IValue fold()
										{
											return this;
										}
										
										@Override
										public Type getType()
										{
											return Type.VOID;
										}
										
										@Override
										public void toString(String prefix, StringBuilder buffer)
										{
											buffer.append(prefix).append("null");
										}
									};
	
	public boolean isConstant();
	
	public IValue fold();
	
	public Type getType();
}
