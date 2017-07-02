package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.field.EnumConstant;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class EnumConstantParser extends AbstractMemberParser
{
	protected static final int DECLARATOR = 0;
	protected static final int NAME       = 1;
	protected static final int VALUE      = 2;

	private final IMemberConsumer consumer;

	private EnumConstant constant;

	public EnumConstantParser(IMemberConsumer consumer)
	{
		this.consumer = consumer;
		this.modifiers = new ModifierList();
	}

	public EnumConstantParser(IMemberConsumer consumer, ModifierSet modifiers, AnnotationList annotations)
	{
		this.consumer = consumer;
		this.modifiers = modifiers;
		this.annotations = annotations;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case DECLARATOR:
			if (type == DyvilKeywords.CASE)
			{
				this.mode = NAME;
			}
			return;
		case NAME:
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "field.identifier");
			}

			this.constant = new EnumConstant(token.raw(), token.nameValue(), this.getModifiers(), this.annotations);
			this.mode = VALUE;
			return;
		case VALUE:
			if (type == BaseSymbols.EQUALS)
			{
				pm.pushParser(new ExpressionParser(this.constant));
				this.mode = END;
				return;
			}
			// Fallthrough
		case END:
			if (this.consumer.acceptEnums())
			{
				//noinspection unchecked
				this.consumer.addDataMember(this.constant);
			}
			else
			{
				pm.report(this.constant.getPosition(), "field.enum.invalid");
			}
			pm.popParser(type != Tokens.EOF);
		}
	}
}
