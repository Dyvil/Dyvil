package dyvil.tools.compiler.ast.operator;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.BooleanValue;
import dyvil.tools.compiler.ast.expression.BoxedValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class NotOperator extends ASTNode implements IValue
{
	public IValue	value;
	
	public NotOperator(IValue right)
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
		return Types.BOOLEAN;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Types.BOOLEAN)
		{
			return this;
		}
		return type.isSuperTypeOf(Types.BOOLEAN) ? new BoxedValue(this, Types.BOOLEAN.boxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.BOOLEAN || type.isSuperTypeOf(Types.BOOLEAN);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Types.BOOLEAN)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Types.BOOLEAN))
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
		writer.writeLabel(label);
		writer.writeLDC(1);
		writer.writeLabel(label2);
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
