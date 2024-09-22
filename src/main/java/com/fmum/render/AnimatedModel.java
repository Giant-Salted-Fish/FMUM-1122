package com.fmum.render;

import com.fmum.animation.IAnimator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import gsf.util.animation.IAnimation;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;
import gsf.util.render.Mesh;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * A record class for a mesh with an animation channel.
 */
@SideOnly( Side.CLIENT )
public class AnimatedModel
{
	@Expose
	@SerializedName( "model" )
	public ModelPath model_path;
	
	@Expose
	public String anim_channel = IAnimation.CHANNEL_NONE;
	
	@Expose
	public Vec3f origin = Vec3f.ORIGIN;
	
	public Mesh mesh;
	
	public AnimatedModel() { }
	
	public AnimatedModel( ModelPath model_path ) {
		this.model_path = model_path;
	}
	
	public void render( IAnimator animator )
	{
		GL11.glPushMatrix();
		GLUtil.glTranslateV3f( this.origin );
		animator.getChannel( this.anim_channel ).glApply();
		this.mesh.draw();
		GL11.glPopMatrix();
	}
}
