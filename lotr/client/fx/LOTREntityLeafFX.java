package lotr.client.fx;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

public class LOTREntityLeafFX extends EntityFX {
    private int leafAge = 600;

    public LOTREntityLeafFX(World world, double d, double d1, double d2, double d3, double d4, double d5, int i) {
        super(world, d, d1, d2, d3, d4, d5);
        this.motionX = d3;
        this.motionY = d4;
        this.motionZ = d5;
        this.particleScale = 0.15f + this.rand.nextFloat() * 0.5f;
        this.setParticleTextureIndex(i);
    }

    public LOTREntityLeafFX(World world, double d, double d1, double d2, double d3, double d4, double d5, int i, int j) {
        this(world, d, d1, d2, d3, d4, d5, i);
        this.leafAge = j;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if(this.leafAge > 0) {
            --this.leafAge;
        }
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        if(this.onGround || this.leafAge == 0 || this.posY < 0.0) {
            this.setDead();
        }
    }

    @Override
    public void setParticleTextureIndex(int i) {
        this.particleTextureIndexX = i % 8;
        this.particleTextureIndexY = i / 8;
    }

    @Override
    public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5) {
        float f6 = this.particleTextureIndexX / 8.0f;
        float f7 = f6 + 0.125f;
        float f8 = this.particleTextureIndexY / 8.0f;
        float f9 = f8 + 0.125f;
        float f10 = 0.2f * this.particleScale;
        float f11 = (float) (this.prevPosX + (this.posX - this.prevPosX) * f - interpPosX);
        float f12 = (float) (this.prevPosY + (this.posY - this.prevPosY) * f - interpPosY);
        float f13 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * f - interpPosZ);
        tessellator.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
        tessellator.addVertexWithUV(f11 - f1 * f10 - f4 * f10, f12 - f2 * f10, f13 - f3 * f10 - f5 * f10, f7, f9);
        tessellator.addVertexWithUV(f11 - f1 * f10 + f4 * f10, f12 + f2 * f10, f13 - f3 * f10 + f5 * f10, f7, f8);
        tessellator.addVertexWithUV(f11 + f1 * f10 + f4 * f10, f12 + f2 * f10, f13 + f3 * f10 + f5 * f10, f6, f8);
        tessellator.addVertexWithUV(f11 + f1 * f10 - f4 * f10, f12 - f2 * f10, f13 + f3 * f10 - f5 * f10, f6, f9);
    }
}
