
package atomicedit.frontend.render.shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 *
 * @author Justin Bonner
 */
public enum DataBufferLayoutFormat {
    DEFAULT_DATA_BUFFER_LAYOUT(
        new BufferElement[]{
            new BufferElement(0, 3, DataType.FLOAT), //position
            new BufferElement(1, 2, DataType.FLOAT), //tex coords
            new BufferElement(2, 4, DataType.FLOAT)  //color
        }
    ),
    NO_TEXTURE_DATA_BUFFER_LAYOUT(
        new BufferElement[]{
            new BufferElement(0, 3, DataType.FLOAT), //position
            new BufferElement(1, 4, DataType.FLOAT)  //color
        }
    ),
    ONLY_POSITION_DATA_BUFFER_LAYOUT(
        new BufferElement[]{
            new BufferElement(0, 3, DataType.FLOAT) //position
        }
    ),
    BLOCK_DATA_BUFFER_LAYOUT(
        new BufferElement[]{
            new BufferElement(0, 3, DataType.FLOAT), //position
            new BufferElement(1, 2, DataType.FLOAT), //tex coords
            new BufferElement(2, 3, DataType.FLOAT)  //color (no alpha)
        }
    ),
    ;
    
    private final BufferElement[] bufferElements;
    private final int stride; //num bytes for one vertex
    public final int NUM_ELEMENTS_PER_VERTEX;
    
    DataBufferLayoutFormat(BufferElement[] bufferElements){
        this.bufferElements = bufferElements;
        int total = 0;
        for(BufferElement element : bufferElements){
            total += element.NUM_PRIMITIVES * element.PRIMITIVE_TYPE.SIZE_IN_BYTES;
        }
        this.stride = total;
        int numElements = 0;
        for(BufferElement element : this.bufferElements){
            numElements += element.NUM_PRIMITIVES;
        }
        this.NUM_ELEMENTS_PER_VERTEX = numElements;
    }
    
    
    
    public void defineBufferLayout(){
        int offset = 0;
        for(BufferElement element : this.bufferElements){
            GL20.glEnableVertexAttribArray(element.LOCATION);
            GL20.glVertexAttribPointer(element.LOCATION, element.NUM_PRIMITIVES, element.PRIMITIVE_TYPE.GL_NAME, false, this.stride, offset);
            offset += element.NUM_PRIMITIVES * element.PRIMITIVE_TYPE.SIZE_IN_BYTES;
        }
    }
    
    
    private static class BufferElement{
        
        public final int LOCATION;
        public final int NUM_PRIMITIVES;
        public final DataType PRIMITIVE_TYPE;
        
        BufferElement(int location, int numPrimitives, DataType primitiveType){
            this.LOCATION = location;
            this.NUM_PRIMITIVES = numPrimitives;
            this.PRIMITIVE_TYPE = primitiveType;
        }
        
    }
    
    private static enum DataType{
        SHORT(2, GL11.GL_SHORT),
        FLOAT(4, GL11.GL_FLOAT),
        ;
        public final int SIZE_IN_BYTES;
        public final int GL_NAME;

        DataType(int sizeInBytes, int glName){
            this.SIZE_IN_BYTES = sizeInBytes;
            this.GL_NAME = glName;
        }
    }
}
