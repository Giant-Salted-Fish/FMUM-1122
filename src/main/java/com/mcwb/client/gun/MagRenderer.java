package com.mcwb.client.gun;

import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.module.IDeferredPriorityRenderer;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.client.render.IRenderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.util.AngleAxis4f;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Mesh;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class MagRenderer< T extends IMag< ? > >
	extends GunPartRenderer< T > implements IMagRenderer< T >
{
	public static final BuildableLoader< IRenderer > LOADER
		= new BuildableLoader<>( "mag", json -> MCWB.GSON.fromJson( json, MagRenderer.class ) );
	
	private static final Vec3f HOLD_POS = new Vec3f( -25F / 160F, -30F / 160F, 70F / 160F );
	private static final Vec3f HOLD_ROT = new Vec3f( -15F, 0F, -10F );
	
	private static final Vec3f[] AMMO_POS = { };
	private static final AngleAxis4f[] AMMO_ROT = { };
	
	private static final Vec3f[] FOLLOWER_POS = { Vec3f.ORIGIN };
	private static final AngleAxis4f[] FOLLOER_ROT = { AngleAxis4f.ORIGIN };
	
	@SerializedName( value = "followerMesh" )
	protected String followerMeshPath = "";
	
	protected transient Mesh followerMesh;
	
	// TODO: change rotation to quatarnion
	protected Vec3f[] followerPos = FOLLOWER_POS;
	protected AngleAxis4f[] followerRot = FOLLOER_ROT;
	
	protected Vec3f[] ammoPos = AMMO_POS;
	protected AngleAxis4f[] ammoRot = AMMO_ROT;
	
	public MagRenderer()
	{
		this.holdPos = HOLD_POS;
		this.holdRot = HOLD_ROT;
	}
	
	@Override
	public IRenderer build( String path, IContentProvider provider )
	{
		super.build( path, provider );
		
		for( int i = this.followerPos.length; i-- > 0; this.followerPos[ i ].scale( this.scale ) );
		if( this.followerRot.length < this.followerPos.length )
		{
			final AngleAxis4f[] arr = new AngleAxis4f[ this.followerPos.length ];
			System.arraycopy( this.followerRot, 0, arr, 0, this.followerRot.length );
			
			final AngleAxis4f rot = this.followerRot[ 0 ];
			for( int i = this.followerRot.length; i < this.followerPos.length; arr[ i++ ] = rot );
			this.followerRot = arr;
		}
		
		for( int i = this.ammoPos.length; i-- > 0; this.ammoPos[ i ].scale( this.scale ) );
		if( this.ammoRot.length < this.ammoPos.length )
		{
			final AngleAxis4f[] arr = new AngleAxis4f[ this.ammoPos.length ];
			System.arraycopy( this.ammoRot, 0, arr, 0, this.ammoRot.length );
			
			final boolean zeroLen = this.ammoRot.length == 0;
			final AngleAxis4f rot = zeroLen ? AngleAxis4f.ORIGIN : this.ammoRot[ 0 ];
			for( int i = this.ammoRot.length; i < this.ammoPos.length; arr[ i++ ] = rot );
			this.ammoRot = arr;
		}
		
		return this;
	}
	
	@Override
	protected void onMeshLoad( com.mcwb.common.load.IContentProvider provider )
	{
		super.onMeshLoad( provider );
		
		this.followerMesh = this.loadMesh( this.followerMeshPath, provider );
	}
	
	@Override
	public void prepareRender(
		T contexted,
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1
	) {
		// Better add a deferred renderer to renderQueue1 if this mag is transparent
		renderQueue0.add( () -> {
			GL11.glPushMatrix(); {
			
			final Mat4f mat = Mat4f.locate();
			IAnimator.getChannel( animator, CHANNEL_MODULE, mat );
			glMultMatrix( mat );
			mat.release();
			
			// Render ammo first as mag itself can be transparent
			final int ammoCount = contexted.ammoCount();
			final boolean flipPosX = ammoCount % 2 != 0;
			for( int i = 0, size = Math.min( ammoCount, this.ammoPos.length ); i < size; ++i )
			{
				GL11.glPushMatrix(); {
				
				final IAmmoType ammo = contexted.getAmmo( ammoCount - i - 1 );
				final Vec3f pos = this.ammoPos[ i ];
				GL11.glTranslatef( flipPosX ? -pos.x : pos.x, pos.y, pos.z );
				
				final AngleAxis4f rot = this.ammoRot[ i ];
				GL11.glRotatef( rot.angle, rot.x, rot.y, rot.z );
				
				ammo.render();
				
				} GL11.glPopMatrix();
			}
			
			contexted.modifyState().doRecommendedRender(
				contexted.texture(),
				() -> {
					GL11.glPushMatrix(); {
					
					// Follower first for the same reason
					final int idx = Math.min( ammoCount, this.followerPos.length - 1 );
					glTranslatef( this.followerPos[ idx ] );
					
					final AngleAxis4f rot = this.followerRot[ idx ];
					GL11.glRotatef( rot.angle, rot.x, rot.y, rot.z );
					
					this.followerMesh.render();
					
					} GL11.glPopMatrix();
					
					this.render();
				}
			);
			
			} GL11.glPopMatrix();
		} );
	}
}
