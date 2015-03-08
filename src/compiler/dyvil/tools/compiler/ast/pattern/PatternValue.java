package dyvil.tools.compiler.ast.pattern;

import java.util.List;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class PatternValue extends ASTNode implements IValue, ICase
{
	private IPattern	pattern;
	private IValue		condition;
	private IValue		value;
	
	public PatternValue(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return CASE_STATEMENT;
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return false;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public void setPattern(IPattern pattern)
	{
		this.pattern = pattern;
	}
	
	@Override
	public IPattern getPattern()
	{
		return this.pattern;
	}
	
	@Override
	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}
	
	@Override
	public IValue getCondition()
	{
		return this.condition;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public PatternValue resolve(List<Marker> markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
		}
		this.value = this.value.resolve(markers, context);
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition.check(markers, context);
		}
		this.value.check(markers, context);
	}
	
	@Override
	public PatternValue foldConstants()
	{
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		this.value = this.value.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void writeExpression(MethodWriter writer, Label elseLabel)
	{
		this.pattern.writeJump(writer, elseLabel);
		if (this.condition != null)
		{
			this.condition.writeInvJump(writer, elseLabel);
		}
		this.value.writeExpression(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer, Label elseLabel)
	{
		this.pattern.writeJump(writer, elseLabel);
		if (this.condition != null)
		{
			this.condition.writeInvJump(writer, elseLabel);
		}
		this.value.writeStatement(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("case ");
		if (this.pattern != null)
		{
			this.pattern.toString(prefix, buffer);
		}
		if (this.condition != null)
		{
			buffer.append(" if ");
			this.condition.toString(prefix, buffer);
		}
		buffer.append(": ");
		this.value.toString(prefix, buffer);
	}
}
