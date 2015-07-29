package dyvil.tools.compiler.ast.member;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.IObjectCompilable;

public interface IClassMember extends IMember, IClassCompilable, IObjectCompilable
{
	public IClass getTheClass();
	
	public void setTheClass(IClass iclass);
	
	@Override
	public default void write(DataOutput out) throws IOException
	{
		this.writeSignature(out);
	}
	
	public void writeSignature(DataOutput out) throws IOException;
	
	@Override
	public default void read(DataInput in) throws IOException
	{
		this.readSignature(in);
	}
	
	public void readSignature(DataInput in) throws IOException;
}
