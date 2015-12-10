package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class SwapOperator extends AbstractValue implements IStatement
{
	public FieldAccess left;
	public FieldAccess right;
	
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
		
		IType leftType = this.left.getType();
		IType rightType = this.right.getType();
		if (!leftType.isSameType(rightType))
		{
			Marker marker = I18n.createMarker(this.position, "swap.type.incompatible");
			marker.addInfo(I18n.getString("swap.type.left", leftType));
			marker.addInfo(I18n.getString("swap.type.right", rightType));
			markers.add(marker);
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
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		int lineNumber = this.getLineNumber();
		IValue leftInstance = this.left.getInstance();
		IDataMember leftField = this.left.getField();
		IValue rightInstance = this.right.getInstance();
		IDataMember rightField = this.right.getField();

		if (leftInstance != null)
		{
			leftInstance.writeExpression(writer, leftField.getTheClass().getType());
		}
		leftField.writeGet(writer, null, lineNumber);

		if (rightInstance != null)
		{
			rightInstance.writeExpression(writer, rightField.getTheClass().getType());
		}
		rightField.writeGet(writer, null, lineNumber);

		if (leftInstance != null)
		{
			leftInstance.writeExpression(writer, leftField.getTheClass().getType());
		}
		leftField.writeSet(writer, null, null, lineNumber);

		if (rightInstance != null)
		{
			rightInstance.writeExpression(writer, rightField.getTheClass().getType());
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
