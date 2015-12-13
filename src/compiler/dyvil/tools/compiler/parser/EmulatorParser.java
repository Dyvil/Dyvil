package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.token.IToken;

public abstract class EmulatorParser extends Parser implements IParserManager
{
	protected IToken firstToken;
	
	protected Parser         tryParser;
	protected Parser         parser;
	protected IParserManager pm;
	
	protected void reset()
	{
		this.firstToken = null;
		this.tryParser = this.parser = null;
		this.pm = null;
	}
	
	protected void tryParser(IParserManager pm, IToken token, Parser parser)
	{
		this.pm = pm;
		this.firstToken = token;
		this.parser = this.tryParser = parser;
	}

	@Override
	public void report(Marker error)
	{
	}

	@Override
	public void stop()
	{
		this.pm.stop();
	}
	
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
	public void popParser(boolean reparse)
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
	public Parser newAnnotationParser(IAnnotation annotation)
	{
		return this.pm.newAnnotationParser(annotation);
	}
}
