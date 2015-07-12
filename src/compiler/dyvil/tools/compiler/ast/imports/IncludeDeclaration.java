package dyvil.tools.compiler.ast.imports;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class IncludeDeclaration implements IASTNode
{
	private ICodePosition	position;
	
	private Name[]			nameParts	= new Name[3];
	private int				namePartCount;
	
	private IDyvilHeader	header;
	
	public IncludeDeclaration(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	public void addNamePart(Name name)
	{
		int index = this.namePartCount++;
		if (index >= this.nameParts.length)
		{
			Name[] temp = new Name[index + 1];
			System.arraycopy(this.nameParts, 0, temp, 0, this.nameParts.length);
			this.nameParts = temp;
		}
		this.nameParts[index] = name;
	}
	
	public IDyvilHeader getHeader()
	{
		return this.header;
	}
	
	public IClass resolveClass(Name name)
	{
		return this.header == null ? null : this.header.resolveClass(name);
	}
	
	public IType resolveType(Name name)
	{
		return this.header == null ? null : this.header.resolveType(name);
	}
	
	public IDataMember resolveField(Name name)
	{
		return this.header == null ? null : this.header.resolveField(name);
	}
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.header != null)
		{
			this.header.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	public void resolve(MarkerList markers)
	{
		Package pack = Package.rootPackage;
		int count = this.namePartCount - 1;
		for (int i = 0; i < count; i++)
		{
			pack = pack.resolvePackage(this.nameParts[i]);
			if (pack == null)
			{
				markers.add(this.position, "resolve.package", this.nameParts[i]);
				return;
			}
		}
		
		this.header = pack.resolveHeader(this.nameParts[count].qualified);
		
		if (this.header == null)
		{
			markers.add(this.position, "resolve.header", this.nameParts[count]);
			return;
		}
		
		if (!this.header.isHeader())
		{
			markers.add(this.position, "include.unit");
		}
	}
	
	public void addOperators(Map<Name, Operator> operatorMap)
	{
		if (this.header == null)
		{
			return;
		}
		
		operatorMap.putAll(this.header.getOperators());
	}
	
	public void write(DataOutput out) throws IOException
	{
		out.writeShort(this.namePartCount);
		for (int i = 0; i < this.namePartCount; i++)
		{
			out.writeUTF(this.nameParts[i].qualified);
		}
	}
	
	public void read(DataInput in) throws IOException
	{
		this.namePartCount = in.readShort();
		this.nameParts = new Name[this.namePartCount];
		for (int i = 0; i < this.namePartCount; i++)
		{
			this.nameParts[i] = Name.getQualified(in.readUTF());
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("include ");
		buffer.append(this.nameParts[0]);
		for (int i = 1; i < this.namePartCount; i++)
		{
			buffer.append('.').append(this.nameParts[i]);
		}
	}
}
