package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ObjectPattern extends Pattern implements IPattern
{
	protected IType type;

	// Metadata
	private IDataMember instanceField;

	public ObjectPattern(ICodePosition position, IType type)
	{
		this.type = type;
		this.position = position;
	}

	@Override
	public int getPatternType()
	{
		return OBJECT;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		if (type.isSameType(this.type))
		{
			return this;
		}
		if (type.isSuperTypeOf(this.type))
		{
			return new TypeCheckPattern(this, this.type);
		}
		return null;
	}

	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(this.type);
	}

	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);

		final IClass theClass = this.type.getTheClass();
		if (theClass == null)
		{
			return this;
		}

		if (!theClass.hasModifier(Modifiers.OBJECT_CLASS))
		{
			markers.add(MarkerMessages.createError(this.position, "pattern.object", theClass.getName()));
			return this;
		}

		this.instanceField = theClass.getMetadata().getInstanceField();
		return this;
	}

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		}

		this.instanceField.writeGet(writer, null, this.getLineNumber());
		writer.writeJumpInsn(Opcodes.IF_ACMPNE, elseLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
	}
}
