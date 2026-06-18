package com.Harbinger.Spore.Core.entityStorages.clientSide;

import com.Harbinger.Spore.Core.entityStorages.SporeEntityGetter;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import net.minecraft.world.level.entity.*;

/**
 * @author karywoodOyo
 */
public final class SporeTransientEntitySectionManager<T extends EntityAccess> extends TransientEntitySectionManager<T> {
    public static final Class<? extends TransientEntitySectionManager<? extends EntityAccess>> transientEntitySectionManagerClass= (Class<? extends TransientEntitySectionManager<? extends EntityAccess>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeTransientEntitySectionManager.class,
            Class.class,
            LevelCallback.class
    );
    public SporeTransientEntitySectionManager(Class<T> p_157643_, LevelCallback<T> p_157644_) {
        super(p_157643_, p_157644_);
    }
    @Override
    public LevelEntityGetter<T> getEntityGetter() {
        if(this.entityGetter.getClass()!= SporeEntityGetter.entityGetterClass){
            this.entityGetter= SporeEntityGetter.newInstance(this.entityGetter,this.entityStorage,this.sectionStorage);
        }
        return this.entityGetter;
    }

    @Override
    public void addEntity(T p_157654_) {
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(p_157654_)) {
            return;
        }
        super.addEntity(p_157654_);
    }
}



