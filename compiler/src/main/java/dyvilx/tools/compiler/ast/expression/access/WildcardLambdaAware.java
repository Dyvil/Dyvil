package dyvilx.tools.compiler.ast.expression.access;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.LambdaExpr;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.builtin.Types;

import java.util.function.Supplier;

public interface WildcardLambdaAware extends IValue
{
	int wildcardCount();

	IValue replaceWildcards(Supplier<IParameter> nextParameter);

	static IValue transform(WildcardLambdaAware value)
	{
		int wildcards = value.wildcardCount();
		if (wildcards <= 0)
		{
			return null;
		}

		final SourcePosition position = value.getPosition();

		final ParameterList parameters = new ParameterList(wildcards);
		for (int i = 0; i < wildcards; i++)
		{
			parameters.add(
				new CodeParameter(null, position, Name.fromRaw("wildcard$" + i), Types.UNKNOWN, new AttributeList()));
		}

		final IValue replaced = value.replaceWildcards(parameters.iterator()::next);

		final LambdaExpr lambdaExpr = new LambdaExpr(position, parameters);
		lambdaExpr.setImplicitParameters(true);
		lambdaExpr.setValue(replaced);
		return lambdaExpr;
	}
}
