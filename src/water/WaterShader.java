package water;

import org.lwjgl.util.vector.Matrix4f;

import org.lwjgl.util.vector.Vector3f;
import shaders.ShaderProgram;
import toolbox.Maths;
import entities.Camera;
import entities.Light;

public class WaterShader extends ShaderProgram {

   private final static String VERTEX_FILE = "src/water/waterVertex.glsl";
   private final static String FRAGMENT_FILE = "src/water/waterFragment.glsl";

   private int location_modelMatrix;
   private int location_viewMatrix;
   private int location_projectionMatrix;
   private int location_reflectionTexture;
   private int location_refractionTexture;
   private int location_dudvMap;
   private int location_moveFactor;
   private int location_cameraPosition;
   private int location_normalMap;
   private int location_lightPosition;
   private int location_lightColour;
   private int location_depthMap;
   private int location_skyColour;

   public WaterShader() {
      super(VERTEX_FILE, FRAGMENT_FILE);
   }

   @Override
   protected void bindAttributes() {
      bindAttribute(0, "position");
   }

   @Override
   protected void getAllUniformLocations() {
      location_projectionMatrix = getUniformLocation("projectionMatrix");
      location_viewMatrix = getUniformLocation("viewMatrix");
      location_modelMatrix = getUniformLocation("modelMatrix");
      location_reflectionTexture = getUniformLocation("reflectionTexture");
      location_refractionTexture = getUniformLocation("refractionTexture");
      location_dudvMap = getUniformLocation("dudvMap");
      location_moveFactor = getUniformLocation("moveFactor");
      location_cameraPosition = getUniformLocation("cameraPosition");
      location_normalMap = getUniformLocation("normalMap");
      location_lightPosition = getUniformLocation("lightPosition");
      location_lightColour = getUniformLocation("lightColour");
      location_depthMap = getUniformLocation("depthMap");
      location_skyColour = getUniformLocation("skyColour");
   }

   public void connectTextureUnits(){
      super.loadInt(location_reflectionTexture, 0);
      super.loadInt(location_refractionTexture, 1);
      super.loadInt(location_dudvMap, 2);
      super.loadInt(location_normalMap, 3);
      super.loadInt(location_depthMap, 4);
   }

   public void loadSkyColour(float r, float g, float b){
      super.loadVector(location_skyColour, new Vector3f(r,g,b));
   }

   public void loadLight(Light light){
      super.loadVector(location_lightColour, light.getColour());
      super.loadVector(location_lightPosition, light.getPosition());
   }

   public void loadMoveFactor(float factor){
      super.loadFloat(location_moveFactor, factor);
   }

   public void loadProjectionMatrix(Matrix4f projection) {
      loadMatrix(location_projectionMatrix, projection);
   }

   public void loadViewMatrix(Camera camera){
      Matrix4f viewMatrix = Maths.createViewMatrix(camera);
      loadMatrix(location_viewMatrix, viewMatrix);
      super.loadVector(location_cameraPosition, camera.getPosition());
   }

   public void loadModelMatrix(Matrix4f modelMatrix){
      loadMatrix(location_modelMatrix, modelMatrix);
   }

}