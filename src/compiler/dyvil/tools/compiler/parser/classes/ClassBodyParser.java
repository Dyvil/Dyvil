package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.IClassBodyConsumer;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.*;
import dyvil.tools.compiler.ast.modifiers.*;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.method.ExceptionListParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.statement.StatementListParser;
import dyvil.tools.compiler.parser.type.TypeVariableListParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class ClassBodyParser extends Parser implements ITypeConsumer
{
	protected static final int TYPE           = 1;
	protected static final int NAME_OPERATOR  = 2;
	protected static final int NAME           = 4;
	protected static final int GENERICS_END   = 8;
	protected static final int PARAMETERS     = 16;
	protected static final int PARAMETERS_END = 32;
	protected static final int FIELD_END      = 64;
	protected static final int PROPERTY_END   = 128;
	protected static final int METHOD_VALUE   = 256;
	protected static final int METHOD_END     = 512;
	
	protected IClass             theClass;
	protected IClassBodyConsumer consumer;
	
	private IType          type;
	private ModifierSet    modifiers = new ModifierList();
	private AnnotationList annotations;
	
	private IMember member;
	
	public ClassBodyParser(IClass theClass, IClassBodyConsumer consumer)
	{
		this.theClass = theClass;
		this.consumer = consumer;
		this.mode = TYPE;
	}
	
	public ClassBodyParser(IClassBodyConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = TYPE;
	}
	
	private void reset()
	{
		this.mode = TYPE;
		this.modifiers = new ModifierList();
		this.annotations = null;
		this.type = null;
		this.member = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		
		switch (this.mode)
		{
		case TYPE:
			switch (type)
			{
			case 0:
				// no error
				pm.popParser();
				return;
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				pm.popParser(true);
				return;
			case BaseSymbols.SEMICOLON:
				if (token.isInferred())
				{
					return;
				}
				this.reset();
				return;
			case DyvilKeywords.NEW:
				if (this.theClass == null)
				{
					this.mode = TYPE;
					pm.report(token, "Cannot define a constructor in this context");
					return;
				}
				Constructor c = new Constructor(token.raw(), this.theClass, this.modifiers);
				c.setAnnotations(this.annotations);
				this.member = c;
				this.mode = PARAMETERS;
				return;
			}
			Modifier modifier;
			if ((modifier = BaseModifiers.parseMemberModifier(token, pm)) != null)
			{
				this.modifiers.addModifier(modifier);
				return;
			}

			int classType;
			if ((classType = ModifierUtil.readClassTypeModifier(token, pm)) >= 0)
			{
				if (this.theClass == null)
				{
					this.reset();
					pm.report(token, "Cannot define a class in this context");
					return;
				}

				this.modifiers.addIntModifier(classType);
				ClassDeclarationParser parser = new ClassDeclarationParser(this.theClass, this.modifiers,
				                                                           this.annotations);
				pm.pushParser(parser);
				this.reset();
				return;
			}
			if (type == DyvilSymbols.AT)
			{
				if (this.annotations == null)
				{
					this.annotations = new AnnotationList();
				}
				
				Annotation annotation = new Annotation(token.raw());
				this.annotations.addAnnotation(annotation);
				pm.pushParser(pm.newAnnotationParser(annotation));
				return;
			}
			pm.pushParser(pm.newTypeParser(this), true);
			this.mode = NAME_OPERATOR;
			return;
		case NAME_OPERATOR:
			if (type == DyvilKeywords.OPERATOR)
			{
				this.mode = NAME;
				return;
			}
			// Fallthrough
		case NAME:
			if (!ParserUtil.isIdentifier(type))
			{
				this.reset();
				pm.report(token, "Invalid Member Declaration - Name expected");
				return;
			}
			IToken next = token.next();
			type = next.type();
			if (type == BaseSymbols.SEMICOLON || type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				Field f = new Field(token.raw(), this.theClass, token.nameValue(), this.type, this.modifiers);
				f.setAnnotations(this.annotations);
				this.consumer.addField(f);
				
				if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
				{
					pm.popParser(true);
					return;
				}
				
				pm.skip();
				this.reset();
				return;
			}
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = PARAMETERS;
				
				AbstractMethod m = new CodeMethod(token.raw(), this.theClass, token.nameValue(), this.type,
				                                  this.modifiers);
				m.setAnnotations(this.annotations);
				this.member = m;
				return;
			}
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				Property p = new Property(token.raw(), this.theClass, token.nameValue(), this.type, this.modifiers);
				p.setAnnotations(this.annotations);
				this.member = p;
				this.mode = FIELD_END;
				
				pm.skip();
				pm.pushParser(new PropertyParser(p));
				return;
			}
			if (type == BaseSymbols.EQUALS)
			{
				Field f = new Field(token.raw(), this.theClass, token.nameValue(), this.type, this.modifiers);
				f.setAnnotations(this.annotations);
				this.member = f;
				this.mode = FIELD_END;
				
				pm.skip();
				pm.pushParser(pm.newExpressionParser(f));
				return;
			}
			if (type == BaseSymbols.OPEN_SQUARE_BRACKET)
			{
				CodeMethod m = new CodeMethod(token.raw(), this.theClass, token.nameValue(), this.type, this.modifiers);
				m.setAnnotations(this.annotations);
				this.member = m;
				
				this.mode = GENERICS_END;
				pm.skip();
				pm.pushParser(new TypeVariableListParser(m));
				return;
			}
			
			this.mode = TYPE;
			pm.report(token, "Invalid Declaration - ';', '=', '(', '[' or '{' expected");
			return;
		case GENERICS_END:
			this.mode = PARAMETERS;
			if (type == BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Generic Type Parameter List - ']' expected");
			return;
		case PARAMETERS:
			this.mode = PARAMETERS_END;
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser((IParameterList) this.member));
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Parameter List - '(' expected");
			return;
		case PARAMETERS_END:
			this.mode = METHOD_VALUE;
			if (type == BaseSymbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			
			pm.reparse();
			pm.report(token, "Invalid Parameter List - ')' expected");
			return;
		case METHOD_VALUE:
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				this.consumer.addMethod((IMethod) this.member);
				pm.popParser(true);
				return;
			}
			if (type == BaseSymbols.SEMICOLON)
			{
				this.consumer.addMethod((IMethod) this.member);
				this.reset();
				return;
			}
			this.mode = METHOD_END;
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.pushParser(new StatementListParser((IValueConsumer) this.member), true);
				return;
			}
			if (type == BaseSymbols.EQUALS)
			{
				pm.pushParser(pm.newExpressionParser((IValueConsumer) this.member));
				return;
			}
			if (type == DyvilKeywords.THROWS)
			{
				pm.pushParser(new ExceptionListParser((IExceptionList) this.member));
				return;
			}
			
			this.mode = TYPE;
			pm.report(token, "Invalid Method Declaration - ';', '=', '{' or 'throws' expected");
			return;
		case METHOD_END:
			if (this.member instanceof IMethod)
			{
				this.consumer.addMethod((IMethod) this.member);
			}
			else
			{
				this.consumer.addConstructor((IConstructor) this.member);
			}
			pm.reparse();
			this.reset();
			return;
		case FIELD_END:
			this.consumer.addField((IField) this.member);
			pm.reparse();
			this.reset();
			return;
		case PROPERTY_END:
			this.consumer.addProperty((IProperty) this.member);
			pm.reparse();
			this.reset();
			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
