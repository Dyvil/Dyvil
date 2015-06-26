package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

import org.objectweb.asm.Label;

public interface IPattern extends IASTNode, ITyped
{
	int	NULL		= 0;
	int	BOOLEAN		= 1;
	int	BYTE		= 2;
	int	SHORT		= 3;
	int	CHAR		= 4;
	int	INT			= 5;
	int	LONG		= 6;
	int	FLOAT		= 7;
	int	DOUBLE		= 8;
	int	STRING		= 9;
	
	int	ARRAY		= 16;
	int	TUPLE		= 17;
	int	LIST		= 18;
	
	int	CASE_CLASS	= 24;
	
	int	BINDING		= 32;
	int	WILDCARD	= 33;
	int	BOXED		= 34;
	int	TYPECHECK	= 35;
	
	public int getPatternType();
	
	public default boolean isExhaustive()
	{
		return false;
	}
	
	@Override
	public IType getType();
	
	@Override
	public default void setType(IType type)
	{
	};
	
	public IPattern withType(IType type);
	
	public static IPattern primitiveWithType(IPattern pattern, IType type, PrimitiveType primitiveType)
	{
		if (type == primitiveType)
		{
			return pattern;
		}
		if (type.classEquals(primitiveType))
		{
			return new BoxPattern(pattern, primitiveType.unboxMethod);
		}
		if (type.isSuperTypeOf(primitiveType))
		{
			return new TypeCheckPattern(new BoxPattern(pattern, primitiveType.unboxMethod), primitiveType.getReferenceType());
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type);
	
	public default IField resolveField(Name name)
	{
		return null;
	}
	
	public default IPattern resolve(MarkerList markers, IContext context)
	{
		return this;
	}
	
	public default void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	public default int intValue()
	{
		return 0;
	}
	
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException;
	
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException;
}
