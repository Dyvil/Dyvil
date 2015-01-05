package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.lexer.marker.Marker;

public interface IMember extends INamed, ITyped, IModified, IAnnotatable
{
	public IClass getTheClass();
	
	public int getAccessLevel();
	
	public byte getAccessibility();
	
	// States
	
	public void resolveTypes(List<Marker> markers, IContext context);
	
	public void resolve(List<Marker> markers, IContext context);
	
	public void check(List<Marker> markers);
	
	public void foldConstants();
}
