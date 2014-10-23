package dyvil.tools.compiler.ast.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ParserUtil;

public class ValueList extends ASTObject implements IValue, IValueList
{
	protected List<IValue>	values	= new ArrayList(3);
	
	protected boolean		isArray;
	
	public ValueList(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.values = list;
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.values;
	}
	
	@Override
	public void addValue(IValue value)
	{
		this.values.add(value);
	}
	
	@Override
	public void setIsArray(boolean isArray)
	{
		this.isArray = isArray;
	}
	
	@Override
	public boolean isArray()
	{
		return this.isArray;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public Type getType()
	{
		if (this.values == null || this.values.isEmpty())
		{
			return Type.VOID;
		}
		return this.values.get(this.values.size() - 1).getType();
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		this.values.replaceAll(v -> v.applyState(state, context));
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int size = this.values.size();
		if (this.isArray)
		{	
			if (size == 0)
			{
				buffer.append(Formatting.Expression.emptyArray);
			}
			else
			{
				buffer.append(Formatting.Expression.arrayStart);
				ParserUtil.astToString(this.values, Formatting.Expression.arraySeperator, buffer);
				buffer.append(Formatting.Expression.arrayEnd);
			}
		}
		else
		{
			if (size == 0)
			{
				buffer.append(Formatting.Expression.emptyExpression);
			}
			else
			{
				buffer.append('\n').append(prefix).append('{').append('\n');
				for (IValue value : this.values)
				{
					buffer.append(prefix).append(Formatting.Method.indent);
					value.toString("", buffer);
					buffer.append(";\n");
				}
				buffer.append(prefix).append('}');
			}
		}
	}
}
