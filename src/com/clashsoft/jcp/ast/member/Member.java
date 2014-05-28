package com.clashsoft.jcp.ast.member;

import java.util.LinkedList;
import java.util.List;

import com.clashsoft.jcp.ast.annotation.Annotation;

public abstract class Member
{
	private int				modifiers;
	
	private String			name;
	private String			type;
	
	private List<Annotation>	annotations	= new LinkedList();
	
	protected Member()
	{
	}
	
	public Member(String name, String type, int modifiers)
	{
		this.name = name;
		this.type = type;
		this.modifiers = modifiers;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	public boolean addAnnotation(Annotation annotation)
	{
		return this.annotations.add(annotation);
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public int getModifiers()
	{
		return this.modifiers;
	}
	
	public List<Annotation> getAnnotations()
	{
		return this.annotations;
	}
}
