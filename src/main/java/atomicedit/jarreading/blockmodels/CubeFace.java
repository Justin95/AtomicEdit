
package atomicedit.jarreading.blockmodels;

import atomicedit.frontend.render.blockmodelcreation.ChunkSectionPlus;
import org.joml.Vector3i;

/**
 *
 * @author Justin Bonner
 */
public enum CubeFace {
    UP(
        "up",
        new Vector3i(0, 1, 0), //up is y + 1
        new int[]{
            0, 1, 0,
            1, 1, 0,
            1, 1, 1,
            0, 1, 1
        },
        new int[]{ //indicies
            0, 2, 1,   0, 3, 2
        }
    ),
    DOWN(
        "down",
        new Vector3i(0, -1, 0), //down is y - 1
        new int[]{
            0, 0, 0,
            1, 0, 0,
            1, 0, 1,
            0, 0, 1
        },
        new int[]{ //indicies
            0, 1, 2,   0, 2, 3
        }
    ),
    NORTH(
        "north",
        new Vector3i(0, 0, -1), //north is z - 1,
        new int[]{
            0, 0, 0,
            1, 0, 0,
            1, 1, 0,
            0, 1, 0
        },
        new int[]{ //indicies
            0, 2, 1,   0, 3, 2
        }
    ),
    SOUTH(
        "south",
        new Vector3i(0, 0, 1), //south is z + 1,
        new int[]{
            0, 0, 1,
            1, 0, 1,
            1, 1, 1,
            0, 1, 1
        },
        new int[]{ //indicies
            0, 1, 2,   0, 2, 3
        }
    ),
    EAST(
        "east",
        new Vector3i(1, 0, 0), //east is x + 1,
        new int[]{
            1, 0, 0,
            1, 0, 1,
            1, 1, 1,
            1, 1, 0
        },
        new int[]{ //indicies
            0, 2, 1,   0, 3, 2
        }
    ),
    WEST(
        "west",
        new Vector3i(-1, 0, 0), //west is x - 1,
        new int[]{
            0, 0, 0,
            0, 0, 1,
            0, 1, 1,
            0, 1, 0
        },
        new int[]{ //indicies
            0, 1, 2,   0, 2, 3
        }
    ),
    ;
    final String faceName;
    final Vector3i adjTrans;
    /**
     * Specify additions to a set of base coordinates to get the corners of this face.
     * The coordinate additions are specified x,y,z around the face following the right hand rule.
     * The coordinates are listed counter clockwise if looking at the face.
     * There are always 4 vertices listed with 3 points each for an array length of 12.
     */
    public final int[] coordAdditions;
    public final int[] indicies;

    CubeFace(String faceName, Vector3i adjTrans, int[] coordAdditions, int[] indicies){
        this.faceName = faceName;
        this.adjTrans = adjTrans;
        this.coordAdditions = coordAdditions;
        this.indicies = indicies;
    }

    public short getAdjTotalLight(int x, int y, int z, ChunkSectionPlus section){
        return section.getTotalLightAt(x + adjTrans.x, y + adjTrans.y, z + adjTrans.z);
    }

    public boolean shouldDrawFace(ChunkSectionPlus section, short blockId, int x, int y, int z){
        return section.getBlockAt(x + adjTrans.x, y + adjTrans.y, z + adjTrans.z) != blockId; //check is adjacent block is the same
    }

}
