package lotr.common.world.village;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.ChunkCoordIntPair;

public class LOTRVillagePositionCache {
    private Map<ChunkCoordIntPair, Result> cacheMap = new HashMap<ChunkCoordIntPair, Result>();
    private static final int MAX_SIZE = 20000;

    public void markResult(int chunkX, int chunkZ, boolean flag) {
        if(this.cacheMap.size() >= 20000) {
            this.clearCache();
        }
        this.cacheMap.put(this.getChunkKey(chunkX, chunkZ), flag ? Result.TRUE : Result.FALSE);
    }

    public boolean isVillageAt(int chunkX, int chunkZ) {
        return this.cacheMap.get(this.getChunkKey(chunkX, chunkZ)) == Result.TRUE;
    }

    public boolean isVillageNotAt(int chunkX, int chunkZ) {
        return this.cacheMap.get(this.getChunkKey(chunkX, chunkZ)) == Result.FALSE;
    }

    private ChunkCoordIntPair getChunkKey(int chunkX, int chunkZ) {
        return new ChunkCoordIntPair(chunkX, chunkZ);
    }

    public void clearCache() {
        this.cacheMap.clear();
    }

    private static enum Result {
        NONE, TRUE, FALSE;

    }

}
