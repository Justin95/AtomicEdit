
package atomicedit.volumes;

import atomicedit.backend.BlockCoord;

/**
 *
 * @author Justin Bonner
 */
public class Box {
    
    private int xLength;
    private int yLength;
    private int zLength;
    /**
     * Define a Box with two block coordinates as corners.
     * Any opposite corners can be used to define this Box.
     * Both points are inside the box.
     * @param pointOne inclusive
     * @param pointTwo inclusive
     */
    public Box(BlockCoord pointOne, BlockCoord pointTwo){
        this.xLength = Math.abs(pointOne.x - pointTwo.x) + 1; //+1 for inclusive
        this.yLength = Math.abs(pointOne.y - pointTwo.y) + 1;
        this.zLength = Math.abs(pointOne.z - pointTwo.z) + 1;
        //this.smallestPoint = new BlockCoord(Math.min(pointOne.x, pointTwo.x), Math.min(pointOne.y, pointTwo.y), Math.min(pointOne.z, pointTwo.z));
        //this.largestPoint = new BlockCoord(Math.max(pointOne.x, pointTwo.x), Math.max(pointOne.y, pointTwo.y), Math.max(pointOne.z, pointTwo.z));
    }
    
    
    public int getXLength(){
        return xLength;
    }
    
    public int getYLength(){
        return yLength;
    }
    
    public int getZLength(){
        return zLength;
    }
    
    public int getNumBlocksContained(){
        return xLength * yLength * zLength;
    }
    
    /**
     * Tests if the given x, y, z are inside this box.
     * x, y, and z are in box relative coords. Meaning they start at 0, 0, 0 and go to xLength - 1, yLength - 1, and zLength - 1.
     * @param x
     * @param y
     * @param z
     * @return 
     */
    public boolean isInsideBox(int x, int y, int z){
        return x >= 0
            && x <  xLength
            && y >= 0
            && y <  yLength
            && z >= 0
            && z <  zLength;
    }
    
    /**
     * Create a box that is the intersection of this box and another box.
     * @param otherBox
     * @return 
     */
    /*public Box getIntersectingBox(Box otherBox){
        int smallX = Math.max(this.smallestPoint.x, otherBox.smallestPoint.x);
        int smallY = Math.max(this.smallestPoint.y, otherBox.smallestPoint.y);
        int smallZ = Math.max(this.smallestPoint.z, otherBox.smallestPoint.z);
        
        int largeX = Math.min(this.largestPoint.x, otherBox.largestPoint.x);
        int largeY = Math.min(this.largestPoint.y, otherBox.largestPoint.y);
        int largeZ = Math.min(this.largestPoint.z, otherBox.largestPoint.z);
        
        if(smallX > largeX || smallY > largeY || smallZ > largeZ){
            return null; //do not intersect
        }
        
        return new Box(new BlockCoord(smallX, smallY, smallZ), new BlockCoord(largeX, largeY, largeZ));
    }*/
    
    /**
     * Performs the given action for each x, y, z in this box, starting at 0, 0, 0.
     * Iterates in YZX order.
     * @param action 
     */
    public void doForXyz(ActionForXYZ action){
        for(int y = 0; y < yLength; y++){
            for(int z = 0; z < zLength; z++){
                for(int x = 0; x < xLength; x++){
                    action.action(x, y, z);
                }
            }
        }
    }
    
    
}
