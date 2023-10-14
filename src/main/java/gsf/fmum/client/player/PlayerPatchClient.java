package gsf.fmum.client.player;

import gsf.fmum.client.EventHandlerClient;
import gsf.fmum.client.FMUMClient;
import gsf.fmum.client.camera.CameraController;
import gsf.fmum.client.camera.ICameraController;
import gsf.fmum.client.input.IInput;
import gsf.fmum.common.item.IEquippedItem;
import gsf.fmum.common.player.PlayerPatch;
import gsf.fmum.util.GLUtil;
import gsf.fmum.util.Mat4f;
import gsf.fmum.util.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly( Side.CLIENT )
public final class PlayerPatchClient extends PlayerPatch
{
	private static final MouseHelper MOUSE_HELPER = new MouseHelper()
	{
		@Override
		public void mouseXYChange()
		{
			super.mouseXYChange();
			instance.onMouseXYChange( this );
		}
	};
	
	private static final Runnable DEFAULT_MOUSE_HELPER_CHECKER =
		() -> FMUMClient.MC.mouseHelper = MOUSE_HELPER;
	
	private static final Runnable FLAN_COMPATIBLE_MOUSE_HELPER_CHECKER = () -> {
		final Minecraft mc = FMUMClient.MC;
		final MouseHelper helper = mc.mouseHelper;
		final Class< ? > helper_class = helper.getClass();
		final boolean is_default_helper = helper == MOUSE_HELPER;
		final boolean is_vanilla_helper = helper_class == MouseHelper.class;
		final boolean is_compatible_helper = helper_class == FlanCompatibleMouseHelper.class;
		mc.mouseHelper = (
			is_default_helper || is_compatible_helper
			? helper
			: (
				is_vanilla_helper
				? MOUSE_HELPER
				: new FlanCompatibleMouseHelper( helper )
			)
		);
	};
	
	public static PlayerPatchClient instance;
	
	private static Runnable mouse_helper_checker;
	
	/**
	 * Player's eye position.
	 */
	public final Vec3f
		position = new Vec3f(),
		prev_position = new Vec3f();
	
	public final Vec3f
		velocity = new Vec3f(),
		prev_velocity = new Vec3f();
	
	public final Vec3f
		acceleration = new Vec3f(),
		prev_acceleration = new Vec3f();
	
	public ICameraController camera = CameraController.INSTANCE;
	
	public PlayerPatchClient() {
		instance = this;
	}
	
	@Override
	public void tick( EntityPlayer player )
	{
		super.tick( player );
		
		// TODO: maybe only update this in GUI event?
		final boolean no_gui_activate = FMUMClient.MC.currentScreen == null;
		if ( no_gui_activate )
		{
			final GameSettings settings = FMUMClient.MC.gameSettings;
			final boolean flag = EventHandlerClient.ori_view_bobbing;
			settings.viewBobbing = this.main_equipped.updateViewBobbing( flag );
		}
		
		// Update recorded player motion information.
		this.prev_position.set( this.position );
		this.position.set(
			( float ) player.posX,
			( float ) player.posY + player.getEyeHeight(),
			( float ) player.posZ
		);
		
		this.prev_velocity.set( this.velocity );
		this.velocity.set( this.position );
		this.velocity.sub( this.prev_position );
		
		this.prev_acceleration.set( this.acceleration );
		this.acceleration.set( this.velocity );
		this.acceleration.sub( this.prev_velocity );
		
		// Tick camera effects.
		this.camera.tick();
		
		// Ensure mouse helper as mods like Flan's Mod \
		// may change mouse helper under certain conditions.
		mouse_helper_checker.run();
	}
	
	public boolean onRenderHand()
	{
		// Check if hand should be rendered or not.
		// Copied from {@link EntityRenderer#renderHand(float, int)}.
		final Minecraft mc = FMUMClient.MC;
		final GameSettings settings = mc.gameSettings;
		final Entity entity = mc.getRenderViewEntity();
		if (
			settings.thirdPersonView != 0
			|| entity instanceof EntityLivingBase
				&& ( ( EntityLivingBase ) entity ).isPlayerSleeping()
			|| settings.hideGUI
			|| mc.playerController.isSpectator()
			|| this.main_equipped.onRenderHand( EnumHand.MAIN_HAND )
				&& this.off_equipped.onRenderHand( EnumHand.OFF_HAND )
		) {
			final boolean cancel_vanilla_hand_render = true;
			return cancel_vanilla_hand_render;
		}
		
		// Otherwise, setup orientation for vanilla item rendering.
		final Mat4f mat = Mat4f.locate();
		this.camera.getViewMat( mat );
		GLUtil.glMulMatrix( mat );
		mat.release();
		
		final EntityPlayerSP player = mc.player;
		GLUtil.glRotateYf( 180F - player.rotationYaw );
		GLUtil.glRotateXf( -player.rotationPitch );
		
		final boolean cancel_vanilla_hand_render = false;
		return cancel_vanilla_hand_render;
	}
	
