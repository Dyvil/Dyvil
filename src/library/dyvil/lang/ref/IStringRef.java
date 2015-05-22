package dyvil.lang.ref;

public interface IStringRef extends IObjectRef<String>
{
	@Override
	public String get();
	
	@Override
	public void set(String value);
}
