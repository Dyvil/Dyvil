package dyvil.tools.compiler.ast.dwt;

import java.util.*;
import java.util.Map.Entry;

import dyvil.collections.SingleElementList;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.IValueMap;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.DWTUtil;
import dyvil.util.StringUtils;

public class DWTNode extends ASTNode implements IValue, INamed, IValueMap<String>
{
	public static final int			NODE		= 256;
	public static final int			LIST		= 257;
	public static final int			REFERENCE	= 258;
	
	public DWTNode					parent;
	
	public String					name;
	public String					fullName;
	public IType					type;
	public Map<String, IValue>		properties	= new TreeMap();
	
	private IClass					theClass;
	private Map<String, IMethod>	setters;
	
	public DWTNode()
	{
	}
	
	public DWTNode(ICodePosition position)
	{
		this.position = position;
	}
	
	public DWTNode(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
		this.fullName = name;
	}
	
	public void setParent(DWTNode parent)
	{
		this.fullName = parent.fullName + "$" + this.name;
		this.parent = parent;
	}
	
	@Override
	public int getValueType()
	{
		return NODE;
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return false;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
		this.fullName = qualifiedName;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.fullName = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.fullName;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.fullName.equals(name);
	}
	
	@Override
	public void setValues(Map<String, IValue> map)
	{
		this.properties = map;
	}
	
	@Override
	public void addValue(String key, IValue value)
	{
		if (value.getValueType() == NODE)
		{
			((DWTNode) value).setParent(this);
		}
		this.properties.put(key, value);
	}
	
	@Override
	public Map<String, IValue> getValues()
	{
		return this.properties;
	}
	
	@Override
	public IValue getValue(String key)
	{
		return this.properties.get(key);
	}
	
	public void addFields(Map<String, IType> fields)
	{
		fields.put(this.fullName, this.type);
		
		String s = this.fullName + "$";
		for (Entry<String, IValue> entry : this.properties.entrySet())
		{
			fields.put(s + entry.getKey(), entry.getValue().getType());
		}
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		String s = "J" + StringUtils.toTitleCase(this.name);
		this.theClass = DWTFile.javaxSwing.resolveClass(s);
		
		if (this.theClass == null)
		{
			markers.add(Markers.create(this.position, "dwt.component.type", this.name, s));
			return;
		}
		
		this.type = this.theClass.getType();
	}
	
	@Override
	public DWTNode resolve(List<Marker> markers, IContext context)
	{
		IMethod constructor = this.theClass.getBody().getMethod("<init>");
		if (constructor == null)
		{
			markers.add(Markers.create(this.position, "dwt.component.constructor"));
		}
		
		this.setters = new HashMap();
		
		for (Entry<String, IValue> entry : this.properties.entrySet())
		{
			String key = entry.getKey();
			IValue value = entry.getValue();
			int type = value.getValueType();
			if (type == LIST)
			{
				for (IValue v : ((IValueList) value).getValues())
				{
					String s1 = DWTUtil.getAddMethodName(key);
					MethodMatch m = this.theClass.resolveMethod(this, s1, new SingleElementList<IValue>(v));
					
					if (m != null)
					{
						value.resolve(markers, m.theMethod);
						continue;
					}
					markers.add(Markers.create(v.getPosition(), "dwt.property.unknown", key, this.type.toString()));
				}
			}
			else
			{
				String s1 = DWTUtil.getSetMethodName(key);
				MethodMatch m = this.theClass.resolveMethod(this, s1, new SingleElementList<IValue>(value));
				
				if (m != null)
				{
					this.setters.put(key, m.theMethod);
					value.resolve(markers, m.theMethod);
					continue;
				}
				markers.add(Markers.create(value.getPosition(), "dwt.property.unknown", key, this.type.toString()));
			}
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public DWTNode foldConstants()
	{
		return null;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	public void write(String owner, MethodWriter writer)
	{
		String internal = this.type.getInternalName();
		String extended = "L" + internal + ";";
		// Constructor
		writer.visitTypeInsn(Opcodes.NEW, internal);
		writer.visitInsn(Opcodes.DUP);
		writer.visitMethodInsn(Opcodes.INVOKESPECIAL, internal, "<init>", "()V", 0, Type.VOID);
		
		writer.visitPutStatic(owner, this.fullName, extended);
		
		for (Entry<String, IMethod> entry : this.setters.entrySet())
		{
			String key = entry.getKey();
			IMethod setter = entry.getValue();
			IValue value = this.properties.get(key);
			
			writer.visitGetStatic(owner, this.fullName, extended, this.type);
			value.writeExpression(writer);
			setter.writeCall(writer, null, Collections.EMPTY_LIST);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append('\n');
		buffer.append(prefix).append('{');
		String prefix1 = prefix + '\t';
		for (Entry<String, IValue> entry : this.properties.entrySet())
		{
			buffer.append('\n').append(prefix1).append(entry.getKey()).append(Formatting.Field.keyValueSeperator);
			IValue value = entry.getValue();
			if (value.isStatement())
			{
				buffer.append('\n').append(prefix1);
				value.toString(prefix1, buffer);
			}
			else
			{
				buffer.append(' ');
				value.toString(prefix1, buffer);
			}
		}
		buffer.append('\n').append(prefix).append('}');
	}
}
