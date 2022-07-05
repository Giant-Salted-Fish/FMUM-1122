package com.fmum.common.util;

@FunctionalInterface
public interface ParserFunc< T >
{
	/**
	 * Parse input split
	 * 
	 * @param split String split
	 * @param dst Destination 
	 * @param sourceTrace Used in error print
	 * @throws UnknownKeywordException If the keyword is unrecognized
	 * @throws KeywordFormatException If the format is invalid
	 */
	public default void parse( String[] split, T dst, Messager sourceTrace )
		throws UnknownKeywordException, KeywordFormatException { this.parse( split, dst ); }
	
	public void parse( String[] split, T dst );
	
	public static class UnknownKeywordException extends RuntimeException
	{
		private static final long serialVersionUID = -8378482596071403931L;
		
		public UnknownKeywordException( String keyword, String source ) {
			super( "Unknown keyword <" + keyword + "> from <" + source + ">" );
		}
		
		public UnknownKeywordException( String msg ) { super( msg ); }
	}
	
	public static class KeywordFormatException extends RuntimeException
	{
		private static final long serialVersionUID = 4158902466919166241L;
		
		public KeywordFormatException( String keyword, String source, Throwable cause ) {
			super( "Error parsing keyword <" + keyword + "> from <" + source + ">", cause );
		}
		
		public KeywordFormatException( String msg, Throwable cause ) { super( msg, cause ); }
		
		// TODO: move to caller maybe?
//		public static KeywordFormatException keywordArgNotEnough( String atLeast, String supplied )
//		{
//			return new KeywordFormatException(
//				FMUM.proxy.format(
//					"fmum.keywordargnumnotenough",
//					atLeast,
//					supplied
//				)
//			);
//		}
	}
}