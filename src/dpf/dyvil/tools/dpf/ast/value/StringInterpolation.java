package dyvil.tools.dpf.ast.value;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.dpf.visitor.StringInterpolationVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.LexerUtil;

public class StringInterpolation extends ValueCreator implements Value, StringInterpolationVisitor
{
	private List<String>	strings	= new ArrayList<String>();
	private List<Value>		values	= new ArrayList<Value>();
	
	@Override
	public void visitStringPart(String string)
	{
		this.strings.add(string);
	}
	
	@Override
	protected void setValue(Value value)
	{
		this.values.add(value);
	}
	
	@Override
	public ValueVisitor visitValue()
	{
		return this;
	}
	
	@Override
	public void visitEnd()
	{
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		StringInterpolationVisitor v = visitor.visitStringInterpolation();
		int len = this.values.size();
		String s = this.strings.get(0);
		v.visitStringPart(s);
		
		for (int i = 0; i < len; i++)
		{
			this.values.get(i).accept(v.visitValue());
			v.visitStringPart(s);
		}
		
		v.visitEnd();
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.values.size();
		String s = this.strings.get(0);
		buffer.append('"');
		LexerUtil.appendStringLiteralBody(s, buffer);
		
		for (int i = 0; i < len; i++)
		{
			buffer.append("\\(");
			this.values.get(i).toString(prefix, buffer);
			s = this.strings.get(i + 1);
			buffer.append(')');
			LexerUtil.appendStringLiteralBody(s, buffer);
		}
		buffer.append('"');
	}
}
