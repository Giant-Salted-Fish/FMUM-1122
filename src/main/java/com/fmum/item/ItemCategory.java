package com.fmum.item;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public final class ItemCategory
{
	private final String[] labels;
	
	public static ItemCategory parse( String raw_category ) {
		return new ItemCategory( raw_category.split( "\\." ) );
	}
	
	private ItemCategory( String... labels ) {
		this.labels = labels;
	}
	
	public String getAt( int index ) {
		return this.labels[ index ];
	}
	
	public int getLength() {
		return this.labels.length;
	}
	
	public boolean startsWith( ItemCategory prefix )
	{
		final int prefix_len = prefix.getLength();
		return (
			prefix_len <= this.getLength()
			&& IntStream.range( 0, prefix_len ).allMatch( i -> {
				final String label_0 = this.labels[ i ];
				final String label_1 = prefix.getAt( i );
				return label_0.equals( label_1 );
			} )
		);
	}
	
	@Override
	public String toString() {
		return "CATEGORY::<" + String.join( ".", this.labels ) + ">";
	}
	
	
	public static Predicate< ItemCategory > createFilterBy( ItemCategory[] whitelist, ItemCategory[] blacklist )
	{
		return category -> {
			final int white_pri = Arrays.stream( whitelist ).mapToInt( matcher -> __match( matcher, category ) ).max().orElse( 0 );
			final int black_pri = Arrays.stream( blacklist ).mapToInt( matcher -> __match( matcher, category ) ).max().orElse( 0 );
			return white_pri > black_pri;  // White and black priority should never be equal.
		};
	}
	
	private static int __match( ItemCategory matcher, ItemCategory target ) {
		return target.startsWith( matcher ) ? matcher.getLength() + 1 : 0;
	}
}
