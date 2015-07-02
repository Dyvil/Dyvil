package dyvil.tools.compiler.ast.generic.type;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class PositionedTypeVarType extends TypeVarType
{
	private ICodePosition	position;
	
	public PositionedTypeVarType(ICodePosition position, ITypeVariable typeVar)
	{
		super(typeVar);
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
	{
		switch (position)
		{
		case CLASS:
		case TYPE:
			markers.add(this.position, "type.class.typevar");
			break;
		case SUPER_TYPE:
			markers.add(this.position, "type.super.typevar");
			break;
		default:
			break;
		}
		return this;
	}
}
