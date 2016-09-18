package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class MemberStatement implements IStatement
{
	protected IClassMember member;
	
	public MemberStatement(IClassMember member)
	{
		this.member = member;
	}

	public IClassMember getMember()
	{
		return this.member;
	}

	public void setMember(IClassMember member)
	{
		this.member = member;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.member.getPosition();
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.member.setPosition(position);
	}
	
	@Override
	public int valueTag()
	{
		return MEMBER_STATEMENT;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.member.setEnclosingClass(context.getThisClass());
		this.member.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.member.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.member.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.member.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.member.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		compilableList.addCompilable(this.member);

		this.member.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
	}

	@Override
	public String toString()
	{
		return this.member.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.member.toString(prefix, buffer);
	}
}
