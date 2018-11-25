
package atomicedit.frontend.render.shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 *
 * @author Justin Bonner
 */
public class DataBufferLayoutFormat {
    
    public static final int NUM_ELEMENTS_PER_VERTEX = BufferElements.sumElements();
    
    public static void defineBufferLayout(){
        int offset = 0;
        for(BufferElements element : BufferElements.values()){
            GL20.glEnableVertexAttribArray(element.LOCATION);
            GL20.glVertexAttribPointer(element.LOCATION, element.NUM_PRIMITIVES, element.PRIMITIVE_TYPE.GL_NAME, false, BufferElements.STRIDE, offset);
            offset += element.NUM_PRIMITIVES * element.PRIMITIVE_TYPE.SIZE_IN_BYTES;
        }
    }
    
    
    private static enum BufferElements{
        POSITION        (0, 3, DataType.FLOAT),
        TEXTURE_COORDS  (1, 2, DataType.FLOAT),
        COLOR           (2, 4, DataType.FLOAT),
        ;
        
        public final int LOCATION;
        public final int NUM_PRIMITIVES;
        public final DataType PRIMITIVE_TYPE;
        public static final int STRIDE; //num bytes for one vertex
        static{
            int total = 0;
            for(BufferElements element : BufferElements.values()){
                total += element.NUM_PRIMITIVES * element.PRIMITIVE_TYPE.SIZE_IN_BYTES;
            }
            STRIDE = total;
        }
        
        BufferElements(int location, int numPrimitives, DataType primitiveType){
            this.LOCATION = location;
            this.NUM_PRIMITIVES = numPrimitives;
            this.PRIMITIVE_TYPE = primitiveType;
        }
        
        static int sumElements(){
            int numElements = 0;
            for(BufferElements element : BufferElements.values()){
                numElements += element.NUM_PRIMITIVES;
            }
            return numElements;
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
    
    
}
