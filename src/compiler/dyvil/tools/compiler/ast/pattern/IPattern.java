package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.asm.Label;
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
	
	int BINDING   = 32;
	int WILDCARD  = 33;
	int UNBOX     = 34;
	int TYPECHECK = 35;
	
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
		if (type.classEquals(primitiveType))
		{
			return new UnboxPattern(pattern, primitiveType.getUnboxMethod());
		}
		if (type.isSuperTypeOf(primitiveType))
		{
			return new TypeCheckPattern(new UnboxPattern(pattern, primitiveType.getUnboxMethod()),
			                            primitiveType.getObjectType());
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
	
	default int switchCases()
	{
		return 0;
	}
	
	default boolean switchCheck()
	{
		return false;
	}
	
	default int switchValue(int index)
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
	
	void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException;
}
