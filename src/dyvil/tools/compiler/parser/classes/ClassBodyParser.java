package dyvil.tools.compiler.parser.classes;

import java.util.LinkedList;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IImplementable;
import dyvil.tools.compiler.ast.api.IThrower;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.lexer.SyntaxException;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.annotation.AnnotationParametersParser;
import dyvil.tools.compiler.parser.codeblock.CodeBlockParser;
import dyvil.tools.compiler.parser.field.ValueParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.method.ThrowsDeclParser;
import dyvil.tools.compiler.parser.type.TypeParser;

public class ClassBodyParser extends Parser
{
	public static int				VARIABLE		= 1;
	public static int				METHOD			= 2;
	public static int				POST_METHOD		= 3;
	public static int				ANNOTATION		= 4;
	public static int				POST_ANNOTATION	= 5;
	
	protected AbstractClass			theClass;
	
	private int						mode;
	private LinkedList<Annotation>	annotations		= new LinkedList();
	private ClassBody				classBody;
	private Member					member;
	
	public ClassBodyParser(AbstractClass theClass)
	{
		this.theClass = theClass;
		this.classBody = theClass.getBody();
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxException
	{
		// TODO Modifiers
		if ("(".equals(value))
		{
			if (this.mode == 0)
			{
				this.mode = METHOD;
				
				Method method = new Method();
				method.setName(token.prev().value());
				pm.pushParser(new ParameterListParser(method));
				
				this.member = method;
				this.classBody.addMethod(method);
			}
			else if (this.mode == ANNOTATION)
			{
				pm.pushParser(new AnnotationParametersParser(this.annotations.getLast()));
			}
			else
			{
				throw new SyntaxException("Misplaced opening parenthesis!");
			}
			return true;
		}
		else if (")".equals(value))
		{
			if (this.mode == METHOD)
			{
				this.mode = POST_METHOD;
			}
			else
			{
				throw new SyntaxException("Misplaced closing parenthesis!");
			}
			return true;
		}
		else if ("{".equals(value))
		{
			if (this.mode == POST_METHOD)
			{
				pm.pushParser(new CodeBlockParser((IImplementable) this.member));
			}
			else
			{
				throw new SyntaxException("Misplaced opening curly brackets!");
			}
			return true;
		}
		else if ("}".equals(value))
		{
			if (this.mode == 0)
			{
				pm.popParser();
			}
			else if (this.mode == POST_METHOD)
			{
				this.mode = 0;
			}
			else
			{
				throw new SyntaxException("Misplaced closing curly brackets!");
			}
			return true;
		}
		else if ("=".equals(value))
		{
			if (this.mode == 0)
			{
				this.mode = VARIABLE;
				
				Variable variable = new Variable();
				variable.setName(token.prev().value());
				pm.pushParser(new ValueParser(variable));
				variable.setAnnotations(this.annotations);
				
				this.member = variable;
				this.annotations = new LinkedList();
				this.classBody.addVariable(variable);
			}
			else
			{
				throw new SyntaxException("Misplaced equals sign!");
			}
			return true;
		}
		else if (";".equals(value))
		{
			if (this.mode == 0)
			{
				throw new SyntaxException("Misplaced semicolon!");
			}
			else
			{
				this.mode = 0;
			}
			return true;
		}
		else if ("@".equals(value))
		{
			if (this.mode == 0)
			{
				this.mode = ANNOTATION;
				
				Annotation annotation = new Annotation();
				pm.pushParser(new TypeParser(annotation));
				
				this.annotations.add(annotation);
			}
			else
			{
				throw new SyntaxException("Misplated @ sign!");
			}
			return true;
		}
		else if ("throws".equals(value))
		{
			if (this.mode == POST_METHOD)
			{
				pm.pushParser(new ThrowsDeclParser((IThrower) this.member));
				return true;
			}
		}
		else if ("default".equals(value))
		{
			if (this.mode == POST_ANNOTATION)
			{
				// TODO Annotation Defaults
				return true;
			}
		}
		return false;
	}
}
