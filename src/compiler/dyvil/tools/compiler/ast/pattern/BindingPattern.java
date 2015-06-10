package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class BindingPattern extends ASTNode implements IPattern
{
	private Name		name;
	private Variable	variable;
	private IType		type = Types.UNKNOWN;
	
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
	public boolean isExhaustive()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		this.type = type;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return true;
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
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (this.variable != null)
		{
			this.writeVar(writer, varIndex);
		}
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (this.variable != null)
		{
			this.writeVar(writer, varIndex);
		}
	}
	
	private void writeVar(MethodWriter writer, int varIndex) throws BytecodeException
	{
		this.variable.type = this.type;
		if (varIndex >= 0)
		{
			writer.writeVarInsn(this.type.getLoadOpcode(), varIndex);
		}
		writer.writeVarInsn(this.type.getStoreOpcode(), this.variable.index = writer.localCount());
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append(' ');
		buffer.append(this.name);
	}
}
