package com.fmum.ammo;

import com.fmum.FMUM;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import com.fmum.item.ItemCategory;
import com.fmum.item.ItemType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IContentLoader;
import com.fmum.load.IMeshLoadContext;
import com.fmum.load.JsonData;
import com.fmum.render.ModelPath;
import com.fmum.render.Texture;
import com.google.gson.JsonObject;
import gsf.util.animation.IAnimator;
import gsf.util.math.AxisAngle4f;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;
import gsf.util.render.Mesh;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class AmmoType extends ItemType implements IAmmoType
{
	public static final IContentLoader< AmmoType > LOADER = IContentLoader.of(
		AmmoType::new,
		IItemType.REGISTRY, IAmmoType.REGISTRY
	);
	
	
	protected ItemCategory category;
	
	protected boolean can_shoot;
	
	protected int max_stack_size;
	
	@SideOnly( Side.CLIENT )
	protected ModelPath model_path;
	
	@SideOnly( Side.CLIENT )
	protected float fp_scale;
	
	@SideOnly( Side.CLIENT )
	protected Texture texture;
	
	@SideOnly( Side.CLIENT )
	protected Vec3f fp_pos;
	
	@SideOnly( Side.CLIENT )
	protected AxisAngle4f fp_rot;
	
	@SideOnly( Side.CLIENT )
	protected Mesh model;
	
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		FMUM.SIDE.runIfClient( () -> ctx.regisMeshLoadCallback( this::_onMeshLoad ) );
	}
	
	@Override
	public void reload( JsonObject json, IContentBuildContext ctx )
	{
		super.reload( json, ctx );
		
		final JsonData data = new JsonData( json, ctx.getGson() );
		this.category = data.get( "category", ItemCategory.class ).orElseGet( () -> ItemCategory.parse( this.name ) );
		this.can_shoot = data.getBool( "can_shoot" ).orElse( true );
		this.max_stack_size = data.getInt( "max_stack_size" ).orElse( 60 );
		FMUM.SIDE.runIfClient( () -> {
			this.model_path = data.get( "model", ModelPath.class ).orElse( ModelPath.NONE );
			this.fp_scale = data.getFloat( "fp_scale" ).orElse( 1.0F );
			this.texture = data.get( "texture", Texture.class ).orElse( Texture.GREEN );
			this.fp_pos = data.get( "fp_pos", Vec3f.class ).orElse( Vec3f.ORIGIN );
			this.fp_rot = data.get( "fp_rot", AxisAngle4f.class ).orElse( AxisAngle4f.IDENTITY );
		} );
	}
	
	@Override
	protected Item _setupVanillaItem( IContentBuildContext ctx )
	{
		final Item item = new Item() {
			@Override
			public ICapabilityProvider initCapabilities(
				@Nonnull ItemStack stack,
				@Nullable NBTTagCompound nbt
			) {
				return IItem.newProviderOf( new AmmoItem() );
			}
		};
		item.setRegistryName( this.pack_info.getNamespace(), this.name );
		item.setTranslationKey( this.name );
		item.setMaxStackSize( this.max_stack_size );
		
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			void _onItemRegis( Register< Item > evt ) {
				evt.getRegistry().register( item );
			}
			
			@SubscribeEvent
			@SideOnly( Side.CLIENT )
			void _onModelRegis( ModelRegistryEvent evt )
			{
				final ResourceLocation res_loc = Objects.requireNonNull( item.getRegistryName() );
				final ModelResourceLocation model_res = new ModelResourceLocation( res_loc, "inventory" );
				ModelLoader.setCustomModelResourceLocation( item, 0, model_res );
			}
		} );
		
		return item;
	}
	
	@SideOnly( Side.CLIENT )
	protected void _onMeshLoad( IMeshLoadContext ctx ) {
		this.model = ctx.loadMesh( this.model_path ).orElse( Mesh.NONE );
	}
	
	@Override
	public ItemStack newItemStack( short meta ) {
		return new ItemStack( this.vanilla_item, 1, meta );
	}
	
	@Override
	public ItemCategory getCategory() {
		return this.category;
	}
	
	@Override
	public boolean canShoot() {
		return this.can_shoot;
	}
	
	@Override
	public IAmmoType prepareShoot()
	{
		return null;  // FIXME
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void renderModel( IAnimator animator )
	{
		GLUtil.bindTexture( this.texture );
		GLUtil.glScale1f( this.fp_scale );
		this.model.draw();
	}
	
	
	protected class AmmoItem implements IItem
	{
		protected AmmoItem() { }
		
		@Override
		public IItemType getType() {
			return AmmoType.this;
		}
		
		@Override
		public IEquippedItem onTakeOut( EnumHand hand, EntityPlayer player ) {
			return new EquippedAmmo();
		}
		
		@Override
		public String toString() {
			return "Item<" + AmmoType.this.name + ">";
		}
	}
	
	
	protected static class EquippedAmmo implements IEquippedItem
	{
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( EnumHand hand, IItem item ) {
			return false;
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderSpecificInHand( EnumHand hand, IItem item )
		{
			final AmmoType type = ( AmmoType ) item.getType();
			
			GL11.glPushMatrix();
			GLUtil.glRotateYf( 180.0F );
			GLUtil.glTranslateV3f( type.fp_pos );
			GLUtil.glRotateAA4f( type.fp_rot );
			type.renderModel( IAnimator.NONE );
			GL11.glPopMatrix();
			return true;
		}
	}
}
