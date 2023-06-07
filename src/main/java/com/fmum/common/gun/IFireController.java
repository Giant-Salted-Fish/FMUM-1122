package com.fmum.common.gun;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public interface IFireController
{
	IFireController SAFETY = new IFireController() {
		@Override
		public int actionRounds() { return 0; }
		
		@Override
		public int getCoolDownForNextRound( int shotCount, int actedRounds ) {
			return Integer.MAX_VALUE;
		}
	};
	
	int actionRounds();
	
	int getCoolDownForNextRound( int shotCount, int actedRounds );
	
	class RPMController implements IFireController
	{
		protected final int shiftedCoolDownTicks;
		protected final int actionRounds;
		
		public RPMController()
		{
			final int shiftedTicksPerMin = 20 * 60 * ( 1 << 16 );
			this.shiftedCoolDownTicks = ( int ) ( shiftedTicksPerMin / 600F );
			this.actionRounds = Integer.MAX_VALUE;
		}
		
		public RPMController( JsonElement e, JsonDeserializationContext ctx )
		{
			final ControllerAttrs attr = ctx.deserialize( e, ControllerAttrs.class );
			final int shiftedTicksPerMin = 20 * 60 * ( 1 << 16 );
			this.shiftedCoolDownTicks = ( int ) ( shiftedTicksPerMin / attr.rpm );
			this.actionRounds = attr.actionRounds;
		}
		
		@Override
		public int actionRounds() { return this.actionRounds; }
		
		@Override
		public int getCoolDownForNextRound( int shotCount, int actedRounds )
		{
			final int prevShiftedTicks = this.shiftedCoolDownTicks * shotCount;
			final int thisShiftedTicks = prevShiftedTicks + this.shiftedCoolDownTicks;
			
			final int mask = 0xFFFF0000;
			final int shiftedDelta = ( mask & thisShiftedTicks ) - ( mask & prevShiftedTicks );
			return shiftedDelta >>> 16;
		}
	}
	
	class ControllerAttrs
	{
		@SerializedName( value = "rpm", alternate = "roundsPerMin" )
		public float rpm = 600F;
		
		public int actionRounds = Integer.MAX_VALUE;
	}
}
