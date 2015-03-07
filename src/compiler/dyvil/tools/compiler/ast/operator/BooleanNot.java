package dyvil.tools.compiler.ast.operator;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.BooleanValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.BoxedValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public class BooleanNot extends ASTNode implements IValue
{
	public IValue	right;
	
	public BooleanNot(IValue right)
	{
		this.right = right;
	}
	
	@Override
	public int getValueType()
	{
		return BOOLEAN_NOT;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		return Type.BOOLEAN;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Type.BOOLEAN)
		{
			return this;
		}
		return type.isSuperTypeOf(Type.BOOLEAN) ? new BoxedValue(this, Type.BOOLEAN.boxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.BOOLEAN || type.isSuperTypeOf(Type.BOOLEAN);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Type.BOOLEAN)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Type.BOOLEAN))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.right.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		this.right = this.right.resolve(markers, context);
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		this.right.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.right.getValueType() == BOOLEAN)
		{
			BooleanValue b = (BooleanValue) this.right;
			b.value = !b.value;
			return b;
		}
		this.right = this.right.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		Label label = new Label();
		Label label2 = new Label();
		this.right.writeInvJump(writer, label);
		writer.writeLDC(0);
		writer.writeFrameJump(Opcodes.GOTO, label2);
		writer.pop();
		writer.writeFrameLabel(label);
		writer.writeLDC(1);
		writer.writeFrameLabel(label2);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest)
	{
		this.right.writeInvJump(writer, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest)
	{
		this.right.writeJump(writer, dest);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('!');
		this.right.toString(prefix, buffer);
	}
}
