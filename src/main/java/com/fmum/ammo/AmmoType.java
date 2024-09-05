package com.fmum.ammo;

import com.fmum.FMUM;
import com.fmum.item.FMUMItemBase;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import com.fmum.item.ItemCategory;
import com.fmum.item.ItemType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IMeshLoadContext;
import com.fmum.render.ModelPath;
import com.fmum.render.Texture;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import gsf.util.math.AxisAngle4f;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;
import gsf.util.render.Mesh;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class AmmoType extends ItemType implements IAmmoType
{
	@Expose
	protected ItemCategory category;
	
	@Expose
	protected boolean can_shoot = true;
	
	@Expose
	protected int max_stack_size = 60;
	
	@Expose
	@SerializedName( "model" )
	@SideOnly( Side.CLIENT )
	protected ModelPath model_path;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected float fp_scale;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected Texture texture;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected Vec3f fp_pos;
	
	@Expose
	@SideOnly( Side.CLIENT )
	protected AxisAngle4f fp_rot;
	
	
	protected Item vanilla_item;
	
	@SideOnly( Side.CLIENT )
	protected Mesh model;
	
	
	public AmmoType()
	{
		FMUM.SIDE.runIfClient( () -> {
			this.model_path = ModelPath.NONE;
			this.fp_scale = 1.0F;
			this.texture = Texture.GREEN;
			this.fp_pos = Vec3f.ORIGIN;
			this.fp_rot = AxisAngle4f.NO_ROT;
		} );
	}
	
	
	@Override
	public void build( JsonObject data, String fallback_name, IContentBuildContext ctx )
	{
		super.build( data, fallback_name, ctx );
		
		if ( this.category == null ) {
			this.category = ItemCategory.parse( this.name );
		}
		
		this.vanilla_item = this._createVanillaItem();
		FMUM.SIDE.runIfClient( () -> ctx.regisMeshLoadCallback( this::_onMeshLoad ) );
	}
	
	protected Item _createVanillaItem()
	{
		final FMUMItemBase item = new FMUMItemBase() {
			@Override
			public IItem getItemFrom( ItemStack stack ) {
				return new AmmoItem( stack );
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
		} );
		
		return item;
	}
	
	@SideOnly( Side.CLIENT )
	protected void _onMeshLoad( IMeshLoadContext ctx ) {
		this.model = ctx.loadMesh( this.model_path ).orElse( Mesh.NONE );
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
	public ItemStack newItemStack( short meta ) {
		return new ItemStack( this.vanilla_item, 1, meta );
	}
	
	
	protected class AmmoItem implements IItem
	{
		protected final ItemStack stack;
		
		protected AmmoItem( ItemStack stack ) {
			this.stack = stack;
		}
		
		@Override
		public IItemType getType() {
			return AmmoType.this;
		}
		
		@Override
		public ItemStack getBoundStack() {
			return this.stack;
		}
		
		@Override
		public IEquippedItem onTakeOut( EnumHand hand, EntityPlayer player ) {
			return new EquippedAmmo();
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
			GLUtil.bindTexture( type.texture );
			GLUtil.glScale1f( type.fp_scale );
			type.model.draw();
			GL11.glPopMatrix();
			return true;
		}
	}
}
