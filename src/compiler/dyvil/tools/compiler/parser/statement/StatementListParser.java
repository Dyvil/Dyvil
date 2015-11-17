package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.access.FieldAssignment;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.statement.Closure;
import dyvil.tools.compiler.ast.statement.FieldInitializer;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.EmulatorParser;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class StatementListParser extends EmulatorParser implements IValueConsumer, ITypeConsumer
{
	private static final int	OPEN_BRACKET	= 1;
	private static final int	EXPRESSION		= 2;
	private static final int	TYPE			= 4;
	private static final int	SEPARATOR		= 8;
	
	protected IValueConsumer consumer;
	
	private boolean			applied;
	private StatementList	statementList;
	
	private Name			label;
	private IType			type;
	private int				modifiers;
	private AnnotationList	annotations;
	
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
		
		this.modifiers = 0;
		this.annotations = null;
	}
	
	@Override
	public void report(IToken token, String message)
	{
		this.pm.jump(this.firstToken);
		this.pm.pushParser(this.pm.newExpressionParser(this));
		this.reset();
		this.mode = SEPARATOR;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		
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
				pm.report(token, "Invalid Statement List - '{' expected");
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
			int i;
			if ((i = ModifierTypes.MEMBER.parse(type)) != -1)
			{
				this.modifiers |= i;
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
			//$FALL-THROUGH$
		case TYPE:
			if (ParserUtil.isIdentifier(type) && token.next().type() == BaseSymbols.EQUALS)
			{
				if (this.type != null)
				{
					Variable variable = new Variable(token.raw(), token.nameValue(), this.type);
					variable.setModifiers(this.modifiers);
					variable.setAnnotations(this.annotations);
					
					FieldInitializer fi = new FieldInitializer(variable);
					pm.pushParser(pm.newExpressionParser(variable));
					this.setValue(fi);
				}
				else if (token != this.firstToken)
				{
					this.parser.parse(this, token);
					pm.reparse();
					return;
				}
				
				this.reset();
				this.mode = SEPARATOR;
				pm.skip();
				return;
			}
			else if (this.parser == null)
			{
				pm.jump(this.firstToken);
				this.reset();
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = SEPARATOR;
				return;
			}
			this.parser.parse(this, token);
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
			
			if (type == 0)
			{
				this.consumer.setValue(this.statementList);
				pm.popParser();
				pm.report(token, "Invalid Statement List - '}' expected");
				return;
			}
			pm.report(token, "Invalid Statement List - ';' expected");
			return;
		}
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
	}
}
