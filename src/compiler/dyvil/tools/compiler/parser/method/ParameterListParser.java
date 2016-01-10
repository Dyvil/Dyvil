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
import dyvil.tools.compiler.ast.type.ArrayType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class ParameterListParser extends Parser implements ITypeConsumer
{
	public static final int TYPE      = 1;
	public static final int NAME      = 2;
	public static final int SEPERATOR = 4;
	
	protected IParameterList paramList;
	
	private ModifierList   modifiers;
	private AnnotationList annotations;
	
	private IType      type;
	private IParameter parameter;
	private boolean    varargs;
	
	public ParameterListParser(IParameterList paramList)
	{
		this.paramList = paramList;
		this.mode = TYPE;
	}
	
	private void reset()
	{
		this.mode = TYPE;
		this.modifiers = null;
		this.annotations = null;
		this.type = null;
		this.parameter = null;
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
			if (type == DyvilSymbols.ELLIPSIS)
			{
				this.varargs = true;
				return;
			}
			if (type == DyvilKeywords.THIS)
			{
				this.mode = SEPERATOR;
				this.annotations = null;
				this.modifiers = null;
				this.varargs = false;
				if (this.paramList instanceof IParameterized && !((IParameterized) this.paramList)
						.setReceiverType(this.type))
				{
					pm.report(token, "parameter.receivertype.invalid");
					return;
				}
				return;
			}
			this.mode = SEPERATOR;
			if (ParserUtil.isIdentifier(type))
			{
				if (this.varargs)
				{
					this.paramList.setVarargs();
					this.type = new ArrayType(this.type);
				}
				
				this.parameter = this.paramList instanceof IClass ?
						new ClassParameter(token.nameValue(), this.type) :
						new MethodParameter(token.nameValue(), this.type);
				this.parameter.setPosition(token.raw());
				this.parameter.setModifiers(this.modifiers == null ? EmptyModifiers.INSTANCE : this.modifiers);
				this.parameter.setAnnotations(this.annotations);
				this.parameter.setVarargs(this.varargs);
				this.paramList.addParameter(this.parameter);

				this.annotations = null;
				this.modifiers = null;
				this.varargs = false;
				
				return;
			}
			pm.report(token, "parameter.identifier");
			return;
		case SEPERATOR:
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			if (type == BaseSymbols.EQUALS)
			{
				pm.pushParser(pm.newExpressionParser(this.parameter));
				return;
			}
			this.reset();
			if (type == BaseSymbols.COMMA || type == BaseSymbols.SEMICOLON)
			{
				return;
			}
			pm.report(token, "parameter.comma");
			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
