package dyvil.tools.compiler.ast.operator;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.BooleanValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class BooleanAnd extends ASTNode implements IValue
{
	public IValue	left;
	public IValue	right;
	
	public BooleanAnd(IValue left, IValue right)
	{
		this.left = left;
		this.right = right;
	}
	
	public BooleanAnd(ICodePosition position, IValue left, IValue right)
	{
		this.position = position;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public int getValueType()
	{
		return BOOLEAN_AND;
	}
	
	@Override
	public IType getType()
	{
		return Type.BOOLEAN;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type == Type.BOOLEAN ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.BOOLEAN;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return type == Type.BOOLEAN ? 3 : 0;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		// Argument Types were already resolved by the MethodCall, so this
		// should never happen
		this.left.resolveTypes(markers, context);
		this.right.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		// Arguments were already resolved by the MethodCall, so this should
		// never happen
		this.left.resolve(markers, context);
		this.right.resolve(markers, context);
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public IValue foldConstants()
	{
		int t1 = this.left.getValueType();
		int t2 = this.right.getValueType();
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
	public void writeExpression(MethodWriter writer)
	{
		Label label = new Label();
		Label label2 = new Label();
		this.left.writeInvJump(writer, label);
		this.right.writeInvJump(writer, label);
		writer.visitLdcInsn(1);
		writer.visitJumpInsn(Opcodes.GOTO, label2);
		writer.pop();
		writer.visitLabel(label);
		writer.visitLdcInsn(0);
		writer.visitLabel(label2);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.visitInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest)
	{
		this.left.writeJump(writer, dest);
		this.right.writeJump(writer, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest)
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
