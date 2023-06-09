package com.fmum.common.gun;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.Map.Entry;
import java.util.Optional;

public interface IFireController
{
	IFireController SAFETY = new IFireController() {
		@Override
		public int actionRounds() { return 0; }
		
		@Override
		public int getCoolDownForShoot( int shotCount, int actedRounds ) {
			return Integer.MAX_VALUE;
		}
		
		@Override
		public String promptMsg() { return "fmum.msg.safety"; }
	};
	
	int actionRounds();
	
	int getCoolDownForShoot( int shotCount, int actedRounds );
	
	String promptMsg();
	
	class RPMController implements IFireController
	{
		protected final String promptMsg;
		protected final int shiftedCoolDownTicks;
		protected final int actionRounds;
		
		public RPMController( String name )
		{
			this.promptMsg = "fmum.msg." + name;
			final int shiftedTicksPerMin = 20 * 60 * ( 1 << 16 );
			this.shiftedCoolDownTicks = ( int ) ( shiftedTicksPerMin / 600F );
			this.actionRounds = Integer.MAX_VALUE;
		}
		
		public RPMController( Entry< String, JsonElement > e, JsonDeserializationContext ctx )
		{
			final ControllerAttrs attr = ctx.deserialize( e.getValue(), ControllerAttrs.class );
			final int shiftedTicksPerMin = 20 * 60 * ( 1 << 16 );
			this.shiftedCoolDownTicks = ( int ) ( shiftedTicksPerMin / attr.rpm );
			this.actionRounds = attr.actionRounds;
			this.promptMsg = Optional.ofNullable( attr.promoteMsg )
				.orElse( "fmum.msg." + e.getKey() );
		}
		
		@Override
		public int actionRounds() { return this.actionRounds; }
		
		@Override
		public int getCoolDownForShoot( int shotCount, int actedRounds )
		{
			final int prevShiftedTicks = this.shiftedCoolDownTicks * shotCount;
			final int thisShiftedTicks = prevShiftedTicks + this.shiftedCoolDownTicks;
			
			final int mask = 0xFFFF0000;
			final int shiftedDelta = ( mask & thisShiftedTicks ) - ( mask & prevShiftedTicks );
			return shiftedDelta >>> 16;
		}
		
		@Override
		public String promptMsg() { return this.promptMsg; }
	}
	
	class ControllerAttrs
	{
		public String promoteMsg;
		
		@SerializedName( value = "rpm", alternate = "roundsPerMin" )
		public float rpm = 600F;
		
		public int actionRounds = Integer.MAX_VALUE;
	}
}
