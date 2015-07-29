package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.operator.Operators;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.parser.type.TypeVariableParser;

public interface IParserManager
{
	public void report(SyntaxError error);
	
	public default void setOperatorMap(IOperatorMap operators)
	{
	}
	
	public default IOperatorMap getOperatorMap()
	{
		return null;
	}
	
	public default Operator getOperator(Name name)
	{
		return Operators.map.get(name);
	}
	
	public void skip();
	
	public void skip(int n);
	
	public void reparse();
	
	public void jump(IToken token);
	
	public void setParser(Parser parser);
	
	public Parser getParser();
	
	public void pushParser(Parser parser);
	
	public void pushParser(Parser parser, boolean reparse);
	
	public void popParser();
	
	public void popParser(boolean reparse);
	
	// Parser Factory Methods
	
	public default Parser newExpressionParser(IValueConsumer valueConsumer)
	{
		return new ExpressionParser(valueConsumer);
	}
	
	public default Parser newTypeParser(ITypeConsumer typeConsumer)
	{
		return new TypeParser(typeConsumer);
	}
	
	public default Parser newAnnotationParser(Annotation annotation)
	{
		return new AnnotationParser(annotation);
	}
	
	public default Parser newTypeVariableParser(IGeneric generic)
	{
		return new TypeVariableParser(generic);
	}
}
