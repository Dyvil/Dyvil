package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

public abstract class EmulatorParser extends Parser implements IParserManager
{
	protected IToken	firstToken;
	protected Parser	tryParser;
	
	protected Parser			parser;
	protected IParserManager	pm;
	
	@Override
	public void skip()
	{
		this.pm.skip();
	}
	
	@Override
	public void skip(int n)
	{
		this.pm.skip(n);
	}
	
	@Override
	public void reparse()
	{
		this.pm.reparse();
	}
	
	@Override
	public void jump(IToken token)
	{
		this.pm.jump(token);
	}
	
	@Override
	public void setParser(Parser parser)
	{
		this.parser = parser;
	}
	
	@Override
	public Parser getParser()
	{
		return this.parser;
	}
	
	@Override
	public void pushParser(Parser parser)
	{
		parser.setParent(this.parser);
		this.parser = parser;
	}
	
	@Override
	public void pushParser(Parser parser, boolean reparse)
	{
		parser.setParent(this.parser);
		this.parser = parser;
		this.pm.reparse();
	}
	
	@Override
	public void popParser()
	{
		if (this.parser == this.tryParser)
		{
			this.tryParser = null;
			return;
		}
		
		this.parser = this.parser.getParent();
	}
	
	@Override
	public void popParser(boolean reparse) throws SyntaxError
	{
		if (reparse)
		{
			this.pm.reparse();
		}
		
		if (this.parser == this.tryParser)
		{
			this.tryParser = null;
			return;
		}
		
		this.parser = this.parser.getParent();
	}
	
	@Override
	public Parser newExpressionParser(IValueConsumer valueConsumer)
	{
		return this.pm.newExpressionParser(valueConsumer);
	}
	
	@Override
	public Parser newTypeParser(ITypeConsumer typeConsumer)
	{
		return this.pm.newTypeParser(typeConsumer);
	}
	
	@Override
	public Parser newAnnotationParser(Annotation annotation)
	{
		return this.pm.newAnnotationParser(annotation);
	}
}
