package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.constant.BooleanValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.BoxedValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.Value;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class OrOperator extends Value
{
	public IValue	left;
	public IValue	right;
	
	public OrOperator(IValue left, IValue right)
	{
		this.left = left;
		this.right = right;
	}
	
	public OrOperator(ICodePosition position, IValue left, IValue right)
	{
		this.position = position;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public int valueTag()
	{
		return BOOLEAN_AND;
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
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.BOOLEAN)
		{
			return this;
		}
		return type.isSuperTypeOf(Types.BOOLEAN) ? new BoxedValue(this, Types.BOOLEAN.getBoxMethod()) : null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.left.resolveTypes(markers, context);
		this.right.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.left = this.left.resolve(markers, context);
		this.right = this.right.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.left.checkTypes(markers, context);
		this.right.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.left.check(markers, context);
		this.right.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		// Left value is true
		if (this.left.valueTag() == BOOLEAN && this.left.booleanValue())
		{
			return BooleanValue.TRUE;
		}
		if (this.bothFalse())
		{
			return BooleanValue.FALSE;
		}
		
		this.left = this.left.foldConstants();
		this.right = this.right.foldConstants();
		
		return this;
	}
	
	private boolean bothFalse()
	{
		return this.left.valueTag() == BOOLEAN && !this.left.booleanValue() && this.right.valueTag() == BOOLEAN && !this.right.booleanValue();
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.left = this.left.cleanup(context, compilableList);
		this.right = this.right.cleanup(context, compilableList);
		
		if (this.bothFalse())
		{
			return BooleanValue.FALSE;
		}
		
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		Label label = new Label();
		Label label2 = new Label();
		this.left.writeJump(writer, label);
		this.right.writeJump(writer, label);
		writer.writeLDC(0);
		writer.writeJumpInsn(Opcodes.GOTO, label2);
		writer.writeLabel(label);
		writer.writeLDC(1);
		writer.writeLabel(label2);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		Label label = new Label();
		this.left.writeInvJump(writer, label);
		this.right.writeJump(writer, dest);
		writer.writeLabel(label);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		Label label = new Label();
		this.left.writeJump(writer, label);
		this.right.writeInvJump(writer, dest);
		writer.writeLabel(label);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" || ");
		this.right.toString(prefix, buffer);
	}
}
