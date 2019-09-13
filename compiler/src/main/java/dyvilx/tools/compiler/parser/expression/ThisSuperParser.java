package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.SuperExpr;
import dyvilx.tools.compiler.ast.expression.ThisExpr;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class ThisSuperParser extends Parser
{
	// =============== Constants ===============

	private static final int THIS_SUPER = 0;
	private static final int TYPE       = 1;
	private static final int TYPE_END   = 2;

	// =============== Fields ===============

	protected final Consumer<IValue> consumer;

	private IValue value;

	// =============== Constructors ===============

	public ThisSuperParser(Consumer<IValue> consumer)
	{
		this.consumer = consumer;

		// this.mode = THIS_SUPER;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case THIS_SUPER:
			switch (type)
			{
			case DyvilKeywords.THIS:
				this.mode = TYPE;
				this.value = new ThisExpr(token.raw());
				return;
			case DyvilKeywords.SUPER:
				this.mode = TYPE;
				this.value = new SuperExpr(token.raw());
				return;
			}

			pm.popParser(true);
			return;
		case TYPE:
			if (ExpressionParser.isGenericCall(token, type))
			{
				this.mode = TYPE_END;

				pm.splitJump(token, 1);
				pm.pushParser(new TypeParser(this.value::setType, true));
				return;
			}

			this.consumer.accept(this.value);
			pm.popParser(true);
			return;
		case TYPE_END:
			pm.popParser();
			this.consumer.accept(this.value);

			if (!TypeParser.isGenericEnd(token, type))
			{
				pm.reparse();
				pm.report(token, this.value.valueTag() == IValue.SUPER ? "super.close_angle" : "this.close_angle");
				return;
			}

			pm.splitJump(token, 1);
		}
	}
}
