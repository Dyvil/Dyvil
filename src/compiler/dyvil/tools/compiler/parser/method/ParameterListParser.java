package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.member.IAnnotationList;
import dyvil.tools.compiler.ast.parameter.IParameterized;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ParameterListParser extends Parser implements IAnnotationList, ITyped
{
	public static final int		TYPE		= 1;
	public static final int		NAME		= 2;
	public static final int		SEPERATOR	= 4;
	
	protected IParameterized	parameterized;
	
	private int					modifiers;
	private Annotation[]		annotations	= new Annotation[2];
	private int					annotationCount;
	
	private IType				type;
	private Parameter			parameter;
	private boolean				varargs;
	
	public ParameterListParser(IParameterized parameterized)
	{
		this.parameterized = parameterized;
		this.mode = TYPE;
	}
	
	@Override
	public void reset()
	{
		this.mode = TYPE;
		this.modifiers = 0;
		this.annotationCount = 0;
		this.type = null;
		this.parameter = null;
		this.varargs = false;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == TYPE)
		{
			int i = 0;
			String value = token.value();
			if ((i = ModifierTypes.PARAMETER.parse(value)) != -1)
			{
				this.modifiers |= i;
				return;
			}
			if (value.charAt(0) == '@')
			{
				pm.pushParser(new AnnotationParser(this), true);
				return;
			}
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			
			this.mode = NAME;
			pm.pushParser(new TypeParser(this), true);
			return;
		}
		if (this.mode == NAME)
		{
			if (token.equals("..."))
			{
				this.varargs = true;
				return;
			}
			this.mode = SEPERATOR;
			if (ParserUtil.isIdentifier(type))
			{
				this.parameter = new Parameter(0, token.value(), this.type);
				this.parameter.modifiers = this.modifiers;
				this.parameter.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.parameterized.addParameter(this.parameter);
				
				if (this.varargs)
				{
					this.parameter.setVarargs();
					this.parameterized.setVarargs();
					this.varargs = false;
				}
				return;
			}
			throw new SyntaxError(token, "Invalid Parameter Declaration - Name expected");
		}
		if (this.mode == SEPERATOR)
		{
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			if (type == Tokens.EQUALS)
			{
				pm.pushParser(new ExpressionParser(this.parameter));
				return;
			}
			this.reset();
			if (type == Tokens.COMMA)
			{
				return;
			}
			if (type == Tokens.SEMICOLON)
			{
				this.parameter.seperator = ';';
				return;
			}
			throw new SyntaxError(token, "Invalid Parameter Declaration - ',' expected");
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	private Annotation[] getAnnotations()
	{
		Annotation[] a = new Annotation[this.annotationCount];
		System.arraycopy(this.annotations, 0, a, 0, this.annotationCount);
		return a;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		int index = this.annotationCount++;
		if (this.annotationCount > this.annotations.length)
		{
			Annotation[] temp = new Annotation[this.annotationCount];
			System.arraycopy(this.annotations, 0, temp, 0, index);
			this.annotations = temp;
		}
		this.annotations[index] = annotation;
	}
	
	// Override Methods
	
	@Override
	public void setAnnotation(int index, Annotation annotation)
	{
	}
	
	@Override
	public void removeAnnotation(int index)
	{
	}
	
	@Override
	public Annotation getAnnotation(int index)
	{
		return null;
	}
	
	@Override
	public Annotation getAnnotation(IType type)
	{
		return null;
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
}
