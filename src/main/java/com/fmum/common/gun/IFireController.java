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
	
	class PRMController implements IFireController
	{
		protected int shiftedCoolDownTicks;
		protected int actionRounds;
		
		public PRMController( JsonElement e, JsonDeserializationContext ctx )
		{
			final SimpleControllerAttrs attr = ctx.deserialize( e, SimpleControllerAttrs.class );
			final int shiftedTicksPerMin = 20 * 60 * ( 1 << 16 );
			this.shiftedCoolDownTicks = ( int ) ( shiftedTicksPerMin / attr.roundsPerMin );
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
	
	class SimpleControllerAttrs
	{
		@SerializedName( value = "roundsPerMin", alternate = "rpm" )
		public float roundsPerMin = 600F;
		
		public int actionRounds = Integer.MAX_VALUE;
	}
}
