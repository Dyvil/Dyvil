package dyvil.tools.compiler.ast.imports;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dyvil.lang.List;

import dyvil.collection.mutable.ArrayList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class SimpleImport extends Import
{
	public Name				name;
	public Name				alias;
	
	private IClass			theClass;
	private Package			thePackage;
	
	private IDataMember		field;
	private List<IMethod>	methods;
	
	public SimpleImport(ICodePosition position)
	{
		super(position);
	}
	
	public SimpleImport(ICodePosition position, Name name)
	{
		super(position);
		this.name = name;
	}
	
	@Override
	public int importTag()
	{
		return SIMPLE;
	}
	
	@Override
	public void setAlias(Name alias)
	{
		this.alias = alias;
	}
	
	@Override
	public Name getAlias()
	{
		return this.alias;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context, boolean using)
	{
		if (this.parent != null)
		{
			this.parent.resolveTypes(markers, context, false);
			context = this.parent.getContext();
		}
		
		if (using)
		{
			if (!(context instanceof IClass))
			{
				markers.add(this.position, "import.using.invalid");
				return;
			}
			
			IClassBody body = ((IClass) context).getBody();
			
			IDataMember field = body.getField(this.name);
			if (field != null)
			{
				this.field = field;
				return;
			}
			
			this.methods = new ArrayList();
			int len = body.methodCount();
			for (int i = 0; i < len; i++)
			{
				IMethod m = body.getMethod(i);
				if (m.getName() == this.name)
				{
					this.methods.add(m);
				}
			}
			if (!this.methods.isEmpty())
			{
				return;
			}
			
			markers.add(this.position, "resolve.method_field", this.name.qualified);
			return;
		}
		
		Package pack = context.resolvePackage(this.name);
		if (pack != null)
		{
			this.thePackage = pack;
			return;
		}
		
		IClass iclass = context.resolveClass(this.name);
		if (iclass != null)
		{
			this.theClass = iclass;
			return;
		}
		
		markers.add(this.position, "resolve.package", this.name.qualified);
	}
	
	@Override
	public IContext getContext()
	{
		return this.theClass != null ? this.theClass : this.thePackage;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		if (name == this.name || name == this.alias)
		{
			return this.thePackage;
		}
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		if (name == this.name || name == this.alias)
		{
			return this.theClass;
		}
		return null;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (name == this.name || name == this.alias)
		{
			return this.field;
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (name != this.name && name != this.alias)
		{
			return;
		}
		
		if (this.methods == null)
		{
			return;
		}
		for (IMethod m : this.methods)
		{
			int match = m.getSignatureMatch(name, instance, arguments);
			if (match > 0)
			{
				list.add(new MethodMatch(m, match));
			}
		}
	}
	
	@Override
	public void write(DataOutputStream dos) throws IOException
	{
		IImport.writeImport(this.parent, dos);
		
		dos.writeUTF(this.name.qualified);
		if (this.alias != null)
		{
			dos.writeUTF(this.alias.qualified);
		}
		else
		{
			dos.writeUTF("");
		}
	}
	
	@Override
	public void read(DataInputStream dis) throws IOException
	{
		this.parent = IImport.readImport(dis);
		
		this.name = Name.getQualified(dis.readUTF());
		
		String alias = dis.readUTF();
		if (!alias.isEmpty())
		{
			this.alias = Name.getQualified(alias);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.appendParent(prefix, buffer);
		buffer.append(this.name);
		if (this.alias != null)
		{
			buffer.append(Formatting.Import.aliasSeperator);
			buffer.append(this.alias);
		}
	}
}
