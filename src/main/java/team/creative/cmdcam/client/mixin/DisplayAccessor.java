package team.creative.cmdcam.client.mixin;

import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayAccessor {
    @Invoker("getBillboardConstraints")
    // @Invoker("method_48864")
    Display.BillboardConstraints cmdcam$getBillboard();

}
