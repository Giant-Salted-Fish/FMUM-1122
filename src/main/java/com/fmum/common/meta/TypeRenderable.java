package com.fmum.common.meta;

import com.fmum.client.render.ModelDebugBox;
import com.fmum.client.render.Renderable;
import com.fmum.common.FMUM;
import com.fmum.common.pack.TypeParser;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TypeRenderable< T extends Renderable > extends TypeTextured
{
	public static final TypeParser< TypeRenderable< ? > >
		parser = new TypeParser<>( TypeTextured.parser );
	static
	{
		parser.addKeyword( "Model", ( s, t ) -> t.modelPath = s[ 1 ] );
		parser.addKeywords(
			( s, t ) -> t.modelScale = Double.parseDouble( s[ 1 ] ),
			"ModelScale",
			"Scale"
		);
	}
	
	public String modelPath = ModelDebugBox.PATH;
	
	/**
	 * Model of the meta. It will be set by {@link #onModelLoad()}.
	 */
	public T model = null;
	
	public double modelScale = 1D;
	
	public TypeRenderable( String name ) { super( name ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void onModelLoad() {
		this.model = this.requiredModelClass().cast( FMUM.MOD.loadModel( this.modelPath ) );
	}
	
	@Override
	public double modelScale() { return this.modelScale; }
	
	/**
	 * Used in {@link #onModelLoad()} to ensure the loaded model is the class that we want
	 */
	@SideOnly( Side.CLIENT )
	protected abstract Class< ? extends T > requiredModelClass();
}
