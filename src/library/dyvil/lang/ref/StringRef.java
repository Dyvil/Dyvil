package dyvil.lang.ref;

public interface StringRef extends ObjectRef<String>
{
	@Override
	public String get();
	
	@Override
	public void set(String value);
}
