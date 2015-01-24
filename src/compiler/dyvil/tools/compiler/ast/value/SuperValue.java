package dyvil.tools.compiler.ast.value;

import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SuperValue extends ASTNode implements IConstantValue
{
	public IType	type;
	
	public SuperValue(ICodePosition position)
	{
		this.position = position;
	}
	
	public SuperValue(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}
	
	@Override
	public int getValueType()
	{
		return SUPER;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.type == null)
		{
			if (context.isStatic())
			{
				markers.add(Markers.create(this.position, "access.super.static"));
			}
			else
			{
				IType thisType = context.getThisType();
				this.type = thisType.getSuperType();
				if (this.type == null)
				{
					Marker marker = Markers.create(this.position, "access.super.type");
					marker.addInfo("Enclosing Type: " + thisType);
					markers.add(marker);
				}
			}
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("super");
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitIntInsn(Opcodes.ALOAD, 0);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
}
