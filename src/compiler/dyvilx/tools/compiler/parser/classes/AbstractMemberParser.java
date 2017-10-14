package dyvilx.tools.compiler.parser.classes;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.CodeAnnotation;
import dyvilx.tools.compiler.ast.attribute.modifiers.Modifier;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.annotation.AnnotationParser;
import dyvilx.tools.compiler.parser.annotation.ModifierParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.token.IToken;

public abstract class AbstractMemberParser extends Parser
{
	protected @NonNull AttributeList attributes;

	public AbstractMemberParser()
	{
		this.attributes = new AttributeList();
	}

	public AbstractMemberParser(@NonNull AttributeList attributes)
	{
		this.attributes = attributes;
	}

	protected boolean parseAttribute(IParserManager pm, IToken token)
	{
		if (this.parseModifier(pm, token))
		{
			return true;
		}
		if (token.type() == DyvilSymbols.AT && token.next().type() != DyvilKeywords.INTERFACE)
		{
			this.parseAnnotation(pm, token);
			return true;
		}
		return false;
	}

	protected void parseAnnotation(IParserManager pm, IToken token)
	{
		final Annotation annotation = new CodeAnnotation(token.raw());
		this.attributes.add(annotation);
		pm.pushParser(new AnnotationParser(annotation));
	}

	protected boolean parseModifier(IParserManager pm, IToken token)
	{
		final Modifier modifier = ModifierParser.parseModifier(token, pm);
		if (modifier == null)
		{
			return false;
		}

		this.attributes.add(modifier);
		return true;
	}
}
