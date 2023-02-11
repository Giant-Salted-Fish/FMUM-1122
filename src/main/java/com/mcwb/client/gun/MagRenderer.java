package com.mcwb.client.gun;

import org.lwjgl.opengl.GL11;

import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.util.Mesh;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class MagRenderer< T extends IMag > extends GunPartRenderer< T >
{
	public static final BuildableLoader< IRenderer >
		LOADER = new BuildableLoader<>( "mag", MagRenderer.class );
	
	protected static final int
		MAG = 0,
		FOLLOWER = 1;
	
	protected static final Vec3f[] AMMO_POS_ROT = { };
	protected static final Vec3f[] FOLLOWER_POS_ROT = { Vec3f.ORIGIN };
	
	protected Vec3f[] ammoPos = AMMO_POS_ROT;
	protected Vec3f[] ammoRot = AMMO_POS_ROT;
	
	// TODO: change rotation to quatarnion
	protected Vec3f[] followerPos = FOLLOWER_POS_ROT;
	protected Vec3f[] followerRot = FOLLOWER_POS_ROT;
	
	@Override
	public IRenderer build( String path, IContentProvider provider )
	{
		super.build( path, provider );
		
		for( int i = this.ammoPos.length; i-- > 0; this.ammoPos[ i ].scale( this.scale ) );
		if( this.ammoRot.length < this.ammoPos.length )
		{
			final Vec3f[] arr = new Vec3f[ this.ammoPos.length ];
			System.arraycopy( this.ammoRot, 0, arr, 0, this.ammoRot.length );
			
			final Vec3f vec = this.ammoRot.length > 0 ? this.ammoRot[ 0 ] : Vec3f.ORIGIN;
			for( int i = this.ammoRot.length; i < this.ammoPos.length; arr[ i++ ] = vec );
			this.ammoRot = arr;
		}
		
		for( int i = this.followerPos.length; i-- > 0; this.followerPos[ i ].scale( this.scale ) );
		if( this.followerRot.length < this.followerPos.length )
		{
			final Vec3f[] arr = new Vec3f[ this.followerPos.length ];
			System.arraycopy( this.followerRot, 0, arr, 0, this.followerRot.length );
			
			final Vec3f vec = this.followerRot[ 0 ];
			for( int i = this.followerRot.length; i < this.followerPos.length; arr[ i++ ] = vec );
			this.followerRot = arr;
		}
		
		return this;
	}
	
	@Override
	public void onMeshLoad()
	{
		super.onMeshLoad();
		
		// Make sure we at least have two 
		if( this.meshes.length < 2 )
			this.meshes = new Mesh[] { this.meshes[ 0 ], Mesh.NONE };
	}
	
	@Override
	public void renderModule( T contexted, IAnimator animator )
	{
		// Render ammo first as mag itself can be transparent
		final int ammoCount = contexted.ammoCount();
		final boolean flipPosX = ammoCount % 2 != 0;
		for( int i = 0, size = Math.min( ammoCount, this.ammoPos.length ); i < size; ++i )
		{
			GL11.glPushMatrix(); {
			
			final IAmmoType ammo = contexted.get( ammoCount - i - 1 );
			final Vec3f pos = this.ammoPos[ i ];
			GL11.glTranslatef( flipPosX ? -pos.x : pos.x, pos.y, pos.z );
			
			final Vec3f rot = this.ammoRot[ i ];
//			GL11.glRotatef( rot.y, 0F, 1F, 0F );
			GL11.glRotatef( rot.x, 1F, 0F, 0F );
//			GL11.glRotatef( rot.z, 0F, 0F, 1F );
			
			ammo.render();
			
			} GL11.glPopMatrix();
		}
		
		contexted.modifyState().doRecommendedRender(
			contexted.texture(),
			() -> {
				GL11.glPushMatrix(); {
				
				// Follower first for the same reason
				final int idx = Math.min( ammoCount, this.followerPos.length - 1 );
				final Vec3f pos = this.followerPos[ idx ];
				GL11.glTranslatef( pos.x, pos.y, pos.z );
				
				final Vec3f rot = this.followerRot[ idx ];
//				GL11.glRotatef( rot.y, 0F, 1F, 0F );
				GL11.glRotatef( rot.x, 1F, 0F, 0F );
//				GL11.glRotatef( rot.z, 0F, 0F, 1F );
				
				this.meshes[ FOLLOWER ].render();
				
				} GL11.glPopMatrix();
				
				this.meshes[ MAG ].render();
			}
		);
	}
}
