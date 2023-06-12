package com.fmum.common.gun;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;
import java.util.function.Function;

public interface IFireController
{
	IFireController SAFETY = new IFireController() {
		@Override
		public int actionRounds() { return 0; }
		
		@Override
		public int getCoolDownTicks(
			int actedRounds,
			int shotCount,
			Function< Float, Float > rpmManipulator
		) { return Integer.MAX_VALUE; }
		
		@Override
		public String promptMsg() { return "fmum.msg.safety"; }
	};
	
	int actionRounds();
	
	int getCoolDownTicks( int actedRounds, int shotCount, Function< Float, Float > rpmManipulator );
	
	String promptMsg();
	
	class RPMController implements IFireController
	{
		protected String promptMsg;
		
		@SerializedName( value = "roundsPerMin", alternate = "rpm" )
		protected float roundsPerMin = 600F;
		protected int actionRounds = Integer.MAX_VALUE;
		
		public RPMController( String type ) { this.compile( type ); }
		
		public IFireController compile( String type )
		{
			final String fallback = "fmum.msg." + type;
			this.promptMsg = Optional.ofNullable( this.promptMsg ).orElse( fallback );
			return this;
		}
		
		@Override
		public int actionRounds() { return this.actionRounds; }
		
		@Override
		public int getCoolDownTicks(
			int actedRounds,
			int shotCount,
			Function< Float, Float > rpmManipulator
		) {
			final float rpm = rpmManipulator.apply( this.roundsPerMin );
			final int shiftedTicksPerMin = 20 * 60 * ( 1 << 16 );
			final int shiftedCoolDownTicks = ( int ) ( shiftedTicksPerMin / rpm );
			
			final int prevShiftedTicks = shiftedCoolDownTicks * shotCount;
			final int thisShiftedTicks = prevShiftedTicks + shiftedCoolDownTicks;
			
			final int mask = 0xFFFF0000;
			final int shiftedDeltaTicks = ( mask & thisShiftedTicks ) - ( mask & prevShiftedTicks );
			return shiftedDeltaTicks >>> 16;
		}
		
		@Override
		public String promptMsg() { return this.promptMsg; }
	}
}
