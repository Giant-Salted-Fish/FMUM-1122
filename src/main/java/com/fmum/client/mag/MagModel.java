package com.fmum.client.mag;

import java.util.Collection;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.fmum.client.gun.GunPartModel;
import com.fmum.client.gun.IEquippedGunPartRenderer;
import com.fmum.client.gun.IGunPartRenderer;
import com.fmum.client.module.IDeferredRenderer;
import com.fmum.client.render.IAnimator;
import com.fmum.common.ammo.IAmmoType;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.mag.IEquippedMag;
import com.fmum.common.mag.IMag;
import com.fmum.util.AngleAxis4f;
import com.fmum.util.Mesh;
import com.fmum.util.Vec3f;
import com.google.gson.annotations.SerializedName;

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
	
	private static final Vec3f[] FOLLOWER_POS = { Vec3f.ORIGIN };
	private static final AngleAxis4f[] FOLLOER_ROT = { AngleAxis4f.ORIGIN };
	
	@SerializedName( value = "followerMesh" )
	protected String followerMeshPath;
	protected transient Mesh followerMesh;
	
	// TODO: change rotation to quatarnion
	protected Vec3f[] followerPos = FOLLOWER_POS;
	protected AngleAxis4f[] followerRot = FOLLOER_ROT;
	
	protected Vec3f[] ammoPos = { };
	protected AngleAxis4f[] ammoRot = { };
	protected boolean isDoubleColumnMag = true;
	
	protected String loadingMagModuleChannel = "";
	
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
			for ( int i = this.followerRot.length; i < this.followerPos.length; ) { arr[ i++ ] = rot; }
			this.followerRot = arr;
		}
		
		for ( Vec3f p : this.ammoPos ) { p.scale( this.scale ); }
		if ( this.ammoRot.length < this.ammoPos.length )
		{
			final AngleAxis4f[] arr = new AngleAxis4f[ this.ammoPos.length ];
			System.arraycopy( this.ammoRot, 0, arr, 0, this.ammoRot.length );
			
			final boolean zeroLen = this.ammoRot.length == 0;
			final AngleAxis4f rot = zeroLen ? AngleAxis4f.ORIGIN : this.ammoRot[ 0 ];
			for ( int i = this.ammoRot.length; i < this.ammoPos.length; ) { arr[ i++ ] = rot; }
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
			C mag, IAnimator animator,
			Collection< IDeferredRenderer > renderQueue0,
			Collection< IDeferredRenderer > renderQueue1
		) {
			mag.base().getRenderTransform( mag, animator, this.mat );
			
			final boolean isLoadingMag = mag.isLoadingMag();
			final String animationChannel = isLoadingMag
				? MagModel.this.loadingMagModuleChannel : MagModel.this.moduleAnimationChannel;
			animator.applyChannel( animationChannel, this.mat );
			
			renderQueue0.add( () -> {
				GL11.glPushMatrix();
				glMulMatrix( this.mat );
				
				// Render ammo first as mag itself can be transparent.
				final int ammoCount = mag.ammoCount();
				final boolean isOddAmmoCount = mag.ammoCount() % 2 != 0;
				final boolean flipPosX = MagModel.this.isDoubleColumnMag && isOddAmmoCount;
				final int size = Math.min( ammoCount, MagModel.this.ammoPos.length );
				mag.forEachAmmo( new Consumer< IAmmoType >() {
					int i = 0;
					
					@Override
					public void accept( IAmmoType ammo )
					{
						if ( this.i < size )
						{
							GL11.glPushMatrix();
							final Vec3f pos = MagModel.this.ammoPos[ this.i ];
							GL11.glTranslatef( flipPosX ? -pos.x : pos.x, pos.y, pos.z );
							glRotatef( MagModel.this.ammoRot[ this.i ] );
							
							ammo.render();
							GL11.glPopMatrix();
							
							this.i += 1;
						}
					}
				} );
				
				mag.modifyState().doRecommendedRender( mag.texture(), () -> {
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
