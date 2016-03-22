package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IVariableConsumer;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class VariableParser extends Parser implements ITypeConsumer
{
	protected static final int TYPE = 0;
	protected static final int NAME = 1;

	protected IVariableConsumer consumer;

	private ModifierSet    modifiers;
	private AnnotationList annotations;
	private IType          type;
	private IVariable      variable;

	public VariableParser(IVariableConsumer consumer)
	{
		this.consumer = consumer;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case TYPE:
			if (type == DyvilKeywords.VAR)
			{
				this.mode = NAME;
				this.type = Types.UNKNOWN;
				return;
			}

			Modifier modifier;
			if ((modifier = BaseModifiers.parseModifier(token, pm)) != null)
			{
				if (this.modifiers == null)
				{
					this.modifiers = new ModifierList();
				}

				this.modifiers.addModifier(modifier);
				return;
			}
			if (type == DyvilSymbols.AT)
			{
				if (this.annotations == null)
				{
					this.annotations = new AnnotationList();
				}

				final Annotation annotation = new Annotation(token.raw());
				this.annotations.addAnnotation(annotation);
				pm.pushParser(pm.newAnnotationParser(annotation));
				return;
			}
			if (ParserUtil.isIdentifier(type) && token.next().type() == BaseSymbols.COLON)
			{
				// IDENTIFIER : TYPE
				this.variable = new Variable(token.raw(), token.nameValue(), Types.UNKNOWN);
				this.mode = END;
				return;
			}

			pm.pushParser(pm.newTypeParser(this), true);
			this.mode = NAME;
			return;
		case NAME:
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "variable.identifier");
				return;
			}

			this.variable = this.consumer.createVariable(token.raw(), token.nameValue(), this.type, this.modifiers,
			                                             this.annotations);

			this.mode = END;
			return;
		case END:
			if (type == BaseSymbols.COLON)
			{
				// ... IDENTIFIER : TYPE ...
				if (this.variable.getType() != Types.UNKNOWN)
				{
					pm.report(token, "variable.type.duplicate");
				}
				pm.pushParser(new TypeParser(this.variable));

				// mode stays END
				return;
			}

			this.consumer.setVariable(this.variable);
			pm.popParser(true);
		}
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
