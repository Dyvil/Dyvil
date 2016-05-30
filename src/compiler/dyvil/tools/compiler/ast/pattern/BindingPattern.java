package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class BindingPattern extends Pattern
{
	protected IType type = Types.UNKNOWN;
	protected Name name;

	// Metadata
	private Variable variable;

	public BindingPattern(ICodePosition position, Name name)
	{
		this.name = name;
		this.position = position;
	}

	public BindingPattern(ICodePosition position, IType type, Name name)
	{
		this.position = position;
		this.name = name;
		this.type = type;
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
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		if (this.type == Types.UNKNOWN)
		{
			this.type = type;
			return this;
		}
		if (Types.isExactType(type, this.type))
		{
			return this;
		}
		if (Types.isSuperType(type, this.type))
		{
			return new TypeCheckPattern(this, type, this.type);
		}

		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.type == Types.UNKNOWN || Types.isSuperType(type, this.type);
	}

	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		return this;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (name != this.name)
		{
			return null;
		}
		
		if (this.variable != null)
		{
			return this.variable;
		}
		
		this.variable = new Variable(this.position, this.name, this.type);
		return this.variable;
	}
	
	@Override
	public boolean isSwitchable()
	{
		return true;
	}
	
	@Override
	public boolean switchCheck()
	{
		return this.variable != null;
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
			throws BytecodeException
	{
		if (this.variable != null)
		{
			IPattern.loadVar(writer, varIndex, matchedType);
			this.variable.writeInit(writer, null);
		}
		else if (varIndex < 0)
		{
			writer.visitInsn(Opcodes.AUTO_POP);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.type == Types.UNKNOWN)
		{
			buffer.append("var ");
		}
		else
		{
			this.type.toString(prefix, buffer);
			buffer.append(' ');
		}
		buffer.append(this.name);
	}
}
