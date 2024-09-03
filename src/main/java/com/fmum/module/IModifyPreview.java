package com.fmum.module;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.function.Supplier;

public interface IModifyPreview< T >
{
	T apply();
	
	/**
	 * Indicates an error that should prevent the application of this
	 * modification.
	 *
	 * @return The error message if present.
	 */
	Optional< String > getApplicationError();
	
	/**
	 * Indicates an error should prevent the application if it is for the
	 * modification preview.
	 *
	 * @return The error message if present.
	 */
	@SideOnly( Side.CLIENT )
	Optional< String > getPreviewError();
	
	
	static < T > IModifyPreview< T > of( Supplier< T > application )
	{
		return new IModifyPreview< T >() {
			@Override
			public T apply() {
				return application.get();
			}
			
			@Override
			public Optional< String > getApplicationError() {
				return Optional.empty();
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public Optional< String > getPreviewError() {
				return Optional.empty();
			}
		};
	}
	
	static < T > IModifyPreview< T > ofAppError( Supplier< T > application, String err_msg )
	{
		return new IModifyPreview< T >() {
			@Override
			public T apply() {
				return application.get();
			}
			
			@Override
			public Optional< String > getApplicationError() {
				return Optional.of( err_msg );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public Optional< String > getPreviewError() {
				return Optional.empty();
			}
		};
	}
	
	static < T > IModifyPreview< T > ofPreviewError( Supplier< T > application, String err_msg )
	{
		return new IModifyPreview< T >() {
			@Override
			public T apply() {
				return application.get();
			}
			
			@Override
			public Optional< String > getApplicationError() {
				return Optional.of( err_msg );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public Optional< String > getPreviewError() {
				return Optional.of( err_msg );
			}
		};
	}
}
