package com.fmum.client.render;

import com.fmum.common.FMUM;
import com.fmum.common.util.ObjRepository;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * {@link FMUM} allows you to load models from a repository. This has several advantages. First one
 * is that you can pack those small models into one repository so you will not have plenty of model
 * .java files that mess up your workspace. This also reduces the number of classes that will be
 * loaded into jvm and decrease memory use. Finally you can create static final instances in you
 * repository and return them by need and this makes sure you will not instantiate a model twice
 * which in most cases is meaningless.
 * 
 * @see FMUM#loadModel(String)
 * @author Giant_Salted_Fish
 */
@FunctionalInterface
@SideOnly( Side.CLIENT )
public interface ModelRepository extends ObjRepository< RenderableBase > { }
