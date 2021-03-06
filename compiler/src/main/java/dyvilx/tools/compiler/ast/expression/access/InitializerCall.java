package dyvilx.tools.compiler.ast.expression.access;

import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public class InitializerCall implements ICall
{
	protected SourcePosition position;

	protected boolean isSuper;
	protected ArgumentList arguments;

	// Metadata
	protected IType        targetType;
	protected IConstructor constructor;

	public InitializerCall(SourcePosition position, boolean isSuper)
	{
		this.position = position;
		this.isSuper = isSuper;
		this.arguments = ArgumentList.EMPTY;
	}

	public InitializerCall(SourcePosition position, boolean isSuper, ArgumentList arguments, IType targetType)
	{
		this.position = position;
		this.isSuper = isSuper;
		this.arguments = arguments;
		this.targetType = targetType;
	}

	public InitializerCall(SourcePosition position, boolean isSuper, ArgumentList arguments, IType targetType, IConstructor constructor)
	{
		this.position = position;
		this.constructor = constructor;
		this.arguments = arguments;
		this.targetType = targetType;
		this.isSuper = isSuper;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public int valueTag()
	{
		return INITIALIZER_CALL;
	}

	@Override
	public ArgumentList getArguments()
	{
		return this.arguments;
	}

	@Override
	public void setArguments(ArgumentList arguments)
	{
		this.arguments = arguments;
	}

	public boolean isSuper()
	{
		return this.isSuper;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		return Types.VOID;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.arguments.resolveTypes(markers, context);
	}

	private IType getTargetType(IContext context)
	{
		if (this.targetType != null)
		{
			return this.targetType;
		}

		final IType type = context.getThisType();

		if (!this.isSuper)
		{
			return this.targetType = type;
		}
		return this.targetType = type.getTheClass().getSuperType();
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context, boolean report)
	{
		final IType targetType = this.getTargetType(context);
		if (targetType == null || !targetType.isResolved())
		{
			return this;
		}

		final MatchList<IConstructor> matches = IContext.resolveConstructors(context, targetType, this.arguments);
		if (matches.hasCandidate())
		{
			this.constructor = matches.getBestMember();
			this.constructor.checkArguments(markers, this.position, context, this.targetType, this.arguments);
			return this;
		}

		if (report)
		{
			ConstructorCall.reportResolve(markers, matches, this.position, targetType, this.arguments);
			return this;
		}
		return null;
	}

	@Override
	public void resolveArguments(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.arguments.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.arguments.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.arguments.foldConstants();
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		writer.visitVarInsn(Opcodes.ALOAD, 0);
		this.constructor.writeArguments(writer, this.arguments);
		this.constructor.writeInvoke(writer, this.lineNumber());
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.isSuper ? "super" : "this");
		this.arguments.toString(prefix, buffer);
	}
}
