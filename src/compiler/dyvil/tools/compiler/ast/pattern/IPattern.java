package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IPattern extends IASTNode
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
	
	public static int	WILDCARD	= 64;
	
	public int getPatternType();
	
	public IType getType();
	
	public boolean isType(IType type);
	
	public default int intValue()
	{
		return 0;
	}
	
	public void writeJump(MethodWriter writer, Label elseLabel);
}
