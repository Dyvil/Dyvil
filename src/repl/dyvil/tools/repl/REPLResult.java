package dyvil.tools.repl;

import dyvil.array.ObjectArray;
import dyvil.string.StringUtils;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class REPLResult implements IConstantValue
{
	private final Object value;
	
	public REPLResult(Object value)
	{
		this.value = value;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return ICodePosition.ORIGIN;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public int valueTag()
	{
		return -1;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return Types.ANY;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return false;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	public int stringSize()
	{
		return 20;
	}
	
	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		builder.append(this.value);
		return true;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.value == null)
		{
			buffer.append("null");
			return;
		}
		
		Class c = this.value.getClass();
		if (c.isArray())
		{
			ObjectArray.toString(this.value, buffer);
			return;
		}
		
		String snapshot = buffer.toString();
		
		try
		{
			String s = this.value.toString();
			int i = s.indexOf('@');
			if (i >= 0)
			{
				String className = c.getName();
				if (i == className.length() && s.regionMatches(0, className, 0, i))
				{
					StringUtils.prettyPrint(this.value, c, buffer, true);
					return;
				}
			}
			
			buffer.append(this.value);
		}
		catch (Throwable t)
		{
			buffer.replace(0, buffer.length(), snapshot);
			buffer.append("<error>");
		}
	}
}
