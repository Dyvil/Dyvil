package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.backend.MethodWriter;

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
	
	int	BINDING		= 32;
	int	WILDCARD	= 33;
	int	BOXED		= 34;
	
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
	
	@Override
	public boolean isType(IType type);
	
	public default IField resolveField(Name name)
	{
		return null;
	}
	
	public default int intValue()
	{
		return 0;
	}
	
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel);
	
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel);
}
