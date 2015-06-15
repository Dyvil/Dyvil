package dyvil.tools.compiler.ast.imports;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import dyvil.lang.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class MultiImport extends ASTNode implements IImport
{
	private IImport[]	children	= new IImport[2];
	private int			childrenCount;
	
	public MultiImport(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int importTag()
	{
		return MULTI;
	}
	
	@Override
	public void addImport(IImport iimport)
	{
		int index = this.childrenCount++;
		if (index >= this.children.length)
		{
			IImport[] temp = new IImport[index];
			System.arraycopy(this.children, 0, temp, 0, this.children.length);
			this.children = temp;
		}
		this.children[index] = iimport;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context, boolean using)
	{
		for (int i = 0; i < this.childrenCount; i++)
		{
			this.children[i].resolveTypes(markers, context, using);
		}
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		for (int i = 0; i < this.childrenCount; i++)
		{
			Package pack = this.children[i].resolvePackage(name);
			if (pack != null)
			{
				return pack;
			}
		}
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		for (int i = 0; i < this.childrenCount; i++)
		{
			IClass iclass = this.children[i].resolveClass(name);
			if (iclass != null)
			{
				return iclass;
			}
		}
		return null;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		for (int i = 0; i < this.childrenCount; i++)
		{
			IField match = this.children[i].resolveField(name);
			if (match != null)
			{
				return match;
			}
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		for (int i = 0; i < this.childrenCount; i++)
		{
			this.children[i].getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void write(DataOutputStream dos) throws IOException
	{
		dos.writeShort(this.childrenCount);
		for (int i = 0; i < this.childrenCount; i++)
		{
			IImport child = this.children[i];
			dos.writeByte(child.importTag());
			child.write(dos);
		}
	}
	
	@Override
	public void read(DataInputStream dis) throws IOException
	{
		this.childrenCount = dis.readShort();
		this.children = new IImport[this.childrenCount];
		for (int i = 0; i < this.childrenCount; i++)
		{
			IImport child = IImport.fromTag(dis.readByte());
			child.read(dis);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Import.multiImportStart);
		Util.astToString(prefix, this.children, this.childrenCount, Formatting.Import.multiImportSeperator, buffer);
		buffer.append(Formatting.Import.multiImportEnd);
	}
}
