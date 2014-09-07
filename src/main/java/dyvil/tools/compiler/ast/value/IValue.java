package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.type.Type;

public interface IValue
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
									};
	
	public boolean isConstant();
	
	public IValue fold();
	
	public Type getType();
}
