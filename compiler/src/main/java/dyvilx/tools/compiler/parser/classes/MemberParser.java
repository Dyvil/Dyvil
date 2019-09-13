package dyvilx.tools.compiler.parser.classes;

import dyvilx.tools.compiler.ast.constructor.IInitializer;
import dyvilx.tools.compiler.ast.consumer.IMemberConsumer;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.annotation.ModifierParser;
import dyvilx.tools.compiler.parser.statement.StatementListParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public final class MemberParser<T extends IDataMember> extends AbstractMemberParser
{
	protected static final int DECLARATOR = 0;

	// Flags

	public static final int NO_FIELD_PROPERTIES = 1;

	// ----------

	protected IMemberConsumer<T> consumer;

	private int flags;

	public MemberParser(IMemberConsumer<T> consumer)
	{
		this.consumer = consumer;
		// this.mode = TYPE;
	}

	public MemberParser<T> withFlags(int flag)
	{
		this.flags |= flag;
		return this;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case DECLARATOR:
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
				if (token.isInferred())
				{
					return;
				}
				// Fallthrough
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				pm.reparse();
				// Fallthrough
			case Tokens.EOF:
				if (!this.attributes.isEmpty())
				{
					pm.report(token, "member.declarator");
				}

				pm.popParser();
				return;
			case DyvilKeywords.INIT: // constructor declaration or initializer
				if (token.next().type() == BaseSymbols.OPEN_CURLY_BRACKET) // initializer
				{
					final IInitializer initializer = this.consumer.createInitializer(token.raw(), this.attributes);
					this.consumer.addInitializer(initializer);
					this.mode = END;
					pm.pushParser(new StatementListParser(initializer::setValue));
					return;
				}

				this.mode = END;
				pm.pushParser(new ConstructorParser(this.consumer, this.attributes), true);
				return;
			case DyvilKeywords.CONST:
			case DyvilKeywords.LET:
			case DyvilKeywords.VAR:
				final FieldParser<T> parser = new FieldParser<>(this.consumer, this.attributes);
				if ((this.flags & NO_FIELD_PROPERTIES) != 0)
				{
					parser.withFlags(FieldParser.NO_PROPERTIES);
				}
				pm.pushParser(parser, true);
				this.mode = END;
				return;
			case DyvilKeywords.CASE:
				if (!Tokens.isIdentifier(token.next().type()))
				{
					break;
				}

				pm.pushParser(new EnumConstantParser(this.consumer), true);
				this.mode = END;
				return;
			case DyvilKeywords.FUNC:
			case DyvilKeywords.OPERATOR:
				this.mode = END;
				pm.pushParser(new MethodParser(this.consumer, this.attributes), true);
				return;
			}

			final int classType = ModifierParser.parseClassTypeModifier(token, pm);
			if (classType >= 0)
			{
				this.attributes.addFlag(classType);
				ClassDeclarationParser parser = new ClassDeclarationParser(this.consumer, this.attributes);
				pm.pushParser(parser);
				this.mode = END;
				return;
			}

			if (this.parseAttribute(pm, token))
			{
				return;
			}

			pm.report(token, "member.declarator");
			return;
		case END:
			pm.popParser(type != Tokens.EOF);
		}
	}

	@Override
	public boolean reportErrors()
	{
		return this.mode != DECLARATOR && super.reportErrors();
	}
}
