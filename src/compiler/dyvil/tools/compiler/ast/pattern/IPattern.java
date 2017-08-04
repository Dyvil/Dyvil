package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.lang.Name;
import dyvil.tools.parsing.ASTNode;
import dyvil.tools.parsing.marker.MarkerList;

public interface IPattern extends ASTNode, ITyped
{
	int NULL    = 0;
	int BOOLEAN = 1;
	int BYTE    = 2;
	int SHORT   = 3;
	int CHAR    = 4;
	int INT     = 5;
	int LONG    = 6;
	int FLOAT   = 7;
	int DOUBLE  = 8;
	int STRING  = 9;

	int ARRAY = 16;
	int TUPLE = 17;
	int LIST  = 18;

	int CASE_CLASS = 24;
	int OBJECT     = 25;
	int FIELD      = 26;

	int BINDING   = 32;
	int WILDCARD  = 33;
	int TYPECHECK = 35;

	int OR  = 48;
	int AND = 49;

	int getPatternType();

	default boolean isExhaustive()
	{
		return false;
	}

	default boolean isWildcard()
	{
		return false;
	}

	@Override
	IType getType();

	@Override
	default void setType(IType type)
	{
	}

	default IPattern withType(IType type, MarkerList markers)
	{
		return this.isType(type) ? this : null;
	}

	static IPattern primitiveWithType(IPattern pattern, IType type, PrimitiveType primitiveType)
	{
		if (type == primitiveType)
		{
			return pattern;
		}

		if (Types.isSuperType(type, primitiveType))
		{
			return new TypeCheckPattern(pattern, type, primitiveType);
		}
		return null;
	}

	@Override
	default boolean isType(IType type)
	{
		return Types.isSuperType(type, this.getType());
	}

	default IDataMember resolveField(Name name)
	{
		return null;
	}

	default IPattern resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	default boolean isSwitchable()
	{
		return false;
	}

	default int subPatterns()
	{
		return 1;
	}

	default boolean switchCheck()
	{
		return false;
	}

	default int switchValue()
	{
		return -1;
	}

	default int minValue()
	{
		return -1;
	}

	default int maxValue()
	{
		return -1;
	}

	default IPattern subPattern(int index)
	{
		return this;
	}

	static void loadVar(MethodWriter writer, int varIndex, IType matchedType) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.visitVarInsn(matchedType.getLoadOpcode(), varIndex);
		}
	}

	static int ensureVar(MethodWriter writer, int varIndex, IType matchedType) throws BytecodeException
	{
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.visitVarInsn(matchedType.getStoreOpcode(), varIndex);
		}
		return varIndex;
	}

	default void writeJump(MethodWriter writer, int varIndex, IType matchedType, Label targetLabel)
		throws BytecodeException
	{
		final Label rightLabel = new Label();
		this.writeInvJump(writer, varIndex, matchedType, rightLabel);
		writer.visitJumpInsn(Opcodes.GOTO, targetLabel);
		writer.visitLabel(rightLabel);
	}

	void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel) throws BytecodeException;
}
