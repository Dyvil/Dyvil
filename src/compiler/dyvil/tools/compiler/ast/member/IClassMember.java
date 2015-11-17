package dyvil.tools.compiler.ast.member;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.IObjectCompilable;

public interface IClassMember extends IMember, IClassCompilable, IObjectCompilable
{
	IClass getTheClass();
	
	void setTheClass(IClass iclass);
	
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
