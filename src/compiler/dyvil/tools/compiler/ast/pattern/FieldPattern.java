package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class FieldPattern implements IPattern
{
	protected IDataMember dataMember;

	// Metadata
	protected ICodePosition position;

	public FieldPattern(ICodePosition position, IDataMember dataMember)
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
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public ICodePosition getPosition()
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
		return this.dataMember == null || type.isSuperTypeOf(this.dataMember.getType());
	}

	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		// TODO Inline Constant Primitives
		return this;
	}

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
			throws BytecodeException
	{
		final IType fieldType = this.dataMember.getType();
		final int lineNumber = this.getLineNumber();

		IType commonType = Types.ANY;
		if (matchedType.isPrimitive())
		{
			commonType = matchedType;
		}
		else if (fieldType.isPrimitive())
		{
			commonType = fieldType;

			if (matchedType != fieldType && matchedType.isSuperTypeOf(fieldType))
			{
				varIndex = IPattern.ensureVar(writer, varIndex, matchedType);
				IPattern.loadVar(writer, varIndex, matchedType);

				writer.writeTypeInsn(Opcodes.INSTANCEOF, fieldType.getInternalName());
				writer.writeJumpInsn(Opcodes.IFEQ, elseLabel);
			}
		}

		IPattern.loadVar(writer, varIndex, matchedType);

		matchedType.writeCast(writer, commonType, lineNumber);
		this.dataMember.writeGet(writer, null, lineNumber);
		fieldType.writeCast(writer, commonType, lineNumber);

		if (commonType.isPrimitive())
		{
			switch (commonType.getTypecode())
			{
			case PrimitiveType.BOOLEAN_CODE:
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.CHAR_CODE:
			case PrimitiveType.INT_CODE:
				writer.writeJumpInsn(Opcodes.IF_ICMPNE, elseLabel);
				return;
			case PrimitiveType.LONG_CODE:
				writer.writeJumpInsn(Opcodes.IF_LCMPNE, elseLabel);
				return;
			case PrimitiveType.FLOAT_CODE:
				writer.writeJumpInsn(Opcodes.IF_FCMPNE, elseLabel);
				return;
			case PrimitiveType.DOUBLE_CODE:
				writer.writeJumpInsn(Opcodes.IF_DCMPNE, elseLabel);
				return;
			}

			return;
		}

		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/lang/Predef", "$eq$eq",
		                       "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
		writer.writeJumpInsn(Opcodes.IFEQ, elseLabel);
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		final IClass enclosingClass = this.dataMember.getTheClass();
		if (enclosingClass != null)
		{
			buffer.append(enclosingClass.getName()).append('.');
		}
		buffer.append(this.dataMember.getName());
	}
}
