package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class BindingPattern extends ASTNode implements IPattern, IPatterned
{
	private Name		name;
	private IPattern	pattern;
	private Variable	variable;
	private IType		type;
	
	public BindingPattern(ICodePosition position, Name name)
	{
		this.name = name;
		this.position = position;
	}
	
	@Override
	public int getPatternType()
	{
		return BINDING;
	}
	
	@Override
	public IType getType()
	{
		if (this.pattern == null)
		{
			return Type.ANY;
		}
		return this.pattern.getType();
	}
	
	@Override
	public IPattern withType(IType type)
	{
		if (this.pattern == null)
		{
			return this;
		}
		IPattern pattern1 = this.pattern.withType(type);
		if (pattern1 == null)
		{
			return null;
		}
		
		this.pattern = pattern1;
		this.type = type;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.pattern == null || this.pattern.isType(type);
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
	public IField resolveField(Name name)
	{
		if (name != this.name)
		{
			return null;
		}
		
		if (this.variable != null)
		{
			return this.variable;
		}
		
		this.variable = new Variable(this.position);
		this.variable.name = this.name;
		this.variable.type = this.type;
		return this.variable;
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
		if (this.variable != null)
		{
			this.writeVar(writer, varIndex);
		}
		if (this.pattern != null)
		{
			this.pattern.writeJump(writer, varIndex, elseLabel);
		}
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
		if (this.variable != null)
		{
			this.writeVar(writer, varIndex);
		}
		if (this.pattern != null)
		{
			this.pattern.writeInvJump(writer, varIndex, elseLabel);
		}
	}
	
	private void writeVar(MethodWriter writer, int varIndex)
	{
		this.variable.type = this.type;
		writer.writeVarInsn(this.type.getLoadOpcode(), varIndex);
		writer.writeVarInsn(this.type.getStoreOpcode(), this.variable.index = writer.localCount());
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(" = ");
		if (this.pattern == null)
		{
			buffer.append('_');
			return;
		}
		this.pattern.toString(prefix, buffer);
	}
}
