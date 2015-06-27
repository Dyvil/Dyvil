package dyvil.tools.compiler.ast.classes;

import dyvil.lang.List;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.CaptureField;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;

public class NestedClass extends CodeClass
{
	protected CaptureField[]	capturedFields;
	protected int				capturedFieldCount;
	
	public transient IContext	context;
	
	public NestedClass()
	{
	}
	
	@Override
	public void setInnerIndex(String internalName, int index)
	{
		String outerName = this.outerClass == null ? this.unit.getName() : this.outerClass.getFileName();
		String indexString = Integer.toString(index);
		
		this.name = Name.getQualified(outerName + '$' + indexString);
		this.fullName = this.unit.getFullName(indexString);
		this.internalName = this.unit.getInternalName(indexString);
	}
	
	@Override
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IClass getThisClass()
	{
		return this;
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
	public ITypeVariable resolveTypeVariable(Name name)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			ITypeVariable var = this.generics[i];
			if (var.getName() == name)
			{
				return var;
			}
		}
		
		return this.context.resolveTypeVariable(name);
	}
	
	@Override
	public IField resolveField(Name name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			if (param.getName() == name)
			{
				return param;
			}
		}
		
		IField match = this.context.resolveField(name);
		if (match == null)
		{
			return null;
		}
		
		if (!match.isVariable())
		{
			return match;
		}
		if (this.capturedFields == null)
		{
			this.capturedFields = new CaptureField[2];
			this.capturedFieldCount = 1;
			return this.capturedFields[0] = new CaptureField(this, match);
		}
		
		// Check if the variable is already in the array
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			if (this.capturedFields[i].field == match)
			{
				// If yes, return the match and skip adding the variable
				// again.
				return this.capturedFields[i];
			}
		}
		
		int index = this.capturedFieldCount++;
		if (this.capturedFieldCount > this.capturedFields.length)
		{
			CaptureField[] temp = new CaptureField[this.capturedFieldCount];
			System.arraycopy(this.capturedFields, 0, temp, 0, index);
			this.capturedFields = temp;
		}
		return this.capturedFields[index] = new CaptureField(this, match);
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
	public byte getVisibility(IClassMember member)
	{
		return this.context.getVisibility(member);
	}
}
