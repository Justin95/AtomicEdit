
package atomicedit.frontend.render.shaders;

import atomicedit.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL20.*;

/**
 *
 * @author Justin Bonner
 */
public enum ShaderProgram {
    DEFAULT_SHADER_PROGRAM(
        "DefaultVertexShader",
        "DefaultFragmentShader"
    ),
    NO_TEXTURE_SHADER_PROGRAM(
        "NoTextureVertexShader",
        "NoTextureFragmentShader"
    ),
    ONLY_POSITION_SHADER_PROGRAM(
        "OnlyPositionVertexShader",
        "OnlyPositionFragmentShader"
    ),
    ;
    
    private final String vertexShaderName;
    private final String fragmentShaderName;
    
    ShaderProgram(String vertexShaderName, String fragmentShaderName){
        this.vertexShaderName = vertexShaderName;
        this.fragmentShaderName = fragmentShaderName;
    }
    
    private static final String FILEPATH_TO_SHADERS = "/shaders/";
    private static final String FILETYPE_OF_SHADERS = ".glsl";
    private static final Map<ShaderProgram, Integer> mapToProgramId = new EnumMap<>(ShaderProgram.class);
    
    
    
    public static int getShaderProgram(ShaderProgram program){
        if(mapToProgramId.containsKey(program)){
            return mapToProgramId.get(program);
        }
        int programId = glCreateProgram();
		int vertexShader   = createShader(program.vertexShaderName, GL_VERTEX_SHADER);
		int fragmentShader = createShader(program.fragmentShaderName, GL_FRAGMENT_SHADER);
		glAttachShader(programId, vertexShader);
		glAttachShader(programId, fragmentShader);
		glLinkProgram(programId);
		glValidateProgram(programId);
		if(!linkedCorrectly(programId)){
            return -1;
        }
        mapToProgramId.put(program, programId);
        return programId;
    }
    
	
	/**
	 * Checks for an error in linking the program.
	 */
	private static boolean linkedCorrectly(int programId){
		int status = glGetProgrami(programId, GL_LINK_STATUS);
		if(status != GL11.GL_TRUE){
            String message = ""
                + "LINKING ERROR -----------\n"
                + glGetProgramInfoLog(programId, 512) + "\n"
                + "-------------------------";
            Logger.error(message);
            return false;
		}
        return true;
	}
	
	
	
    
    private static int createShader(String name, int type){
        String filepath = FILEPATH_TO_SHADERS + name + FILETYPE_OF_SHADERS;
		String source = readFile(filepath);
		CharSequence charSeqSource = source.subSequence(0, source.length() - 1);
		int shaderId = glCreateShader(type);
		glShaderSource(shaderId, charSeqSource);
		glCompileShader(shaderId);
		if(!errorCompileCheck(shaderId, filepath)){
            return -1;
        }
        return shaderId;
	}
	
	/**
	 * 
	 * @return true if the shader compiled successfully, false otherwise
	 */
	private static boolean errorCompileCheck(int shaderId, String filepath){
		int status = glGetShaderi(shaderId, GL_COMPILE_STATUS);
		if(status != GL11.GL_TRUE){
            String message = ""
                + "------------------------------------------------\n"
                + "MAJOR ERROR: Shader compilation failure: " + filepath + "\n"
                + "------------------------------------------------\n"
                + glGetShaderInfoLog(shaderId, 512) + "\n"
                + "------------------------------------------------";
            Logger.error(message);
			return false;
		}
		return true;
	}
    
    private static String readFile(String filepath){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(ShaderProgram.class.getResourceAsStream(filepath)));
            StringBuilder shaderSource = new StringBuilder();
            Scanner fileScanner;
            fileScanner = new Scanner(reader);
            while(fileScanner.hasNextLine()){
                shaderSource.append(fileScanner.nextLine());
                shaderSource.append("\n");
            }
            fileScanner.close();
            String shaderSourceString = shaderSource.toString();
            return shaderSourceString;
        }catch(Exception e){
            Logger.error("Exception in reading shader: " + e);
            return "Exception: " + e;
        }
    }
    
}
