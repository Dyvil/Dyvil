package dyvil.lang;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.lang.literal.IntConvertible;
import dyvil.lang.literal.LongConvertible;
import dyvil.reflect.Modifiers;

@IntConvertible
@LongConvertible
public interface Integer extends Number
{
	@DyvilModifiers(Modifiers.PREFIX)
	Integer $tilde();
	
	Integer $bslash(Integer v);
	
	Integer $amp(Integer v);
	
	Integer $bar(Integer v);
	
	Integer $up(Integer v);
	
	Integer $lt$lt(Integer v);
	
	Integer $gt$gt(Integer v);
	
	Integer $gt$gt$gt(Integer v);
}
