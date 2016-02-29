package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.ast.modifiers.EmptyModifiers;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.parameter.*;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class ParameterListParser extends Parser implements ITypeConsumer
{
	public static final int TYPE      = 1;
	public static final int NAME      = 2;
	public static final int SEPARATOR = 4;
	
	protected IParameterList paramList;

	// Metadata
	private ModifierList   modifiers;
	private AnnotationList annotations;
	
	private IType   type;
	private boolean varargs;

	public ParameterListParser(IParameterList paramList)
	{
		this.paramList = paramList;
		this.mode = TYPE;
	}
	
	private void reset()
	{
		this.modifiers = null;
		this.annotations = null;
		this.type = null;
		this.varargs = false;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();

		switch (this.mode)
		{
		case TYPE:
			if (type == BaseSymbols.SEMICOLON && token.isInferred())
			{
				return;
			}

			final Modifier modifier;
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
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			this.mode = NAME;
			pm.pushParser(pm.newTypeParser(this), true);
			return;
		case NAME:
			switch (type)
			{
			case Tokens.EOF:
				pm.report(token, "parameter.identifier");
				pm.popParser();
				return;
			case DyvilSymbols.ELLIPSIS:
				if (this.varargs)
				{
					pm.report(token, "parameter.identifier");
					return;
				}
				this.varargs = true;
				return;
			case DyvilKeywords.THIS:
				this.mode = SEPARATOR;
				this.reset();
				if (this.paramList instanceof IParametric && !((IParametric) this.paramList).setReceiverType(this.type))
				{
					pm.report(token, "parameter.receivertype.invalid");
					return;
				}
				return;
			}

			this.mode = SEPARATOR;
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "parameter.identifier");
				return;
			}

			if (this.varargs)
			{
				this.paramList.setVariadic();
				this.type = new ArrayType(this.type);
			}

			final IParameter parameter = this.paramList instanceof IClass ?
					new ClassParameter(token.nameValue(), this.type) :
					new MethodParameter(token.nameValue(), this.type);
			parameter.setPosition(token.raw());
			parameter.setModifiers(this.modifiers == null ? EmptyModifiers.INSTANCE : this.modifiers);
			parameter.setAnnotations(this.annotations);
			parameter.setVarargs(this.varargs);
			this.paramList.addParameter(parameter);

			if (token.next().type() == BaseSymbols.EQUALS)
			{
				pm.skip(1);
				pm.pushParser(pm.newExpressionParser(parameter));

				return;
			}

			this.reset();
			return;
		case SEPARATOR:
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}

			this.mode = TYPE;
			this.reset();
			if (type != BaseSymbols.COMMA && type != BaseSymbols.SEMICOLON)
			{
				pm.report(token, "parameter.comma");

				if (type == Tokens.EOF)
				{
					pm.popParser();
				}
			}

			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
