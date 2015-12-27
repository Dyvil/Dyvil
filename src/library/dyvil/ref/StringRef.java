package dyvil.ref;

public interface StringRef extends ObjectRef<String>
{
	@Override
	String get();
	
	@Override
	void set(String value);
}
