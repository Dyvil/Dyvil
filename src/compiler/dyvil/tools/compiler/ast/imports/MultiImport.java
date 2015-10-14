package dyvil.tools.compiler.ast.imports;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class MultiImport extends Import implements IImportList
{
	private IImport[]	imports	= new IImport[2];
	private int			importCount;
	
	public MultiImport(ICodePosition position)
	{
		super(position);
	}
	
	@Override
	public int importTag()
	{
		return MULTI;
	}
	
	@Override
	public int importCount()
	{
		return this.importCount;
	}
	
	@Override
	public void addImport(IImport iimport)
	{
		int index = this.importCount++;
		if (index >= this.imports.length)
		{
			IImport[] temp = new IImport[index + 1];
			System.arraycopy(this.imports, 0, temp, 0, this.imports.length);
			this.imports = temp;
		}
		this.imports[index] = iimport;
	}
	
	@Override
	public void setImport(int index, IImport iimport)
	{
		this.imports[index] = iimport;
	}
	
	@Override
	public IImport getImport(int index)
	{
		return this.imports[index];
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context, boolean using)
	{
		if (this.parent != null)
		{
			this.parent.resolveTypes(markers, context, false);
			context = this.parent.getContext();
		}
		
		for (int i = 0; i < this.importCount; i++)
		{
			this.imports[i].resolveTypes(markers, context, using);
		}
	}
	
	@Override
	public IContext getContext()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		for (int i = 0; i < this.importCount; i++)
		{
			Package pack = this.imports[i].resolvePackage(name);
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
		for (int i = 0; i < this.importCount; i++)
		{
			IClass iclass = this.imports[i].resolveClass(name);
			if (iclass != null)
			{
				return iclass;
			}
		}
		return null;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.importCount; i++)
		{
			IDataMember match = this.imports[i].resolveField(name);
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
		for (int i = 0; i < this.importCount; i++)
		{
			this.imports[i].getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		IImport.writeImport(this.parent, out);
		
		out.writeShort(this.importCount);
		for (int i = 0; i < this.importCount; i++)
		{
			IImport.writeImport(this.imports[i], out);
		}
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.parent = IImport.readImport(in);
		
		this.importCount = in.readShort();
		this.imports = new IImport[this.importCount];
		for (int i = 0; i < this.importCount; i++)
		{
			this.imports[i] = IImport.readImport(in);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.appendParent(prefix, buffer);
		buffer.append(Formatting.Import.multiImportStart);
		Util.astToString(prefix, this.imports, this.importCount, Formatting.Import.multiImportSeperator, buffer);
		buffer.append(Formatting.Import.multiImportEnd);
	}
}
