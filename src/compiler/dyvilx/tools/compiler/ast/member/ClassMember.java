package dyvilx.tools.compiler.ast.member;

import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.header.ClassCompilable;
import dyvilx.tools.compiler.ast.header.ObjectCompilable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ClassMember extends Member, ClassCompilable, ObjectCompilable
{
	// --------------- Enclosing Class ---------------

	IClass getEnclosingClass();

	void setEnclosingClass(IClass enclosingClass);

	// --------------- Object Serialization ---------------

	@Override
	default void write(DataOutput out) throws IOException
	{
		this.writeSignature(out);
	}

	void writeSignature(DataOutput out) throws IOException;

	@Override
	default void read(DataInput in) throws IOException
	{
		this.readSignature(in);
	}

	void readSignature(DataInput in) throws IOException;
}
