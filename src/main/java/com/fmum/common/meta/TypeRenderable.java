package com.fmum.common.meta;

import com.fmum.client.render.RenderableBase;
import com.fmum.common.FMUM;
import com.fmum.common.pack.TypeParser;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TypeRenderable< T extends RenderableBase > extends TypeTextured
{
	public static final TypeParser< TypeRenderable< ? > >
		parser = new TypeParser<>( TypeTextured.parser );
	static
	{
		parser.addKeyword(
			"Model",
			( s, t ) -> {
				switch( s.length )
				{
				case 4: t.scale = Double.parseDouble( s[ 3 ] );
				default: t.modelPath = s[ 1 ];
				}
			}
		);
		parser.addKeywords(
			( s, t ) -> t.scale = Double.parseDouble( s[ 1 ] ),
			"ModelScale",
			"Scale"
		);
	}
	
	public String modelPath = "debug:box";
	
	/**
	 * Model of the meta. It will be set by {@link #onModelLoad()}.
	 */
	public T model = null;
	
	public double scale = 1D;
	
	public TypeRenderable( String name ) { super( name ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void onModelLoad() {
		this.model = this.requiredModelClass().cast( FMUM.MOD.loadModel( this.modelPath ) );
	}
	
	@Override
	public double scale() { return this.scale; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void render() { this.model.render( this ); }
	
	/**
	 * Used in {@link #onModelLoad()} to ensure the loaded model is the class that we want
	 */
	@SideOnly( Side.CLIENT )
	protected abstract Class< ? extends T > requiredModelClass();
}