	public boolean onRenderSpecificHand( EnumHand hand )
	{
		final IEquippedItem< ? > equipped = this.getEquipped( hand );
		return equipped.onRenderSpecificHand( hand );
	}
	
	public boolean onMouseWheelInput( int dwheel ) {
		return this.main_equipped.onMouseWheelInput( dwheel );
	}
	
	public void onInputSignal( String signal, IInput input ) {
		this.main_equipped.onInputSignal( signal, input );
	}
	
	public float getMouseSensitivity( float original ) {
		return this.main_equipped.getMouseSensitivity( original );
	}
	
	private void onMouseXYChange( MouseHelper mouse )
	{
		// This method is called right before the render and \
		// player rotation is also updated. Hence, it is the \
		// proper place to fire prepare render callback.
		this.camera.prepareRender( mouse );
		this.main_equipped.prepareRenderInHand( EnumHand.MAIN_HAND );
		this.off_equipped.prepareRenderInHand( EnumHand.OFF_HAND );
	}
	
	public boolean shouldHideCrosshair() {
		return this.main_equipped.shouldHideCrosshair();
	}
	
	public void setupInHandAndRender( Runnable callback )
	{
		GL11.glPushMatrix(); {
		
		// Do customized rendering.
		final Minecraft mc = FMUMClient.MC;
		final EntityPlayer player = mc.player;
		
		// Copied from {@link EntityRenderer#renderHand(float, int)}.
		final EntityRenderer renderer = mc.entityRenderer;
		renderer.enableLightmap();
		
		/// Copied from {@link ItemRenderer#renderItemInFirstPerson(float)}.
		// {@link ItemRenderer#rotateArroundXAndY(float, float)}.
		final Mat4f mat = Mat4f.locate();
		this.camera.getViewMat( mat );
		GLUtil.glMulMatrix( mat );
		mat.release();
		
		GLUtil.glRotateYf( 180.0F );
		RenderHelper.enableStandardItemLighting();
		
		// {@link ItemRenderer#setLightmap()}.
		final double eye_height = player.posY + player.getEyeHeight();
		final BlockPos blockPos = new BlockPos(
			player.posX, eye_height, player.posZ );
		int light = mc.world.getCombinedLight( blockPos, 0 );
		
		final float x = light & 0xFFFF;
		final float y = light >> 16;
		OpenGlHelper.setLightmapTextureCoords(
			OpenGlHelper.lightmapTexUnit, x, y );
		
		// {@link ItemRenderer#rotateArm(float)} is not applied to avoid shift.
		
		// TODO: Re-scale may not be needed. Do not forget that there is a disable pair call.
		GlStateManager.enableRescaleNormal();
		
		// Setup and render!
		GLUtil.glRotateYf( 180.0F - player.rotationYaw );
		GLUtil.glRotateXf( player.rotationPitch );
		callback.run();
		
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		/// End of {@link ItemRenderer#renderItemInFirstPerson(float)}.
		
		renderer.disableLightmap();
		
		} GL11.glPopMatrix();
	}
	
	
	public static void setMouseHelperStrategy(
		boolean use_flan_compatible_helper
	) {
		mouse_helper_checker = (
			use_flan_compatible_helper
			? FLAN_COMPATIBLE_MOUSE_HELPER_CHECKER
			: DEFAULT_MOUSE_HELPER_CHECKER
		);
	}
	
	private final static class FlanCompatibleMouseHelper extends MouseHelper
	{
		private final MouseHelper wrapped;
		
		private FlanCompatibleMouseHelper( MouseHelper wrapped ) {
			this.wrapped = wrapped;
		}
		
		@Override
		public void mouseXYChange()
		{
			this.wrapped.mouseXYChange();
			instance.onMouseXYChange( this.wrapped );
		}
		
		@Override
		public void grabMouseCursor() {
			this.wrapped.grabMouseCursor();
		}
		
		@Override
		public void ungrabMouseCursor() {
			this.wrapped.ungrabMouseCursor();
		}
		
		@Override
		@SuppressWarnings( "EqualsWhichDoesntCheckParameterClass" )
		public boolean equals( Object obj ) {
			return this.wrapped.equals( obj );
		}
	}
}
