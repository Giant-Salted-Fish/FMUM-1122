package gsf.util.render;

import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;

public final class PoseBuilder
{
	private final Quat4f rot = new Quat4f();
	private final Vec3f pos = new Vec3f();
	
	public PoseBuilder loadIdentity()
	{
		this.rot.setIdentity();
		this.pos.setZero();
		return this;
	}
	
	public PoseBuilder set( IPose pose )
	{
		pose.getPos( this.pos );
		pose.getRot( this.rot );
		return this;
	}
	
	public PoseBuilder set( PoseBuilder builder )
	{
		this.pos.set( builder.pos );
		this.rot.set( builder.rot );
		return this;
	}
	
	public PoseBuilder translate( Vec3f vec ) {
		return this.translate( vec.x, vec.y, vec.z );
	}
	
	public PoseBuilder translate( float x, float y, float z )
	{
		final Vec3f pos = this.pos;
		final float ori_x = pos.x;
		final float ori_y = pos.y;
		final float ori_z = pos.z;
		pos.set( x, y, z );
		this.rot.transform( pos, pos );
		pos.add( ori_x, ori_y, ori_z );
		return this;
	}
	
	public PoseBuilder rotateX( float angle )
	{
		this.rot.rotateX( angle );
		return this;
	}
	
	public PoseBuilder rotateY( float angle )
	{
		this.rot.rotateY( angle );
		return this;
	}
	
	public PoseBuilder rotateZ( float angle )
	{
		this.rot.rotateZ( angle );
		return this;
	}
	
	public PoseBuilder rotate( Quat4f quat )
	{
		this.rot.mul( quat );
		return this;
	}
	
	public void transform( Vec3f point, Vec3f dst )
	{
		this.rot.transform( point, dst );
		dst.add( this.pos );
	}
	
	public IPose build()
	{
		final Quat4f rot = new Quat4f();
		rot.set( this.rot );
		final Vec3f pos = new Vec3f();
		pos.set( this.pos );
		return IPose.of( pos, rot );
	}
	
	public IPose takeAndBuild() {
		return IPose.of( this.pos, this.rot );
	}
}
