package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.parser.type.TypeParameterParser;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.token.IToken;

public interface IParserManager
{
	void report(IToken token, String message);

	void report(Marker error);
	
	default void setOperatorMap(IOperatorMap operators)
	{
	}
	
	default IOperatorMap getOperatorMap()
	{
		return null;
	}
	
	default Operator getOperator(Name name)
	{
		return null;
	}
	
	void stop();
	
	void skip();
	
	void skip(int n);
	
	void reparse();
	
	void jump(IToken token);
	
	void setParser(Parser parser);
	
	Parser getParser();
	
	void pushParser(Parser parser);
	
	void pushParser(Parser parser, boolean reparse);
	
	void popParser();
	
	void popParser(boolean reparse);
	
	// Parser Factory Methods
	
	default ExpressionParser newExpressionParser(IValueConsumer valueConsumer)
	{
		return new ExpressionParser(valueConsumer);
	}
	
	default TypeParser newTypeParser(ITypeConsumer typeConsumer)
	{
		return new TypeParser(typeConsumer);
	}
	
	default AnnotationParser newAnnotationParser(IAnnotation annotation)
	{
		return new AnnotationParser(annotation);
	}
	
	default TypeParameterParser newTypeParameterParser(ITypeParametric generic)
	{
		return new TypeParameterParser(generic);
	}
}
