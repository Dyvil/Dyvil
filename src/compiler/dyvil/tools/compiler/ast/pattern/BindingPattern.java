package dyvil.tools.compiler.ast.pattern;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.lang.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

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
	public Variable createDataMember(SourcePosition position, Name name, IType type, ModifierSet modifiers,
		                                AnnotationList annotations)
	{
		return new Variable(position, name, type, modifiers, annotations);
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
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
		throws BytecodeException
	{
		if (this.variableRequested)
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
	public void toString(@NonNull String prefix, @NonNull StringBuilder buffer)
	{
		this.variable.toString(prefix, buffer);
	}
}
