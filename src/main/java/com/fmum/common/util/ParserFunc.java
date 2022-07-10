package com.fmum.common.util;

@FunctionalInterface
public interface ParserFunc< T >
{
	/**
	 * Parse input string split
	 * 
	 * @param split String split
	 * @param dst Destination
	 * @throws UnknownKeywordException If the keyword is unrecognized
	 * @throws KeywordFormatException If the format is invalid
	 * @throws Exception For any other case
	 */
	public void parse( String[] split, T dst )
		throws UnknownKeywordException, KeywordFormatException, Exception;
	
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