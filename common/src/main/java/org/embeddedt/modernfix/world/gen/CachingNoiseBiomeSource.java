package org.embeddedt.modernfix.world.gen;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

public class CachingNoiseBiomeSource implements BiomeManager.NoiseBiomeSource {
    private static final boolean DEBUG_OOB = false;
    private static final int HORIZONTAL_CELLS_PER_CHUNK = 8;
    private static final int CELL_SHIFT = 2;
    private final BiomeManager.NoiseBiomeSource biomeSource;
    private final Holder<Biome>[] biomesInCells;
    private final int numVerticalCells;
    private final int verticalShift;
    private final int chunkOriginX, chunkOriginZ;

    public CachingNoiseBiomeSource(BiomeManager.NoiseBiomeSource backingSource, int chunkOriginX, int chunkOriginZ, int worldHeight, int numBlocksBelowYZero) {
        this.biomeSource = backingSource;
        this.numVerticalCells = worldHeight / QuartPos.SIZE + 2;
        this.chunkOriginX = chunkOriginX;
        this.chunkOriginZ = chunkOriginZ;
        this.verticalShift = numBlocksBelowYZero / QuartPos.SIZE;
        this.biomesInCells = new Holder[numVerticalCells * HORIZONTAL_CELLS_PER_CHUNK * HORIZONTAL_CELLS_PER_CHUNK];
    }

    private static int getLocalIndex(int relX, int relY, int relZ) {
        return (relY * HORIZONTAL_CELLS_PER_CHUNK * HORIZONTAL_CELLS_PER_CHUNK) + (relZ * HORIZONTAL_CELLS_PER_CHUNK) + relX;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z) {
        int relX = x + CELL_SHIFT - this.chunkOriginX;
        int relZ = z + CELL_SHIFT - this.chunkOriginZ;
        int relY = y + this.verticalShift + 1;
        if (((relX | relZ) & ~(HORIZONTAL_CELLS_PER_CHUNK - 1)) != 0 || relY < 0 || relY >= this.numVerticalCells) {
            return this.biomeSource.getNoiseBiome(x, y, z);
        }
        int i = getLocalIndex(relX, relY, relZ);
        if (DEBUG_OOB) {
            if (i < 0 || i >= this.biomesInCells.length) {
                throw new ArrayIndexOutOfBoundsException("Error: Requested noise biome cell is out of range: (%d, %d, %d)".formatted(relX, relY, relZ));
            }
        }
        var biomes = this.biomesInCells;
        var result = biomes[i];
        if (result == null) {
            result = this.biomeSource.getNoiseBiome(x, y, z);
            biomes[i] = result;
        }
        return result;
    }
}
