package dyvil.tools.compiler.parser.method;

import java.lang.annotation.ElementType;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.IAnnotationList;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
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
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;

public final class ParameterListParser extends Parser implements IAnnotationList, ITyped
{
	public static final int		TYPE		= 1;
	public static final int		NAME		= 2;
	public static final int		SEPERATOR	= 4;
	
	protected IParameterList	paramList;
	
	private int					modifiers;
	private Annotation[]		annotations	= new Annotation[2];
	private int					annotationCount;
	
	private IType				type;
	private IParameter			parameter;
	private boolean				varargs;
	
	public ParameterListParser(IParameterList paramList)
	{
		this.paramList = paramList;
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
			if ((i = ModifierTypes.PARAMETER.parse(type)) != -1)
			{
				this.modifiers |= i;
				return;
			}
			if (token.nameValue() == Name.at)
			{
				Annotation annotation = new Annotation(token.raw());
				this.addAnnotation(annotation);
				pm.pushParser(new AnnotationParser(annotation));
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
				if (this.varargs)
				{
					this.paramList.setVarargs();
					this.varargs = false;
					this.type = this.type.getArrayType();
				}
				
				this.parameter = this.paramList instanceof IClass ? new ClassParameter(token.nameValue(), this.type) : new MethodParameter(token.nameValue(),
						this.type);
				this.parameter.setModifiers(this.modifiers);
				this.parameter.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.paramList.addParameter(this.parameter);
				
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
			if (type == Symbols.EQUALS)
			{
				pm.pushParser(new ExpressionParser(this.parameter));
				return;
			}
			this.reset();
			if (type == Symbols.COMMA)
			{
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
	public ElementType getAnnotationType()
	{
		return null;
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
}
