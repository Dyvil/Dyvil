package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.expression.access.ConstructorCall;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.statement.MemberStatement;
import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.ast.statement.VariableStatement;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.gensrc.ast.Template;

public class FuncDirective extends MemberStatement
{
	public FuncDirective(IMethod member)
	{
		super(member);
	}

	public void setBlock(StatementList block)
	{
		((IMethod) this.member).setValue(convertBlock(block));
	}

	protected static StatementList convertBlock(StatementList block)
	{
		final StatementList value = new StatementList();

		// new StringWriter()
		final ConstructorCall newStringWriter = new ConstructorCall(null, Template.LazyTypes.STRING_WRITER, ArgumentList.EMPTY);

		// let writer = new StringWriter()
		final Variable writer = new Variable(Name.fromRaw("writer"), Template.LazyTypes.WRITER,
		                                     newStringWriter);
		writer.getAttributes().addFlag(Modifiers.FINAL | Modifiers.GENERATED);

		// { let writer = new StringWriter; { ... }; writer.toString }
		value.add(new VariableStatement(writer));
		value.add(block);
		value.add(new MethodCall(null, new FieldAccess(writer), Names.toString));

		return value;
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		// TODO
		buffer.append('#');
		super.toString(indent, buffer);
	}
}
