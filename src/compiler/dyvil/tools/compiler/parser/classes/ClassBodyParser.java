package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.constructor.Constructor;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.constructor.Initializer;
import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.CodeMethod;
import dyvil.tools.compiler.ast.method.IExceptionList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.*;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.method.ExceptionListParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.statement.StatementListParser;
import dyvil.tools.compiler.parser.type.TypeParameterListParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class ClassBodyParser extends Parser implements ITypeConsumer
{
	protected static final int END            = 0;
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

	// Member Kinds

	private static final byte IGNORE      = 0;
	private static final byte FIELD       = 1;
	private static final byte PROPERTY    = 2;
	private static final byte METHOD      = 3;
	private static final byte CONSTRUCTOR = 4;
	private static final byte INITIALIZER = 5;

	protected IMemberConsumer consumer;
	
	private IType type;
	private ModifierSet modifiers = new ModifierList();
	private AnnotationList annotations;

	private IMember member;
	private byte    memberKind;
	
	public ClassBodyParser(IMemberConsumer consumer)
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
		final int type = token.type();
		
		switch (this.mode)
		{
		case TYPE:
			switch (type)
			{
			case END:
				// no error
				pm.popParser();
				return;
			case BaseSymbols.CLOSE_CURLY_BRACKET: // end of body
				pm.popParser(true);
				return;
			case BaseSymbols.SEMICOLON:
				if (token.isInferred())
				{
					return;
				}
				this.reset();
				return;
			case DyvilKeywords.INIT: // constructor declaration or initializer
				if (token.next().type() == BaseSymbols.OPEN_CURLY_BRACKET) // initializer
				{
					final Initializer initializer = new Initializer(token.raw(), this.modifiers);
					initializer.setAnnotations(this.annotations);
					this.member = initializer;
					this.memberKind = INITIALIZER;

					this.mode = METHOD_END;
					pm.pushParser(new StatementListParser(initializer));
					return;
				}

				this.parseConstructorDeclaration(token);
				return;
			case DyvilKeywords.NEW: // legacy, TODO drop 'new' support
				pm.report(Markers.syntaxWarning(token, "constructor.declaration.new"));
				this.parseConstructorDeclaration(token);
				return;
			}

			Modifier modifier;
			if ((modifier = BaseModifiers.parseModifier(token, pm)) != null)
			{
				this.modifiers.addModifier(modifier);
				return;
			}

			int classType;
			if ((classType = ModifierUtil.readClassTypeModifier(token, pm)) >= 0)
			{
				this.modifiers.addIntModifier(classType);
				ClassDeclarationParser parser = new ClassDeclarationParser(this.consumer, this.modifiers,
				                                                           this.annotations);
				pm.pushParser(parser);
				this.reset();
				return;
			}

			// This is not in the above switch because 'readClassTypeModifier' above has to check for '@ interface' first
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
				pm.report(token, "member.identifier");
				return;
			}

			final IToken nextToken = token.next();
			final int nextType = nextToken.type();

			switch (nextType)
			{
			case BaseSymbols.SEMICOLON:
			case BaseSymbols.CLOSE_CURLY_BRACKET:
			{
				final IField field = new Field(token.raw(), token.nameValue(), this.type, this.modifiers,
				                               this.annotations);
				this.consumer.addField(field);

				if (nextType == BaseSymbols.CLOSE_CURLY_BRACKET)
				{
					pm.popParser(true);
					return;
				}

				pm.skip();
				this.reset();
				return;
			}
			case BaseSymbols.OPEN_PARENTHESIS:
			{
				final IMethod method = new CodeMethod(token.raw(), token.nameValue(), this.type, this.modifiers,
				                                      this.annotations);
				this.memberKind = METHOD;
				this.member = method;
				this.mode = PARAMETERS;
				return;
			}
			case BaseSymbols.OPEN_CURLY_BRACKET:
			{
				final Property property = new Property(token.raw(), token.nameValue(), this.type, this.modifiers,
				                                       this.annotations);
				this.memberKind = PROPERTY;
				this.member = property;
				this.mode = PROPERTY_END;

				pm.skip();
				pm.pushParser(new PropertyParser(property));
				return;
			}
			case BaseSymbols.EQUALS:
			{
				final IField field = new Field(token.raw(), token.nameValue(), this.type, this.modifiers,
				                               this.annotations);
				this.memberKind = FIELD;
				this.member = field;
				this.mode = FIELD_END;

				pm.skip();
				pm.pushParser(pm.newExpressionParser(field));
				return;
			}
			case BaseSymbols.OPEN_SQUARE_BRACKET:
			{
				final CodeMethod method = new CodeMethod(token.raw(), token.nameValue(), this.type, this.modifiers,
				                                         this.annotations);
				this.memberKind = METHOD;
				this.member = method;
				this.mode = GENERICS_END;
				pm.skip();
				pm.pushParser(new TypeParameterListParser(method));
				return;
			}
			}
			
			this.mode = TYPE;
			pm.report(token, "class.body.declaration.invalid");
			return;
		case GENERICS_END:
			this.mode = PARAMETERS;
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(token, "method.generic.close_bracket");
			}
			return;
		case PARAMETERS:
			this.mode = PARAMETERS_END;
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser((IParameterList) this.member));
				return;
			}
			pm.reparse();
			pm.report(token, "method.parameters.open_paren");
			return;
		case PARAMETERS_END:
			this.mode = METHOD_VALUE;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "method.parameters.close_paren");
			}

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
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.pushParser(new StatementListParser((IValueConsumer) this.member), true);
				this.mode = METHOD_END;
				return;
			}
			if (type == BaseSymbols.EQUALS)
			{
				pm.pushParser(pm.newExpressionParser((IValueConsumer) this.member));
				this.mode = METHOD_END;
				return;
			}
			if (type == DyvilKeywords.THROWS)
			{
				pm.pushParser(new ExceptionListParser((IExceptionList) this.member));
				// mode stays METHOD_VALUE
				return;
			}

			pm.reparse();
			pm.report(token, "method.body.separator");
			this.mode = TYPE;
			return;
		case METHOD_END:
			switch (this.memberKind)
			{
			// case IGNORE: break;
			case METHOD:
				this.consumer.addMethod((IMethod) this.member);
				break;
			case CONSTRUCTOR:
				this.consumer.addConstructor((IConstructor) this.member);
				break;
			case INITIALIZER:
				this.consumer.addInitializer((IInitializer) this.member);
				break;
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

	public void parseConstructorDeclaration(IToken token)
	{
		this.member = new Constructor(token.raw(), this.modifiers, this.annotations);
		this.memberKind = CONSTRUCTOR;
		this.mode = PARAMETERS;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public boolean reportErrors()
	{
		return !(this.mode == PARAMETERS && this.member instanceof IConstructor) && this.mode > NAME;
	}
}
