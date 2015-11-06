package dyvil.tools.compiler.parser.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.*;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeVariableListParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class ClassDeclarationParser extends Parser implements ITypeConsumer
{
	private static final int	NAME			= 1;
	private static final int	GENERICS		= 2;
	private static final int	GENERICS_END	= 4;
	private static final int	PARAMETERS		= 8;
	private static final int	PARAMETERS_END	= 16;
	private static final int	EXTENDS			= 32;
	private static final int	IMPLEMENTS		= 64;
	private static final int	BODY			= 128;
	private static final int	BODY_END		= 256;
	
	protected IDyvilHeader	header;
	protected IClass		outerClass;
	protected IClassList	classList;
	
	protected int				modifiers;
	protected AnnotationList	annotations;
	
	private CodeClass theClass;
	
	public ClassDeclarationParser(IDyvilHeader header)
	{
		this.header = header;
		this.classList = header;
		this.mode = NAME;
	}
	
	public ClassDeclarationParser(IDyvilHeader header, int modifiers, AnnotationList annotations)
	{
		this.header = header;
		this.classList = header;
		
		this.modifiers = modifiers;
		this.annotations = annotations;
		this.mode = NAME;
	}
	
	public ClassDeclarationParser(IClass outerClass, int modifiers, AnnotationList annotations)
	{
		this.outerClass = outerClass;
		this.header = outerClass.getHeader();
		this.classList = outerClass.getBody();
		
		this.modifiers = modifiers;
		this.annotations = annotations;
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case NAME:
			if (ParserUtil.isIdentifier(type))
			{
				this.theClass = new CodeClass(token.raw(), this.header, this.modifiers);
				this.theClass.setAnnotations(this.annotations);
				this.theClass.setOuterClass(this.outerClass);
				this.theClass.setName(token.nameValue());
				this.mode = GENERICS;
				return;
			}
			pm.report(token, "Invalid Class Declaration - Name expected");
			return;
		case GENERICS_END:
			this.mode = PARAMETERS;
			if (type == BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Generic Type Variable List - ']' expected");
			return;
		case PARAMETERS_END:
			this.mode = EXTENDS;
			if (type == BaseSymbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Class Parameter List - ')' expected");
			return;
		case GENERICS:
			if (type == BaseSymbols.OPEN_SQUARE_BRACKET)
			{
				pm.pushParser(new TypeVariableListParser(this.theClass));
				this.theClass.setGeneric();
				this.mode = GENERICS_END;
				return;
			}
		case PARAMETERS:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser(this.theClass));
				this.mode = PARAMETERS_END;
				return;
			}
		case EXTENDS:
			if (type == DyvilKeywords.EXTENDS)
			{
				if (this.theClass.hasModifier(Modifiers.INTERFACE_CLASS))
				{
					pm.pushParser(new TypeListParser(this));
					this.mode = BODY;
					return;
				}
				
				pm.pushParser(pm.newTypeParser(this));
				this.mode = IMPLEMENTS;
				return;
			}
		case IMPLEMENTS:
			if (type == DyvilKeywords.IMPLEMENTS)
			{
				pm.pushParser(new TypeListParser(this));
				this.mode = BODY;
				
				if (this.theClass.hasModifier(Modifiers.INTERFACE_CLASS))
				{
					pm.report(token, "Interfaces cannot implement other interfaces - Use 'extends' instead");
					return;
				}
				return;
			}
		case BODY:
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				IClassBody body = new ClassBody(this.theClass);
				this.theClass.setBody(body);
				pm.pushParser(new ClassBodyParser(this.theClass, body));
				this.mode = BODY_END;
				return;
			}
			if (ParserUtil.isTerminator(type))
			{
				if (token.isInferred())
				{
					IToken next = token.next();
					if (next != null)
					{
						int nextType = next.type();
						switch (nextType)
						{
						case DyvilKeywords.EXTENDS:
							this.mode = EXTENDS;
							return;
						case DyvilKeywords.IMPLEMENTS:
							this.mode = IMPLEMENTS;
							return;
						case BaseSymbols.OPEN_SQUARE_BRACKET:
							this.mode = GENERICS;
							return;
						case BaseSymbols.OPEN_PARENTHESIS:
							this.mode = PARAMETERS;
							return;
						}
					}
				}
				
				pm.popParser();
				this.classList.addClass(this.theClass);
				return;
			}
			this.mode = BODY_END;
			pm.report(token, "Invalid Class Declaration - '{' or ';' expected");
			return;
		case BODY_END:
			pm.popParser();
			this.classList.addClass(this.theClass);
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				pm.report(token, "Invalid Class Declaration - '}' expected");
			}
			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		switch (this.mode)
		{
		case IMPLEMENTS:
		case EXTENDS: // extends
			this.theClass.setSuperType(type);
			return;
		case BODY: // implements
			this.theClass.addInterface(type);
			return;
		}
	}
}
