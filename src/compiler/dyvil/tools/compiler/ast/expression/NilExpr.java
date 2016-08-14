package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class NilExpr extends LiteralConversion
{
	public static final class LazyFields
	{
		public static final IClass NIL_CONVERTIBLE_CLASS = Types.LITERALCONVERTIBLE_CLASS
			                                                   .resolveClass(Name.fromRaw("FromNil"));

		private LazyFields()
		{
			// no instances
		}
	}

	public NilExpr()
	{
		super(null);
	}

	public NilExpr(ICodePosition position)
	{
		super(position);
	}

	@Override
	public int valueTag()
	{
		return NIL;
	}

	@Override
	public boolean isType(IType type)
	{
		return type.getAnnotation(LazyFields.NIL_CONVERTIBLE_CLASS) != null;
	}

	@Override
	public int getTypeMatch(IType type)
	{
		return this.isType(type) ? CONVERSION_MATCH : MISMATCH;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IAnnotation annotation = type.getAnnotation(LazyFields.NIL_CONVERTIBLE_CLASS);
		if (annotation != null)
		{
			this.name = getMethodName(annotation);
			return super.withType(type, typeContext, markers, context);
		}

		markers.add(Markers.semanticError(this.position, "nil.type", type));
		return this;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	protected void reportResolve(MarkerList markers, MatchList<IMethod> matches)
	{
		markers.add(Markers.semanticError(this.position, "nil.method", this.type.toString(), this.name));
	}

	@Override
	public String toString()
	{
		return "nil";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("nil");
	}
}
