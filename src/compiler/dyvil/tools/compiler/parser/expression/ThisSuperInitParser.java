package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.expression.access.InitializerCall;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.SuperExpr;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.source.position.SourcePosition;
import dyvil.tools.parsing.token.IToken;

public class ThisSuperInitParser extends Parser
{
	private static final int THIS_SUPER       = 0;
	private static final int INIT             = 1;
	private static final int INIT_END         = 2;
	private static final int TYPE             = 4;
	private static final int TYPE_END         = 8;

	protected IValueConsumer valueConsumer;
	protected boolean        isSuper;

	private IValue value;

	public ThisSuperInitParser(IValueConsumer valueConsumer)
	{
		this.valueConsumer = valueConsumer;

		// this.mode = THIS_SUPER;
	}

	public ThisSuperInitParser(IValueConsumer valueConsumer, boolean isSuper)
	{
		this.valueConsumer = valueConsumer;
		this.isSuper = isSuper;

		this.mode = TYPE;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case THIS_SUPER:
			if (type == DyvilKeywords.THIS)
			{
				this.isSuper = false;
				this.mode = TYPE;
				return;
			}
			if (type == DyvilKeywords.SUPER)
			{
				this.isSuper = true;
				this.mode = TYPE;
				return;
			}

			// Fallthrough
		case INIT:
			if (type == DyvilKeywords.INIT)
			{
				final InitializerCall initializerCall = new InitializerCall(token.raw(), this.isSuper);
				this.value = initializerCall;

				final IToken next = token.next();
				if (next.type() == BaseSymbols.OPEN_PARENTHESIS)
				{
					pm.skip();
					ArgumentListParser.parseArguments(pm, next.next(), initializerCall);
					this.mode = INIT_END;
				}

				return;
			}

			throw new Error(); // should 'never' be reached
		case INIT_END:
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "initializer.call.close_paren");
			}

			this.valueConsumer.setValue(this.value);
			pm.popParser();
			return;
		case TYPE:
			if (type == BaseSymbols.DOT && token.next().type() == DyvilKeywords.INIT)
			{
				this.mode = INIT;
				return;
			}

			final SourcePosition position = token.prev().raw();
			this.value = this.isSuper ? new SuperExpr(position) : new ThisExpr(position);

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
				pm.report(token, this.isSuper ? "super.close_angle" : "this.close_angle");
				return;
			}

			pm.splitJump(token, 1);
		}
	}
}
