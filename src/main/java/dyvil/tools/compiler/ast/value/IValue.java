package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IASTObject;
import dyvil.tools.compiler.ast.type.Type;

public interface IValue extends IASTObject
{
	public static IValue	NULL	= new IValue()
									{
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
										public String toString()
										{
											return "null";
										}
										
										@Override
										public void toString(String prefix, StringBuilder buffer)
										{
											buffer.append("null");
										}
									};
	
	public IValue fold();
	
	public Type getType();
	
	@Override
	public default void applyState(CompilerState state)
	{}
}
