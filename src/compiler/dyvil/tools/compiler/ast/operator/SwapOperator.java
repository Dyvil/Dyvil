package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.Value;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SwapOperator extends Value
{
	public FieldAccess	left;
	public FieldAccess	right;
	
	public SwapOperator(FieldAccess left, FieldAccess right)
	{
		this.left = left;
		this.right = right;
	}
	
	public SwapOperator(ICodePosition position, FieldAccess left, FieldAccess right)
	{
		this.position = position;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public int valueTag()
	{
		return SWAP_OPERATOR;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.VOID ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.VOID;
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
		this.left.resolve(markers, context);
		this.right.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.left.checkTypes(markers, context);
		this.right.checkTypes(markers, context);
		
		IType type1 = this.left.getType();
		IType type2 = this.right.getType();
		if (!type1.equals(type2))
		{
			Marker m = markers.create(this.position, "swap.type");
			m.addInfo("Left-Hand Type: " + type1);
			m.addInfo("Right-Hand Type: " + type2);
		}
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
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.writeStatement(writer);
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		int lineNumber = this.getLineNumber();
		IValue leftInstance = this.left.getInstance();
		IDataMember leftField = this.left.getField();
		IValue rightInstance = this.right.getInstance();
		IDataMember rightField = this.right.getField();
		
		if (leftInstance != null)
		{
			leftInstance.writeExpression(writer);
		}
		leftField.writeGet(writer, null, lineNumber);
		
		if (rightInstance != null)
		{
			rightInstance.writeExpression(writer);
		}
		rightField.writeGet(writer, null, lineNumber);
		
		if (leftInstance != null)
		{
			leftInstance.writeExpression(writer);
		}
		leftField.writeSet(writer, null, null, lineNumber);
		
		if (rightInstance != null)
		{
			rightInstance.writeExpression(writer);
		}
		rightField.writeSet(writer, null, null, lineNumber);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" :=: ");
		this.right.toString(prefix, buffer);
	}
}
