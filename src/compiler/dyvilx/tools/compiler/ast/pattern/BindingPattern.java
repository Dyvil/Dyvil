package dyvilx.tools.compiler.ast.pattern;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public final class BindingPattern implements IPattern, IDataMemberConsumer<Variable>
{
	private Variable variable;

	// Metadata
	private boolean variableRequested;

	public BindingPattern()
	{
	}

	public BindingPattern(SourcePosition position, Name name, IType type)
	{
		this.variable = new Variable(position, name, type);
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
		return this.variable.getType();
	}

	@Override
	public void setType(IType type)
	{
		this.variable.setType(type);
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.variable.getPosition();
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.variable.setPosition(position);
	}

	@Override
	public void addDataMember(Variable dataMember)
	{
		this.variable = dataMember;
	}

	@Override
	public Variable createDataMember(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new Variable(position, name, type, attributes);
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		final IType thisType = this.getType();
		if (!thisType.isResolved())
		{
			this.setType(type);
			return this;
		}
		if (Types.isExactType(type, thisType))
		{
			return this;
		}
		return Types.isSuperType(type, thisType) ? new TypeCheckPattern(this, type, thisType) : null;
	}

	@Override
	public boolean isType(IType type)
	{
		final IType thisType = this.getType();
		return !thisType.isResolved() || Types.isSuperType(type, thisType);
	}

	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		this.setType(this.getType().resolveType(markers, context));
		return this;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (name != this.variable.getName())
		{
			return null;
		}

		this.variableRequested = true;
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
		return this.variableRequested;
	}

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target)
		throws BytecodeException
	{
		if (this.variableRequested)
		{
			IPattern.loadVar(writer, varIndex);
			this.variable.writeInit(writer, null);
		}
		else if (varIndex < 0)
		{
			writer.visitInsn(Opcodes.AUTO_POP);
		}
	}

	@Override
	public void toString(@NonNull String prefix, @NonNull StringBuilder buffer)
	{
		this.variable.toString(prefix, buffer);
	}
}
