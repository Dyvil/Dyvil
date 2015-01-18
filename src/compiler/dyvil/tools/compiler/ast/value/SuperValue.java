package dyvil.tools.compiler.ast.value;

import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
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
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public int getValueType()
	{
		return SUPER;
	}
	
	@Override
	public SuperValue resolve(List<Marker> markers, IContext context)
	{
		if (this.type == null)
		{
			if (context.isStatic())
			{
				markers.add(new SemanticError(this.position, "'super' cannot be accessed in a static context"));
			}
			else
			{
				IType thisType = context.getThisType();
				this.type = thisType.getSuperType();
				if (this.type == null)
				{
					SemanticError error = new SemanticError(this.position, "'super' cannot be accessed: The enclosing type does not have a super type");
					error.addInfo("Enclosing Type: " + thisType);
					markers.add(error);
				}
			}
		}
		return this;
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
