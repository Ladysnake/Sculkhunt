package ladysnake.sculkhunt.cca;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

public class SculkComponent implements AutoSyncedComponent {
    private final LivingEntity obj;
    private boolean isSculk = false;
    private int detectedTime = 0;

    public SculkComponent(LivingEntity obj) {
        this.obj = obj;
    }

    public boolean isSculk() {
        return this.isSculk;
    }

    public void setSculk(boolean setSculk) {
        this.isSculk = setSculk;
        SculkhuntComponents.SCULK.sync(obj);
    }

    public int detectedTime() {
        return this.detectedTime;
    }

    public boolean isDetected() {
        return this.detectedTime > 0;
    }

    public void setDetectedTime(int detectedTime) {
        this.detectedTime = detectedTime;
        SculkhuntComponents.SCULK.sync(obj);
    }

    public void decrementDetectedTime() {
        this.detectedTime--;
    }

    public void readFromNbt(NbtCompound nbtCompound) {
        this.isSculk = nbtCompound.getBoolean("isSculk");
        this.detectedTime = nbtCompound.getInt("detectedTime");
    }

    public void writeToNbt(NbtCompound nbtCompound) {
        nbtCompound.putBoolean("isSculk", this.isSculk);
        nbtCompound.putInt("detectedTime", this.detectedTime);
    }
}