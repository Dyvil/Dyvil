package dyvilx.tools.compiler.ast.pattern;

import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.Typed;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

public interface Pattern extends ASTNode, Typed
{
	// =============== Constants ===============

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

	// =============== Properties ===============

	// --------------- General Pattern Info ---------------

	int getPatternType();

	default boolean isExhaustive()
	{
		return false;
	}

	default boolean isWildcard()
	{
		return false;
	}

	// --------------- Constant Value ---------------

	default Object getConstantValue()
	{
		return null;
	}

	// --------------- Sub Patterns ---------------

	default int getSubPatternCount()
	{
		return 1;
	}

	default Pattern getSubPattern(int index)
	{
		return this;
	}

	// --------------- Switch Hashs ---------------

	default boolean hasSwitchHash()
	{
		return false;
	}

	default boolean isSwitchHashInjective()
	{
		return true;
	}

	default int getSwitchHashValue()
	{
		return -1;
	}

	default int getMinSwitchHashValue()
	{
		return this.getSwitchHashValue();
	}

	default int getMaxSwitchHashValue()
	{
		return this.getSwitchHashValue();
	}

	// --------------- Type ---------------

	@Override
	IType getType();

	@Override
	default void setType(IType type)
	{
	}

	default Pattern withType(IType type, MarkerList markers)
	{
		return this.isType(type) ? this : null;
	}

	@Override
	default boolean isType(IType type)
	{
		return Types.isSuperType(type, this.getType());
	}

	// --------------- Field Resolution ---------------

	default IDataMember resolveField(Name name)
	{
		return null;
	}

	// --------------- Resolution Phases ---------------

	default Pattern resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	// --------------- Compilation ---------------

	static void loadVar(MethodWriter writer, int varIndex) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.visitVarInsn(Opcodes.AUTO_LOAD, varIndex);
		}
	}

	static int ensureVar(MethodWriter writer, int varIndex) throws BytecodeException
	{
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.visitVarInsn(Opcodes.AUTO_STORE, varIndex);
		}
		return varIndex;
	}

	default void writeJumpOnMatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		final Label rightLabel = new Label();
		this.writeJumpOnMismatch(writer, varIndex, rightLabel);
		writer.visitJumpInsn(Opcodes.GOTO, target);
		writer.visitLabel(rightLabel);
	}

	void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException;
}
