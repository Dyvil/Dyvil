package dyvilx.tools.compiler.ast.expression.access;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.compiler.ast.classes.AnonymousClass;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class ClassConstructorCall extends ConstructorCall
{
	private @NonNull AnonymousClass nestedClass;

	public ClassConstructorCall(SourcePosition position, IType type, ArgumentList arguments)
	{
		super(position, type, arguments);
		this.nestedClass = new AnonymousClass(position);
	}

	public AnonymousClass getNestedClass()
	{
		return this.nestedClass;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);

		final IClass theClass = this.type.getTheClass();
		if (theClass != null)
		{
			if (theClass.isInterface())
			{
				this.nestedClass.getInterfaces().add(this.type);
			}
			else
			{
				this.nestedClass.setSuperType(this.type);
			}
		}

		this.nestedClass.resolveTypes(markers, context);
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context, boolean report)
	{
		if (!this.type.isResolved())
		{
			return this;
		}

		final IClass theClass = this.type.getTheClass();
		if (theClass == null)
		{
			return this;
		}

		final IType type = theClass.isInterface() ? Types.OBJECT : this.type;

		final MatchList<IConstructor> candidates = this.resolveCandidates(context, type);
		if (candidates.hasCandidate())
		{
			this.checkArguments(markers, context, candidates.getBestMember());
			return this;
		}

		if (report)
		{
			reportResolve(markers, candidates, this.position, type, this.arguments);
			return this;
		}
		return null;
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		this.arguments.resolve(markers, context);

		this.resolveCall(markers, context, true);

		final IClass enclosingClass = context.getThisClass();
		assert enclosingClass != null;

		this.nestedClass.setConstructor(this.constructor);
		this.nestedClass.setEnclosingClass(enclosingClass);

		final IHeaderUnit header = enclosingClass.getHeader();
		assert header != null;

		this.nestedClass.setHeader(header);
		header.addCompilable(this.nestedClass);

		this.nestedClass.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);

		this.nestedClass.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.arguments.check(markers, context);

		this.nestedClass.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();

		this.nestedClass.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.arguments.cleanup(compilableList, classCompilableList);
		this.nestedClass.cleanup(compilableList, classCompilableList);

		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.nestedClass.writeConstructorCall(writer, this.arguments, this.lineNumber());
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		super.toString(indent, buffer);

		ClassBody body = this.nestedClass.getBody();
		if (body != null)
		{
			body.toString(indent, buffer);
		}
		else
		{
			buffer.append(" {}");
		}
	}
}
