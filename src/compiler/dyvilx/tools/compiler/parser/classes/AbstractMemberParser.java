package dyvilx.tools.compiler.parser.classes;

import dyvilx.tools.compiler.ast.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.modifiers.Modifier;
import dyvilx.tools.compiler.ast.modifiers.ModifierList;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.parser.annotation.AnnotationParser;
import dyvilx.tools.compiler.parser.annotation.ModifierParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.token.IToken;

public abstract class AbstractMemberParser extends Parser
{
	protected ModifierSet   modifiers;
	protected AttributeList annotations;

	protected void parseAnnotation(IParserManager pm, IToken token)
	{
		if (this.annotations == null)
		{
			this.annotations = new AttributeList();
		}

		final Annotation annotation = new Annotation(token.raw());
		this.annotations.add(annotation);
		pm.pushParser(new AnnotationParser(annotation));
	}

	protected boolean parseModifier(IParserManager pm, IToken token)
	{
		final Modifier modifier = ModifierParser.parseModifier(token, pm);
		if (modifier == null)
		{
			return false;
		}

		this.getModifiers().addModifier(modifier);
		return true;
	}

	protected ModifierSet getModifiers()
	{
		if (this.modifiers != null)
		{
			return this.modifiers;
		}
		return this.modifiers = new ModifierList();
	}
}
