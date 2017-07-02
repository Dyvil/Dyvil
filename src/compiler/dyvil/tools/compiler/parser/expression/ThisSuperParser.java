package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.SuperExpr;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.token.IToken;

public class ThisSuperParser extends Parser
{
	private static final int THIS_SUPER = 0;
	private static final int TYPE       = 1;
	private static final int TYPE_END   = 2;

	protected IValueConsumer valueConsumer;

	private IValue value;

	public ThisSuperParser(IValueConsumer valueConsumer)
	{
		this.valueConsumer = valueConsumer;

		// this.mode = THIS_SUPER;
	}

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
				pm.pushParser(new TypeParser(this.value, true));
				return;
			}

			this.valueConsumer.setValue(this.value);
			pm.popParser(true);
			return;
		case TYPE_END:
			pm.popParser();
			this.valueConsumer.setValue(this.value);

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
