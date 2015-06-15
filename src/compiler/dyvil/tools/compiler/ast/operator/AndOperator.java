package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.BooleanValue;
import dyvil.tools.compiler.ast.expression.BoxedValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import org.objectweb.asm.Label;

public final class AndOperator extends ASTNode implements IValue
{
	public IValue	left;
	public IValue	right;
	
	public AndOperator(IValue left, IValue right)
	{
		this.left = left;
		this.right = right;
	}
	
	public AndOperator(ICodePosition position, IValue left, IValue right)
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
		int t1 = this.left.valueTag();
		int t2 = this.right.valueTag();
		if (t1 == BOOLEAN && !((BooleanValue) this.left).value)
		{
			return BooleanValue.FALSE;
		}
		if (t2 == BOOLEAN && !((BooleanValue) this.left).value)
		{
			return BooleanValue.FALSE;
		}
		
		this.left.foldConstants();
		this.right.foldConstants();
		
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		Label label = new Label();
		Label label2 = new Label();
		this.left.writeInvJump(writer, label);
		this.right.writeInvJump(writer, label);
		writer.writeLDC(1);
		writer.writeJumpInsn(Opcodes.GOTO, label2);
		writer.writeLabel(label);
		writer.writeLDC(0);
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
		this.left.writeJump(writer, dest);
		this.right.writeJump(writer, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.left.writeInvJump(writer, dest);
		this.right.writeInvJump(writer, dest);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" && ");
		this.right.toString(prefix, buffer);
	}
}
