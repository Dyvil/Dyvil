package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.annotation.ModifierParser;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.token.IToken;

public abstract class AbstractMemberParser extends Parser
{
	protected ModifierSet    modifiers;
	protected AnnotationList annotations;

	protected void parseAnnotation(IParserManager pm, IToken token)
	{
		if (this.annotations == null)
		{
			this.annotations = new AnnotationList();
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
