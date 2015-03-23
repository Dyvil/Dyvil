package dyvil.tools.compiler.ast.operator;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.BooleanValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.BoxValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class BooleanNot extends ASTNode implements IValue
{
	public IValue	value;
	
	public BooleanNot(IValue right)
	{
		this.value = right;
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
		return type.isSuperTypeOf(Type.BOOLEAN) ? new BoxValue(this, Type.BOOLEAN.boxMethod) : null;
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
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.value.getValueType() == BOOLEAN)
		{
			BooleanValue b = (BooleanValue) this.value;
			b.value = !b.value;
			return b;
		}
		this.value = this.value.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		Label label = new Label();
		Label label2 = new Label();
		this.value.writeInvJump(writer, label);
		writer.writeLDC(0);
		writer.writeJumpInsn(Opcodes.GOTO, label2);
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
		this.value.writeInvJump(writer, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest)
	{
		this.value.writeJump(writer, dest);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('!');
		this.value.toString(prefix, buffer);
	}
}
