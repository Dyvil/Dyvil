package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.access.FieldAssignment;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.NestedMethod;
import dyvil.tools.compiler.ast.modifiers.*;
import dyvil.tools.compiler.ast.statement.Closure;
import dyvil.tools.compiler.ast.statement.FieldInitializer;
import dyvil.tools.compiler.ast.statement.MethodStatement;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.EmulatorParser;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.classes.ClassBodyParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class StatementListParser extends EmulatorParser implements IValueConsumer, ITypeConsumer
{
	private static final int OPEN_BRACKET          = 1;
	private static final int EXPRESSION            = 2;
	private static final int TYPE                  = 4;
	private static final int VARIABLE_NAME         = 8;
	private static final int METHOD_PARAMETERS_END = 16;
	private static final int METHOD_VALUE          = 32;
	private static final int SEPARATOR             = 64;
	
	protected IValueConsumer consumer;
	
	private boolean       applied;
	private StatementList statementList;
	
	private Name           label;
	private IType          type;
	private ModifierSet    modifiers;
	private AnnotationList annotations;

	private IMethod method;
	
	public StatementListParser(IValueConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = OPEN_BRACKET;
	}
	
	public void setApplied(boolean applied)
	{
		this.applied = applied;
	}
	
	@Override
	protected void reset()
	{
		super.reset();
		
		this.mode = EXPRESSION;
		this.label = null;
		this.type = null;
		
		this.modifiers = null;
		this.annotations = null;
	}
	
	@Override
	public void report(IToken token, String message)
	{
		this.revertExpression(this.pm);
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		
		if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
		{
			if (this.firstToken != null)
			{
				pm.jump(this.firstToken);
				this.reset();
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = 0;
				return;
			}
			
			this.consumer.setValue(this.statementList);
			pm.popParser();
			return;
		}
		
		switch (this.mode)
		{
		case OPEN_BRACKET:
			this.mode = EXPRESSION;
			this.statementList = this.applied ? new Closure(token) : new StatementList(token);
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "statementlist.close_brace");
				pm.reparse();
			}
			return;
		case EXPRESSION:
			if (type == BaseSymbols.SEMICOLON)
			{
				return;
			}
			if (ParserUtil.isIdentifier(type))
			{
				int nextType = token.next().type();
				if (nextType == BaseSymbols.COLON)
				{
					this.label = token.nameValue();
					pm.skip();
					return;
				}
				if (nextType == BaseSymbols.EQUALS)
				{
					FieldAssignment fa = new FieldAssignment(token.raw(), null, token.nameValue());
					pm.skip();
					pm.pushParser(pm.newExpressionParser(fa));
					this.setValue(fa);
					this.mode = SEPARATOR;
					return;
				}
			}
			Modifier modifier;
			if ((modifier = BaseModifiers.parseMemberModifier(token, pm)) != null)
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
				
				Annotation a = new Annotation(token.raw());
				pm.pushParser(pm.newAnnotationParser(a));
				this.annotations.addAnnotation(a);
				return;
			}
			
			this.tryParser(pm, token, pm.newTypeParser(this));
			this.mode = TYPE;
			// Fallthrough
		case TYPE:
			if (this.parser == null)
			{
				this.revertExpression(pm);
				return;
			}

			this.parser.parse(this, token);
			return;
		case VARIABLE_NAME:
		{
			final int nextType = token.next().type();
			if (ParserUtil.isIdentifier(type))
			{
				if (nextType == BaseSymbols.EQUALS)
				{
					final Variable variable = new Variable(token.raw(), token.nameValue(), this.type);
					variable.setModifiers(this.modifiers == null ? EmptyModifiers.INSTANCE : this.modifiers);
					variable.setAnnotations(this.annotations);

					final FieldInitializer fieldInitializer = new FieldInitializer(variable);
					this.setValue(fieldInitializer);

					pm.skip();
					pm.pushParser(pm.newExpressionParser(variable));

					this.reset();
					this.mode = SEPARATOR;
					return;
				}
				else if (nextType == BaseSymbols.OPEN_PARENTHESIS)
				{
					final NestedMethod nestedMethod = new NestedMethod(token.raw(), token.nameValue(), this.type,
					                                                   this.modifiers == null ?
							                                                   new ModifierList() :
							                                                   this.modifiers);
					nestedMethod.setAnnotations(this.annotations);

					final MethodStatement methodStatement = new MethodStatement(nestedMethod);
					this.setValue(methodStatement);

					pm.skip();
					pm.pushParser(new ParameterListParser(nestedMethod));

					this.reset();
					this.method = nestedMethod;
					this.mode = METHOD_PARAMETERS_END;
					return;
				}
			}

			this.revertExpression(pm);
			return;
		}
		case METHOD_PARAMETERS_END:
		{
			this.mode = METHOD_VALUE;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "method.parameters.close_paren");
				pm.reparse();
			}
			return;
		}
		case METHOD_VALUE:
			if (ClassBodyParser.parseMethodBody(pm, type, this.method))
			{
				this.mode = SEPARATOR;
				return;
			}

			pm.reparse();
			pm.report(token, "method.body.separator");
			return;
		case SEPARATOR:
			this.mode = EXPRESSION;
			if (type == BaseSymbols.SEMICOLON)
			{
				return;
			}
			if (token.prev().type() == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				return;
			}
			
			if (type == Tokens.EOF)
			{
				this.consumer.setValue(this.statementList);
				pm.popParser();
				pm.report(token, "statementlist.close_brace");
				return;
			}
			pm.report(token, "statementlist.semicolon");
			return;
		}
	}

	public void revertExpression(IParserManager pm)
	{
		pm.jump(this.firstToken);
		this.reset();
		pm.pushParser(pm.newExpressionParser(this));
		this.mode = SEPARATOR;
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.label != null)
		{
			this.statementList.addValue(value, new Label(this.label, value));
			this.label = null;
		}
		else
		{
			this.statementList.addValue(value);
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
		this.mode = VARIABLE_NAME;
	}
}
