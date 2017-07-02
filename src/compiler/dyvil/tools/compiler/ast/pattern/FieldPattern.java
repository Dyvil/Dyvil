package dyvil.tools.compiler.ast.pattern;

import dyvil.lang.Formattable;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.pattern.constant.*;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.CaseClasses;
import dyvil.tools.parsing.marker.MarkerList;

public class FieldPattern implements IPattern
{
	protected IDataMember dataMember;

	// Metadata
	protected SourcePosition position;

	public FieldPattern(SourcePosition position, IDataMember dataMember)
	{
		this.position = position;
		this.dataMember = dataMember;
	}

	@Override
	public int getPatternType()
	{
		return FIELD;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public IType getType()
	{
		if (this.dataMember == null)
		{
			return Types.UNKNOWN;
		}
		return this.dataMember.getType();
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		return this.isType(type) ? this : null;
	}

	@Override
	public boolean isType(IType type)
	{
		return this.dataMember == null || Types.isSuperType(type, this.dataMember.getType());
	}

	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		if (!this.dataMember.hasModifier(Modifiers.CONST))
		{
			return this;
		}

		final IValue value = toConstant(this.dataMember.getValue(), context);
		if (value == null)
		{
			return this;
		}

		switch (value.valueTag())
		{
		case IValue.NULL:
			return new NullPattern(this.position);
		case IValue.INT:
			return new IntPattern(this.position, value.intValue());
		case IValue.LONG:
			return new LongPattern(this.position, value.longValue());
		case IValue.FLOAT:
			return new FloatPattern(this.position, value.floatValue());
		case IValue.DOUBLE:
			return new DoublePattern(this.position, value.doubleValue());
		case IValue.STRING:
			return new StringPattern(this.position, value.stringValue());
		}
		return this;
	}

	private static IValue toConstant(IValue value, IContext context)
	{
		int depth = context.getCompilationContext().config.getMaxConstantDepth();

		do
		{
			if (value == null || depth-- < 0)
			{
				return null;
			}

			value = value.foldConstants();
		}
		while (!value.isConstantOrField());

		return value;
	}

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
			throws BytecodeException
	{
		final IType fieldType = this.dataMember.getType();
		final int lineNumber = this.lineNumber();

		final IType commonType;
		if (matchedType.isPrimitive())
		{
			commonType = matchedType;
		}
		else if (fieldType.isPrimitive())
		{
			commonType = fieldType;

			if (matchedType != fieldType && Types.isSuperType(matchedType, fieldType))
			{
				varIndex = IPattern.ensureVar(writer, varIndex, matchedType);
				IPattern.loadVar(writer, varIndex, matchedType);

				writer.visitTypeInsn(Opcodes.INSTANCEOF, fieldType.getInternalName());
				writer.visitJumpInsn(Opcodes.IFEQ, elseLabel);
			}
		}
		else
		{
			commonType = Types.ANY;
		}

		IPattern.loadVar(writer, varIndex, matchedType);

		matchedType.writeCast(writer, commonType, lineNumber);
		this.dataMember.writeGet(writer, null, lineNumber);
		fieldType.writeCast(writer, commonType, lineNumber);

		CaseClasses.writeIFNE(writer, commonType, elseLabel);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		final IClass enclosingClass = this.dataMember.getEnclosingClass();
		if (enclosingClass != null)
		{
			buffer.append(enclosingClass.getName()).append('.');
		}
		buffer.append(this.dataMember.getName());
	}
}
