package dyvil.tools.compiler.parser.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class DataMemberParser<T extends IDataMember> extends Parser implements ITypeConsumer
{
	protected static final int TYPE = 0;
	protected static final int NAME = 1;

	protected IDataMemberConsumer<T> consumer;

	private ModifierSet    modifiers;
	private AnnotationList annotations;
	private IType          type;
	private T              dataMember;

	public DataMemberParser(IDataMemberConsumer<T> consumer)
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
			if (type == DyvilKeywords.LET)
			{
				this.mode = NAME;
				this.type = Types.UNKNOWN;

				if (this.modifiers == null)
				{
					this.modifiers = new ModifierList();
				}

				this.modifiers.addIntModifier(Modifiers.FINAL);
				return;
			}

			Modifier modifier;
			if ((modifier = ModifierUtil.parseModifier(token, pm)) != null)
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
				pm.pushParser(new AnnotationParser(annotation));
				return;
			}
			if (ParserUtil.isIdentifier(type) && token.next().type() == BaseSymbols.COLON)
			{
				// IDENTIFIER : TYPE
				this.dataMember = this.consumer
					                  .createDataMember(token.raw(), token.nameValue(), Types.UNKNOWN, this.modifiers,
					                                    this.annotations);
				this.mode = END;
				return;
			}

			pm.pushParser(new TypeParser(this), true);
			this.mode = NAME;
			return;
		case NAME:
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "variable.identifier");
				return;
			}

			this.dataMember = this.consumer.createDataMember(token.raw(), token.nameValue(), this.type, this.modifiers,
			                                                 this.annotations);

			this.mode = END;
			return;
		case END:
			if (type == BaseSymbols.COLON)
			{
				// ... IDENTIFIER : TYPE ...
				if (this.dataMember.getType() != Types.UNKNOWN)
				{
					pm.report(token, "variable.type.duplicate");
				}
				pm.pushParser(new TypeParser(this.dataMember));

				// mode stays END
				return;
			}

			this.consumer.addDataMember(this.dataMember);
			pm.popParser(true);
		}
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
