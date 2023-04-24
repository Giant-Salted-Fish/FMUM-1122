package com.mcwb.client.gun;

import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.google.gson.annotations.SerializedName;
import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.ammo.IAmmoType;
import com.mcwb.common.gun.IEquippedMag;
import com.mcwb.common.gun.IMag;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.util.AngleAxis4f;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Mesh;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class MagModel<
	C extends IMag< ? >,
	E extends IEquippedMag< ? extends C >,
	ER extends IEquippedGunPartRenderer< ? super E >,
	R extends IGunPartRenderer< ? super C, ? extends ER >
> extends GunPartModel< C, E, ER, R >
{
	private static final Vec3f HOLD_POS = new Vec3f( -25F / 160F, -30F / 160F, 70F / 160F );
	private static final Vec3f HOLD_ROT = new Vec3f( -15F, 0F, -10F );
	
	private static final Vec3f[] AMMO_POS = { };
	private static final AngleAxis4f[] AMMO_ROT = { };
	
	private static final Vec3f[] FOLLOWER_POS = { Vec3f.ORIGIN };
	private static final AngleAxis4f[] FOLLOER_ROT = { AngleAxis4f.ORIGIN };
	
	@SerializedName( value = "followerMesh" )
	protected String followerMeshPath;
	protected transient Mesh followerMesh;
	
	// TODO: change rotation to quatarnion
	protected Vec3f[] followerPos = FOLLOWER_POS;
	protected AngleAxis4f[] followerRot = FOLLOER_ROT;
	
	protected Vec3f[] ammoPos = AMMO_POS;
	protected AngleAxis4f[] ammoRot = AMMO_ROT;
	protected boolean isDoubleColumnMag = true;
	
	protected String loadingMagChannel = "";
	
	public MagModel()
	{
		this.holdPos = HOLD_POS;
		this.holdRot = HOLD_ROT;
	}
	
	@Override
	public Object build( String path, IContentProvider provider )
	{
		super.build( path, provider );
		
		for ( Vec3f p : this.followerPos ) { p.scale( this.scale ); }
		if ( this.followerRot.length < this.followerPos.length )
		{
			final AngleAxis4f[] arr = new AngleAxis4f[ this.followerPos.length ];
			System.arraycopy( this.followerRot, 0, arr, 0, this.followerRot.length );
			
			final AngleAxis4f rot = this.followerRot[ 0 ];
			for ( int i = this.followerRot.length; i < this.followerPos.length; arr[ i++ ] = rot );
			this.followerRot = arr;
		}
		
		for ( Vec3f p : this.ammoPos ) { p.scale( this.scale ); }
		if ( this.ammoRot.length < this.ammoPos.length )
		{
			final AngleAxis4f[] arr = new AngleAxis4f[ this.ammoPos.length ];
			System.arraycopy( this.ammoRot, 0, arr, 0, this.ammoRot.length );
			
			final boolean zeroLen = this.ammoRot.length == 0;
			final AngleAxis4f rot = zeroLen ? AngleAxis4f.ORIGIN : this.ammoRot[ 0 ];
			for ( int i = this.ammoRot.length; i < this.ammoPos.length; arr[ i++ ] = rot );
			this.ammoRot = arr;
		}
		
		return this;
	}
	
	@Override
	protected void onMeshLoad( IContentProvider provider )
	{
		super.onMeshLoad( provider );
		
		this.followerMesh = this.loadMesh( this.followerMeshPath, provider );
	}
	
	protected abstract class MagRenderer extends GunPartRenderer
	{
		@Override
		public void prepareRender(
			C contexted, IAnimator animator,
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredRenderer > renderQueue1
		) {
			contexted.base().getRenderTransform( contexted, this.mat );
			
			final boolean isLoadingMag = contexted.isLoadingMag();
			final String animationChannel = isLoadingMag
				? MagModel.this.loadingMagChannel : MagModel.this.animationChannel;
			animator.applyChannel( animationChannel, this.mat );
			
			renderQueue0.add( () -> {
				GL11.glPushMatrix();
				final Mat4f mat = Mat4f.locate();
				animator.getChannel( CHANNEL_ITEM, mat );
				mat.mul( this.mat ); // TODO: validate order
				glMulMatrix( mat );
				mat.release();
				
				// Render ammo first as mag itself can be transparent.
				final int ammoCount = contexted.ammoCount();
				final boolean isOddAmmoCount = contexted.ammoCount() % 2 != 0;
				final boolean flipPosX = MagModel.this.isDoubleColumnMag && isOddAmmoCount;
				final int size = Math.min( ammoCount, MagModel.this.ammoPos.length );
				for ( int i = 0; i < size; ++i )
				{
					GL11.glPushMatrix();
					
					final IAmmoType ammo = contexted.getAmmo( ammoCount - i - 1 );
					final Vec3f pos = MagModel.this.ammoPos[ i ];
					GL11.glTranslatef( flipPosX ? -pos.x : pos.x, pos.y, pos.z );
					glRotatef( MagModel.this.ammoRot[ i ] );
					
					ammo.render();
					
					GL11.glPopMatrix();
				}
				
				contexted.modifyState().doRecommendedRender( contexted.texture(), () -> {
					GL11.glPushMatrix();
					
					// Follower first for the same reason.
					final int idx = Math.min( ammoCount, MagModel.this.followerPos.length - 1 );
					glTranslatef( MagModel.this.followerPos[ idx ] );
					glRotatef( MagModel.this.followerRot[ idx ] );
					
					MagModel.this.followerMesh.render();
					
					GL11.glPopMatrix();
					
					MagModel.this.render();
				} );
				
				GL11.glPopMatrix();
			} );
		}
	}
}
