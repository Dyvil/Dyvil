package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ParserUtil;

public class ConstructorCall extends Call implements ITyped
{
	protected Type	type;
	
	public ConstructorCall(ICodePosition position)
	{
		super(position);
	}
	
	@Override
	public boolean resolve(IContext context)
	{
		// TODO
		return false;
	}
	
	@Override
	public IAccess resolve2(IContext context)
	{
		return this;
	}
	
	@Override
	public Marker getResolveError()
	{
		return null;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString("", buffer);
		if (this.isSugarCall && !Formatting.Method.convertSugarCalls)
		{
			this.arguments.get(0).toString("", buffer);
		}
		else
		{
			ParserUtil.parametersToString(this.arguments, buffer, true);
		}
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
	}
	
	@Override
	public void setIsArray(boolean isArray)
	{}
	
	@Override
	public boolean isArray()
	{
		return false;
	}

	@Override
	public IValue getValue()
	{
		return null;
	}

	@Override
	public void setValue(IValue value)
	{}

	@Override
	public void setName(String name)
	{}

	@Override
	public String getName()
	{
		return null;
	}
	
	
}
