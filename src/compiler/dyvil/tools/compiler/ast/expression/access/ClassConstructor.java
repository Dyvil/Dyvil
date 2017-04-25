package dyvil.tools.compiler.ast.expression.access;

import dyvil.tools.compiler.ast.classes.AnonymousClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class ClassConstructor extends ConstructorCall
{
	private AnonymousClass nestedClass;

	public ClassConstructor(SourcePosition position)
	{
		this.position = position;
		this.nestedClass = new AnonymousClass(position);
	}

	public ClassConstructor(SourcePosition position, IType type, ArgumentList arguments)
	{
		super(position, type, arguments);
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
		this.nestedClass.writeConstructorCall(writer, this.arguments);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);

		IClassBody body = this.nestedClass.getBody();
		if (body != null)
		{
			body.toString(prefix, buffer);
		}
		else
		{
			buffer.append(" {}");
		}
	}
}
