package dyvilx.tools.compiler.ast.pattern.object;

import dyvil.lang.Formattable;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.constant.*;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.CaseClasses;
import dyvilx.tools.parsing.marker.MarkerList;

public class FieldPattern implements Pattern
{
	protected IDataMember dataMember;

	// Metadata
	protected SourcePosition position;
	protected IType          targetType;

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
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
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
	public boolean isType(IType type)
	{
		return this.dataMember == null || Types.isSuperType(type, this.dataMember.getType());
	}

	@Override
	public Pattern withType(IType type, MarkerList markers)
	{
		if (!this.isType(type))
		{
			return null;
		}

		this.targetType = type;
		return this;
	}

	@Override
	public Object getConstantValue()
	{
		if (this.dataMember.hasConstantValue())
		{
			final IValue value = this.dataMember.getValue();
			return value != null ? value.toObject() : null;
		}
		return null;
	}

	@Override
	public Pattern resolve(MarkerList markers, IContext context)
	{
		if (this.dataMember.hasModifier(Modifiers.ENUM_CONST))
		{
			return new EnumPattern(this.position, this.dataMember);
		}
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
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		final IType matchedType = this.targetType;
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
				varIndex = Pattern.ensureVar(writer, varIndex);
				Pattern.loadVar(writer, varIndex);

				writer.visitTypeInsn(Opcodes.INSTANCEOF, fieldType.getInternalName());
				writer.visitJumpInsn(Opcodes.IFEQ, target);
			}
		}
		else
		{
			commonType = Types.ANY;
		}

		Pattern.loadVar(writer, varIndex);

		matchedType.writeCast(writer, commonType, lineNumber);
		this.dataMember.writeGet(writer, null, lineNumber);
		fieldType.writeCast(writer, commonType, lineNumber);

		CaseClasses.writeIFNE(writer, commonType, target);
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
