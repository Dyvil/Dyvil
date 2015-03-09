package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IPattern extends IASTNode, ITyped
{
	public static int	NULL		= 0;
	public static int	BOOLEAN		= 1;
	public static int	BYTE		= 2;
	public static int	SHORT		= 3;
	public static int	CHAR		= 4;
	public static int	INT			= 5;
	public static int	LONG		= 6;
	public static int	FLOAT		= 7;
	public static int	DOUBLE		= 8;
	public static int	STRING		= 9;
	
	public static int	ARRAY		= 16;
	public static int	TUPLE		= 17;
	public static int	LIST		= 18;
	
	public static int	BINDING		= 32;
	
	public static int	WILDCARD	= 64;
	
	public int getPatternType();
	
	@Override
	public default void setType(IType type)
	{
	}
	
	public default IField resolveField(String name)
	{
		return null;
	}
	
	public default int intValue()
	{
		return 0;
	}
	
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel);
}
