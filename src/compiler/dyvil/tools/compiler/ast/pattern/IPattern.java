package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.asm.Label;
import dyvil.tools.asm.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

public interface IPattern extends IASTNode, ITyped
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
	
	int BINDING   = 32;
	int WILDCARD  = 33;
	int UNBOX     = 34;
	int TYPECHECK = 35;

	int OR  = 48;
	int AND = 49;
	
	int getPatternType();
	
	default boolean isExhaustive()
	{
		return false;
	}
	
	@Override
	IType getType();
	
	@Override
	default void setType(IType type)
	{
	}
	
	IPattern withType(IType type, MarkerList markers);
	
	static IPattern primitiveWithType(IPattern pattern, IType type, PrimitiveType primitiveType)
	{
		if (type == primitiveType)
		{
			return pattern;
		}

		if (type.isSuperTypeOf(primitiveType))
		{
			return new TypeCheckPattern(pattern, type, primitiveType);
		}
		return null;
	}
	
	@Override
	boolean isType(IType type);
	
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
			writer.writeVarInsn(matchedType.getLoadOpcode(), varIndex);
		}
	}

	static int ensureVar(MethodWriter writer, int varIndex, IType matchedType) throws BytecodeException
	{
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.writeVarInsn(matchedType.getStoreOpcode(), varIndex);
		}
		return varIndex;
	}

	default void writeJump(MethodWriter writer, int varIndex, IType matchedType, Label targetLabel)
			throws BytecodeException
	{
		final Label rightLabel = new Label();
		this.writeInvJump(writer, varIndex, matchedType, rightLabel);
		writer.writeJumpInsn(Opcodes.GOTO, targetLabel);
		writer.writeLabel(rightLabel);
	}
	
	void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel) throws BytecodeException;
}
