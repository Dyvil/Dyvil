package dyvil.tools.compiler.ast.expression;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.pattern.ICase;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class MatchCase implements ICase, IContext
{
	protected IPattern			pattern;
	protected IValue			condition;
	protected IValue			action;
	
	private transient IContext	context;
	
	@Override
	public IPattern getPattern()
	{
		return this.pattern;
	}
	
	@Override
	public void setPattern(IPattern pattern)
	{
		this.pattern = pattern;
	}
	
	@Override
	public IValue getCondition()
	{
		return this.condition;
	}
	
	@Override
	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}
	
	@Override
	public IValue getAction()
	{
		return this.action;
	}
	
	@Override
	public void setAction(IValue action)
	{
		this.action = action;
	}
	
	public boolean isExhaustive()
	{
		return this.pattern.isExhaustive() && this.condition == null;
	}
	
	@Override
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IDyvilHeader getHeader()
	{
		return this.context.getHeader();
	}
	
	@Override
	public IClass getThisClass()
	{
		return this.context.getThisClass();
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public IType resolveType(Name name)
	{
		return this.context.resolveType(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.context.resolveTypeVariable(name);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		IDataMember field = this.pattern.resolveField(name);
		if (field != null)
		{
			return field;
		}
		
		return this.context.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return this.context.handleException(type);
	}
	
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
		if (this.action != null)
		{
			this.action.resolveTypes(markers, context);
		}
	}
	
	public void resolve(MarkerList markers, IType type, IContext context)
	{
		if (pattern != null)
		{
			pattern = pattern.resolve(markers, context);
			
			IPattern pattern1 = pattern.withType(type, markers);
			if (pattern1 == null)
			{
				Marker marker = markers.create(pattern.getPosition(), "pattern.type");
				marker.addInfo("Pattern Type: " + pattern.getType());
				marker.addInfo("Value Type: " + type);
			}
			else
			{
				pattern = pattern1;
			}
		}
		
		this.context = context;
		if (this.condition != null)
		{
			// TODO Check boolean type
			this.condition = this.condition.resolve(markers, this);
		}
		
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, this);
		}
		this.context = null;
	}
	
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.context = context;
		if (this.condition != null)
		{
			this.condition.checkTypes(markers, this);
		}
		if (this.action != null)
		{
			this.action.checkTypes(markers, this);
		}
		this.context = null;
	}
	
	public void check(MarkerList markers, IContext context)
	{
		this.context = context;
		if (this.condition != null)
		{
			this.condition.check(markers, this);
		}
		if (this.action != null)
		{
			this.action.check(markers, this);
		}
		this.context = null;
	}
	
	public void foldConstants()
	{
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		if (this.action != null)
		{
			this.action = this.action.foldConstants();
		}
	}
	
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.context = context;
		if (this.condition != null)
		{
			this.condition = this.condition.cleanup(this, compilableList);
		}
		if (this.action != null)
		{
			this.action = this.action.cleanup(this, compilableList);
		}
		this.context = null;
	}
	
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("case ");
		if (this.pattern != null)
		{
			this.pattern.toString(prefix, buffer);
		}
		if (this.condition != null)
		{
			buffer.append(" if ");
			this.condition.toString(prefix, buffer);
		}
		buffer.append(" => ");
		if (this.action != null)
		{
			this.action.toString(prefix, buffer);
		}
	}
}
