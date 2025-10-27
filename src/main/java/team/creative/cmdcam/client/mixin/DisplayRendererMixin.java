package team.creative.cmdcam.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import team.creative.cmdcam.client.mixin.DisplayAccessor;

@Mixin(DisplayRenderer.class)
public class DisplayRendererMixin {

    // Very scuffed setup to re-add back the text/block/item displays without changing any core logic.
    @Redirect(
            method = "render(Lnet/minecraft/world/entity/Display;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V")
    )
    private void cmdcam$replaceBillboard(PoseStack pose, Quaternionf ignored,
                                         Display d, float entityYaw, float pt, PoseStack poseParam, MultiBufferSource mb, int light) {
        if (!(d instanceof TextDisplay || d instanceof ItemDisplay || d instanceof BlockDisplay)) {
            pose.mulPose(ignored);
            return;
        }

        BillboardConstraints bb = ((DisplayAccessor) d).cmdcam$getBillboard();
        if (bb == BillboardConstraints.FIXED) {
            pose.mulPose(ignored);
            return;
        }

        Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();

        Vec3 camPos  = cam.getPosition();
        Vec3 dispPos = d.getPosition(pt);

        double dx = camPos.x - dispPos.x;
        double dy = camPos.y - dispPos.y;
        double dz = camPos.z - dispPos.z;

        double horiz = Math.sqrt(dx * dx + dz * dz);

        // yaw = atan2(dz, dx) - PI/2; pitch = atan2(dy, horiz)

        float yawRad   = (float) (Math.atan2(dz, dx) - Math.PI / 2.0);
        float pitchRad = (float) Math.atan2(dy, horiz);

        Quaternionf q;
        switch (bb) {
            case CENTER:
                q = new Quaternionf()
                        .rotationY(-yawRad)
                        .rotateX(-pitchRad);
                break;
            case VERTICAL:
                q = new Quaternionf().rotationY(-yawRad);
                break;
            case HORIZONTAL:
                q = new Quaternionf().rotationX(-pitchRad);
                break;
            default:
                pose.mulPose(ignored);
                return;
        }
        pose.mulPose(q);
    }
}
