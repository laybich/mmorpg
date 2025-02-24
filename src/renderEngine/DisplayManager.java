package renderEngine;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

public class DisplayManager {

   private static final int WIDTH = 1360;
   private static final int HEIGHT = 768;
   private static final int FPS_CAP = 60;

   private static long lastFrameTime;
   private static float delta;

   public static void createDisplay(){

      DisplayMode displayMode = null;
      try {
         DisplayMode[] modes = Display.getAvailableDisplayModes();

         for (int i = 0; i < modes.length; i++)
         {
            if (modes[i].getWidth() == WIDTH
                  && modes[i].getHeight() == HEIGHT
                  && modes[i].isFullscreenCapable())
            {
               displayMode = modes[i];
            }
         }
      } catch (Exception e) {
         System.exit(-1);
         e.printStackTrace();
      }

      ContextAttribs attribs = new ContextAttribs(3,3)
            .withForwardCompatible(true)
            .withProfileCore(true);

      try {
         Display.setFullscreen(false);
         Display.setDisplayMode(displayMode);
         Display.create(new PixelFormat().withDepthBits(24), attribs);
         Display.setTitle("R3 Offline");
         GL11.glEnable(GL13.GL_MULTISAMPLE);
      } catch (LWJGLException e) {
         e.printStackTrace();
      }

      GL11.glViewport(0,0, WIDTH, HEIGHT);
      lastFrameTime = getCurrentTime();
   }

   public static void updateDisplay(){
      Display.sync(FPS_CAP);
      Display.update();
      long currentFrameTime = getCurrentTime();
      delta = (currentFrameTime - lastFrameTime)/1000f;
      lastFrameTime = currentFrameTime;
   }

   public static float getFrameTimeSeconds(){
      return delta;
   }

   public static void closeDisplay(){
      Display.destroy();
   }

   private static long getCurrentTime(){
      return Sys.getTime()*1000/Sys.getTimerResolution();
   }




}