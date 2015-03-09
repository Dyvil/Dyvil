package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class BindingPattern extends ASTNode implements IPattern, IPatterned
{
	private String		name;
	private IPattern	pattern;
	private Variable	variable;
	private IType		type;
	
	public BindingPattern(ICodePosition position, String name)
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
	public boolean isType(IType type)
	{
		if (this.pattern == null)
		{
			return true;
		}
		if (this.pattern.isType(type))
		{
			this.type = type;
			return true;
		}
		return false;
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
	public IField resolveField(String name)
	{
		if (!name.equals(this.name))
		{
			return null;
		}
		
		if (this.variable != null)
		{
			return this.variable;
		}
		
		this.variable = new Variable(this.position);
		this.variable.name = this.name;
		this.variable.qualifiedName = this.name;
		this.variable.type = this.type;
		return this.variable;
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
		if (this.variable != null)
		{
			this.variable.type = this.type;
			Object frameType = this.type.getFrameType();
			if (writer.getLocal(varIndex) == frameType)
			{
				this.variable.index = varIndex;
			}
			else
			{
				this.variable.index = writer.registerLocal(frameType);
				writer.writeVarInsn(this.type.getLoadOpcode(), varIndex);
				writer.writeVarInsn(this.type.getStoreOpcode(), this.variable.index);
			}
		}
		
		if (this.pattern != null)
		{
			this.pattern.writeJump(writer, varIndex, elseLabel);
		}
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
