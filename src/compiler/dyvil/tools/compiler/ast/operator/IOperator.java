package dyvil.tools.compiler.ast.operator;

import dyvil.tools.parsing.Name;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface IOperator
{
	byte PREFIX = 0;
	byte INFIX = 1;
	byte POSTFIX = 2;
	byte TERNARY = 3;

	byte LEFT = 0;
	byte RIGHT = 1;
	byte NONE = 2;

	Name getName();

	void setName(Name name);

	Name getTernaryName();

	void setTernaryName(Name name);

	byte getType();

	void setType(byte type);

	byte getAssociativity();

	void setAssociativity(byte associativity);

	int getPrecedence();

	void setPrecedence(int precedence);

	int comparePrecedence(IOperator other);

	void writeData(DataOutput out) throws IOException;

	void readData(DataInput in) throws IOException;

	void toString(StringBuilder builder);
}
