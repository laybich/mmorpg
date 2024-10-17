package engineTester;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import audio.AudioMaster;
import audio.Source;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import postProcessing.Fbo;
import postProcessing.PostProcessing;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrain.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

import javax.imageio.ImageIO;

public class MainGameLoop {

   public static Vector2f getMapPosition(Player player,Terrain terrain) {

      float posx = 0.6985f + ((0.2765f * player.getPosition().x )/ terrain.getSize());
      float posy = 0.483f + ((0.472f * -1 * player.getPosition().z)/ terrain.getSize());

      return new Vector2f(posx, posy);
   }

   public static void main(String[] args) {

      int border_Adjustment = 100;

      DisplayManager.createDisplay();
      Loader loader = new Loader();

      //**********************AUDIO***************************

      AudioMaster.init();
      AudioMaster.setListenerData(0, 0, 0);
      AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);
      int menuMusic = 0;
      int gameMusic1 = 0;

      try {
         menuMusic = AudioMaster.loadSound(new File("src/audio/mainMenuMusic.wav"));
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }

      try {
         gameMusic1 = AudioMaster.loadSound(new File("src/audio/gameMusic1.wav"));
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }

      Source source = new Source();
      source.setLooping(true);

      source.play(menuMusic);

      /*
		 Just add each of the model in below models......
		 And also add it to anim_frames list ........................
		*/

      List<TexturedModel> anim_frames = new ArrayList<TexturedModel>();
      String player_texture =  "models/trex/trex";
      TexturedModel trex_standing = new TexturedModel(OBJLoader.loadObjModel("models/trex/trex_standing", loader), new ModelTexture(loader.loadTexture(player_texture)));
      for(int i = 1; i < 65; i++) {
         if(i < 10) {
            anim_frames.add(new TexturedModel(OBJLoader.loadObjModel("models/trex/trex_00000" + i, loader), new ModelTexture(loader.loadTexture(player_texture))));
         }
         else if(i < 100){
            anim_frames.add(new TexturedModel(OBJLoader.loadObjModel("models/trex/trex_0000" + i, loader), new ModelTexture(loader.loadTexture(player_texture))));
         }
         else {
            anim_frames.add(new TexturedModel(OBJLoader.loadObjModel("models/trex/trex_000" + i, loader), new ModelTexture(loader.loadTexture(player_texture))));
         }
      }
      Player player = new Player(trex_standing, anim_frames, new Vector3f(50, 0, -50),0 ,180,0,1.5f);
      Camera camera = new Camera(player);

      //Text stuff on screen.............................
      TextMaster.init(loader);
      FontType font = new FontType(loader.loadTexture("candara"), new File("res/candara.fnt"));
      GUIText text = new GUIText("R3 offline", 2, font, new Vector2f(0,0), 1, true);
      text.setColour(1, 1, 0);
      GUIText text3 = new GUIText("", 3.5f, font, new Vector2f(0,0.2f), 1, true);
      text3.setColour(0, 0.2f, 1);
      GUIText text4 = new GUIText("", 2, font, new Vector2f(0,0.45f), 1, true);
      text4.setColour(1, 0.80f, 0);
      GUIText text5 = new GUIText("", 2, font, new Vector2f(0,0.55f), 1, true);
      text5.setColour(1, 0.80f, 0);
      FontType font1 = new FontType(loader.loadTexture("fonts/sans"), new File("res/fonts/sans.fnt"));
      GUIText text1 = new GUIText("100", 2, font, new Vector2f(-0.425f,0.935f), 1, true);
      text1.setColour(1, 0, 0);

      //This part creates 4 objects of the Terrain texture class that stores and return the id for textures..........
      // We pass loader.loadTexture which loads the texture in the opengl.....................
      TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("TerrainTextures/grass2"));                 // For grass texture........................
      TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("TerrainTextures/dryGrass"));                          // For dirt texture.....................
      TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("TerrainTextures/sand"));                            // For mud texture.......................
      TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("TerrainTextures/path1"));                         // For path/road texture...............


      //Object of the Texture pack to assign the 4 textures we have to the terrain of our game..........................................
      TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);

      //Now we load our blender map in the opengl...................................................
      TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("map/blendMap3"));

      //Create a object of rawmodel to load our tree  model in opengl and returns the id of the model..........
      RawModel model = OBJLoader.loadObjModel("trees/pine", loader);
      TexturedModel staticModel = new TexturedModel(model,new ModelTexture(loader.loadTexture("trees/pine")));
      TexturedModel grass = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader),new ModelTexture(loader.loadTexture("grassTexture")));
      grass.getTexture().setHasTransparency(true);
      grass.getTexture().setUseFakeLighting(true);
      ModelTexture fernt = new ModelTexture(loader.loadTexture("fern1"));
      fernt.setNumberOfRows(2);
      TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel("fern", loader),fernt);
      fern.getTexture().setHasTransparency(true);
      TexturedModel lowPolyTree = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader),new ModelTexture(loader.loadTexture("flower")));
      lowPolyTree.getTexture().setHasTransparency(true);
      TexturedModel cherryModel = new TexturedModel(OBJLoader.loadObjModel("cherry", loader),new ModelTexture(loader.loadTexture("cherry")));
      cherryModel.getTexture().setHasTransparency(true);
      cherryModel.getTexture().setShineDamper(10);
      cherryModel.getTexture().setReflectivity(0.5f);
      cherryModel.getTexture().setExtraInfoMap(loader.loadTexture("cherryS"));
      TexturedModel lantern = new TexturedModel(OBJLoader.loadObjModel("lantern", loader),
            new ModelTexture(loader.loadTexture("lantern")));
      lantern.getTexture().setExtraInfoMap(loader.loadTexture("lanternS"));

      Terrain terrain = new Terrain(0,-1,loader,texturePack,blendMap, "heightmap perlin");
      List<Terrain> terrains = new ArrayList<>();
      terrains.add(terrain);

      List<Entity> entities = new ArrayList<Entity>();               // This creates the list of entities(objects) in our game.........................
      List<Entity> collisionEntities = new ArrayList<Entity>();               // This creates the list of entities(objects) in our game.........................
      List<Entity> collisionEntities1 = new ArrayList<Entity>();

      Random random = new Random();  //Creating the object of the random class to give us random terrain objects in our terrain........................

      BufferedImage blendImage = null;
      String blendMapImage = "map/blendMap3edit";
      try {
         blendImage = ImageIO.read(new File("res/" + blendMapImage  + ".png"));                         //load height map.........................
      } catch (IOException e) {
         e.printStackTrace();
      }
      int blendImageHeight = blendImage.getHeight();

      //terrain entities like fern, plants , grass, shrubs and cherry blossom
      int adjustterrain = 60;

      entities.add(new Entity(fern, random.nextInt(4), new Vector3f(0,0,0),0,0,0,0.4f));

      MasterRenderer renderer = new MasterRenderer(loader, camera);

      WaterFrameBuffers buffers = new WaterFrameBuffers();
      WaterShader waterShader = new WaterShader();
      WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
      List<WaterTile> waters = new ArrayList<WaterTile>();
      for (int i = 0; i < 6; i++) {
         for (int j = 0; j < 6; j++) {
            waters.add(new WaterTile(i * 160, -j * 160, -2.5f));
         }
      }

      for (int i = 0; i < 900; i++) {
         int x = (int) (random.nextFloat()*(terrain.getSize() - border_Adjustment - adjustterrain) + border_Adjustment);// - 400;
         int z = (int) (random.nextFloat() * -(terrain.getSize() - border_Adjustment - adjustterrain) - border_Adjustment);
         float y = terrain.getHeightOfTerrain(x, z);

         int xb = (int) (x * (blendImageHeight/ terrain.getSize()));
         int zb =  blendImageHeight - (-1 * ((int) ( (z )* (blendImageHeight / terrain.getSize()))));

         Color blendPixelColor = new Color(blendImage.getRGB(xb, zb));

         if(blendPixelColor.getBlue() < 1 && y > waters.get(0).getHeight()) {
            float scaleTree =  Math.max(random.nextFloat(), 0.3f);
            Entity tree1 = new Entity(staticModel, new Vector3f(x, y, z),0,0,0,2 * scaleTree);
            entities.add(tree1);
            collisionEntities1.add(tree1);
         }

         x = (int) (random.nextFloat()*(terrain.getSize()  - border_Adjustment- adjustterrain) + border_Adjustment);
         z = (int) (random.nextFloat() * -(terrain.getSize() - border_Adjustment- adjustterrain) - border_Adjustment);
         y = terrain.getHeightOfTerrain(x, z);
         xb = (int) (x * (blendImageHeight/ terrain.getSize()));
         zb = blendImageHeight -( -1 * ((int) ( (z )* (blendImageHeight / terrain.getSize()))));
         Color blendPixelColor5 = new Color(blendImage.getRGB(xb, zb));
         if(blendPixelColor5.getBlue() < 1 && y > waters.get(0).getHeight()) {
            entities.add(new Entity(grass, new Vector3f(x, y, z),0,0,0,4f));
         }

         x = (int) (random.nextFloat()*(terrain.getSize() - border_Adjustment- adjustterrain) + border_Adjustment) ;
         z = (int) (random.nextFloat() * -(terrain.getSize() - border_Adjustment- adjustterrain) - border_Adjustment);
         y = terrain.getHeightOfTerrain(x, z);

         xb = (int) (x * (blendImageHeight/ terrain.getSize()));
         zb = blendImageHeight -( -1 * ((int) ( (z )* (blendImageHeight / terrain.getSize()))));
         Color blendPixelColor1 = new Color(blendImage.getRGB(xb, zb));

         if(blendPixelColor1.getBlue() < 1 && y > waters.get(0).getHeight()) {
            entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x,y,z),0,0,0,0.8f));
         }

         x = (int) (random.nextFloat()*(terrain.getSize() - border_Adjustment- adjustterrain) + border_Adjustment);
         z = (int) (random.nextFloat() * -(terrain.getSize() - border_Adjustment- adjustterrain) - border_Adjustment);
         y = terrain.getHeightOfTerrain(x, z);
         xb = (int) (x * (blendImageHeight/ terrain.getSize()));
         zb = blendImageHeight -( -1 * ((int) ( (z )* (blendImageHeight / terrain.getSize()))));
         Color blendPixelColor2 = new Color(blendImage.getRGB(xb, zb));
         if(blendPixelColor2.getBlue() < 1 && y > waters.get(0).getHeight()) {
            entities.add(new Entity(lowPolyTree, new Vector3f(x,y,z),0,0,0,1));
         }

         if(i % 11 == 0) {
            x = (int) (random.nextFloat()*(terrain.getSize() - border_Adjustment - adjustterrain) + border_Adjustment);
            z = (int) (random.nextFloat() * -(terrain.getSize() - border_Adjustment - adjustterrain) - border_Adjustment);
            y = terrain.getHeightOfTerrain(x, z);

            xb = (int) (x * (blendImageHeight/ terrain.getSize()));
            zb = blendImageHeight -( -1 * ((int) ( (z )* (blendImageHeight / terrain.getSize()))));
            Color blendPixelColor3 = new Color(blendImage.getRGB(xb, zb));

            if(blendPixelColor3.getBlue() < 1 && y > waters.get(0).getHeight()) {
               Entity lpt = new Entity(cherryModel, new Vector3f(x,y,z),0,0,0,3.6f);
               entities.add(lpt);
               collisionEntities.add(lpt);
            }
         }
      }

      TexturedModel house = new TexturedModel(OBJLoader.loadObjModel("WoodenCabinObj", loader),
            new ModelTexture(loader.loadTexture("WoodCabinDif")));

      ModelTexture t_lamp = new ModelTexture(loader.loadTexture("lamp"));
      t_lamp.setUseFakeLighting(true);
      TexturedModel lamp = new TexturedModel(OBJLoader.loadObjModel("lamp", loader), t_lamp);

      List<Light> lights = new ArrayList<Light>();
      Light sun = new Light(new Vector3f(0, 10000, -10000), new Vector3f(1.3f, 1.3f, 1.3f));
      lights.add(sun);
      lights.add(new Light(new Vector3f(185,10,-293), new Vector3f(2,0,0), new Vector3f(1, 0.01f, 0.0002f)));
      lights.add(new Light(new Vector3f(370,17,-300), new Vector3f(0,0,2), new Vector3f(1, 0.01f, 0.0002f)));
      lights.add(new Light(new Vector3f(293,7,-305), new Vector3f(0,2,0), new Vector3f(1, 0.01f, 0.0002f)));

      entities.add(new Entity(lantern, new Vector3f(180, terrain.getHeightOfTerrain(180, -293), -293), 0,0,0,1));
      entities.add(new Entity(lantern, new Vector3f(370, terrain.getHeightOfTerrain(370, -293), -293), 0,0,0,1));
      entities.add(new Entity(lantern, new Vector3f(293, terrain.getHeightOfTerrain(293, -293), -300), 0,0,0,1));
      entities.add(new Entity(house, new Vector3f(30, -2, -30), 0,180,0,0.4f));

      List<Entity> normalMapEntities = new ArrayList<>();

      TexturedModel barrel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader),
            new ModelTexture(loader.loadTexture("barrel")));
      barrel.getTexture().setNormalMap(loader.loadTexture("barrelNormal"));
      barrel.getTexture().setShineDamper(10);
      barrel.getTexture().setReflectivity(0.5f);
      barrel.getTexture().setExtraInfoMap(loader.loadTexture("barrelS"));
      //normalMapEntities.add(new Entity(barrel, new Vector3f(100, 70, 100), 0, 0, 0, 10));

      float h;
      for(int i = 0; i < blendImageHeight; i++) {
         for(int j = 0; j < blendImageHeight; j++) {
            Color blendPixelColorlamp = new Color(blendImage.getRGB(i, j));

            if(blendPixelColorlamp.getGreen() > 150 && blendPixelColorlamp.getRed() > 150 && blendPixelColorlamp.getBlue() > 150
               && waters.get(0).getHeight() < terrain.getHeightOfTerrain(i / 2, (j - blendImageHeight)/2)) {

               h = terrain.getHeightOfTerrain(i / 2, (j - blendImageHeight)/2);
               float scaleTree1 =  Math.max(random.nextFloat(), 0.3f);
               Entity tree12 = new Entity(staticModel, new Vector3f(i/2,h, (j - blendImageHeight)/2),0,0,0,2 *scaleTree1);
               entities.add(tree12);
               collisionEntities1.add(tree12);
            }
            else if(blendPixelColorlamp.getGreen() > 100
                  && waters.get(0).getHeight() < terrain.getHeightOfTerrain(i / 2, (j - blendImageHeight)/2)) {
               h = terrain.getHeightOfTerrain(i/2, (j - blendImageHeight)/2);
               entities.add(new Entity(lamp, new Vector3f(i/2, h, (j - blendImageHeight)/2), 0,0,0,1));
            }
         }
      }

      //coin
      ModelTexture coin1m = new ModelTexture(loader.loadTexture("cube"));
      coin1m.setShineDamper(1.5f);
      coin1m.setReflectivity(0.6f);
      coin1m.setUseFakeLighting(true);
      TexturedModel coin1 = new TexturedModel(OBJLoader.loadObjModel("models/coin1", loader), coin1m);
      coin1.getTexture().setHasTransparency(true);

      Entity coin1e  = new Entity(coin1, new Vector3f(60, 3, -60), 0,0,0,20);
      entities.add(coin1e);

      //fence
      ModelTexture fenceT = new ModelTexture(loader.loadTexture("models/fence"));
      fenceT.isUseFakeLighting();
      TexturedModel fence = new TexturedModel(OBJLoader.loadObjModel("models/fence1", loader),fenceT);
      fence.getTexture().setHasTransparency(true);

      for(int i = 1; i < 102; i++) {
         entities.add(new Entity(fence, new Vector3f(-100, -3, -21 * i + 100), 0,-90,0,0.05f));
      }
      for(int i = 0; i < 100; i++) {
		   entities.add(new Entity(fence, new Vector3f(21 * i - 100, -3, 100), 0,0,0,0.05f));
      }

      TexturedModel boat = new TexturedModel(OBJLoader.loadObjModel("models/boat", loader),new ModelTexture(loader.loadTexture("models/wood")));
      Entity e_boat = new Entity(boat, new Vector3f(-50, 0, 0),0 ,90,0,800);

      entities.add(e_boat);
      entities.add(player);

      ParticleMaster.init(loader,renderer.getProjectionMatrix());

      List<Entity> shadowEntities = new ArrayList<>();
      shadowEntities.addAll(entities);
      shadowEntities.addAll(normalMapEntities);
      MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);

      //GUIs

      List<GuiTexture> guis = new ArrayList<GuiTexture>();
      GuiTexture gui = new GuiTexture(loader.loadTexture("map/game_map2"), new Vector2f(0.86f, 0.63f), new Vector2f(0.175f, 0.30f));
      GuiTexture gui1 = new GuiTexture(loader.loadTexture("map/pointer"), new Vector2f(0.71f, 0.51f), new Vector2f(0.02f, 0.04f));
      GuiTexture gui2 = new GuiTexture(loader.loadTexture("map/life"), new Vector2f(-0.94f, -0.92f), new Vector2f(0.05f, 0.08f));
      GuiTexture gui4 = new GuiTexture(loader.loadTexture("map/sword"), new Vector2f(-0.94f, -0.75f), new Vector2f(0.05f, 0.08f));

      List<GuiTexture> guisMenu = new ArrayList<GuiTexture>();
      GuiTexture gui3 = new GuiTexture(loader.loadTexture("map/background6"), new Vector2f(-0.00f, -0.60f), new Vector2f(0.9f, 1.6f));

      guisMenu.add(gui3);

      guis.add(gui4);
      guis.add(gui2);
      guis.add(gui);
      guis.add(gui1);
      GuiRenderer guiRenderer = new GuiRenderer(loader);

      ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture("cosmic"), 4, true);
      ParticleSystem system = new ParticleSystem(particleTexture ,150, 50, 0.15f, 4,2 );

      Fbo multisampleFbo = new Fbo(Display.getWidth(), Display.getHeight());
      Fbo outputFbo = new Fbo(Display.getWidth(), Display.getHeight(), Fbo.DEPTH_TEXTURE);
      PostProcessing.init(loader);

      /// Main game loop which is running during the game....................................
      byte flag999 = 0;
      int chance= 0, i1 = 0;
      int life = 100;
      boolean startFlag = false;
      boolean gameOver = false;
      boolean quitGame = false;

      int mouseX;
      int mouseY;

      while (!startFlag && !quitGame) {
         guiRenderer.render(guisMenu);
         mouseX = Mouse.getX();
         mouseY = Mouse.getY();

         if(((mouseX>292 && mouseX<374)&&(mouseY > 543 && mouseY < 579)) && Mouse.isButtonDown(0)) {
            startFlag = true;
         }
         if(((mouseX > 301 && mouseX < 444)&&(mouseY > 423 && mouseY < 466)) && Mouse.isButtonDown(0)) {
            quitGame = true;
         }
         if(Display.isCloseRequested()) {
            quitGame = true;
         }

         DisplayManager.updateDisplay();
      }

      source.stop();

      source.play(gameMusic1);

      try {
         Thread.sleep(100);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      if(startFlag) {
         while(!quitGame && !gameOver) {
            Vector2f mapPointer = getMapPosition(player, terrain);
            gui1.setPosition(mapPointer);

            if(life < 1) {
               gameOver = true;
            }
            if(Display.isCloseRequested()) {
               quitGame = true;
            }

            player.move(terrain);
            camera.move();
            picker.update();
            renderer.renderShadowMap(shadowEntities, sun);
            ParticleMaster.update(camera);

            if(i1 < 40) {
               system.generateParticles(new Vector3f(100,10,-100));
               i1++;
            }

            GL11.glEnable(GL30.GL_CLIP_DISTANCE0);

            //render reflection texture
            buffers.bindReflectionFrameBuffer();
            float distance = 2 * (camera.getPosition().y - waters.get(0).getHeight());
            camera.getPosition().y -= distance;
            camera.invertPitch();
            renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, 1, 0, -waters.get(0).getHeight()+1));
            camera.getPosition().y += distance;
            camera.invertPitch();

            //render refraction texture
            buffers.bindRefractionFrameBuffer();
            renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, -1, 0, waters.get(0).getHeight()));

            //render to screen
            GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
            buffers.unbindCurrentFrameBuffer();

            multisampleFbo.bindFrameBuffer();
            renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, -1, 0, 100000));
            waterRenderer.render(waters, camera, sun);
            ParticleMaster.renderParticles(camera);
            multisampleFbo.unbindFrameBuffer();
            multisampleFbo.resolveToFbo(outputFbo);
            PostProcessing.doPostProcessing(outputFbo.getColourTexture());

            guiRenderer.render(guis);
            TextMaster.render();
            DisplayManager.updateDisplay();
         }
      }

      //*********Clean Up Below**************

      PostProcessing.cleanUp();
      outputFbo.cleanUp();
      multisampleFbo.cleanUp();
      ParticleMaster.cleanUp();
      TextMaster.cleanUp();
      buffers.cleanUp();
      waterShader.cleanUp();
      guiRenderer.cleanUp();
      renderer.cleanUp();
      loader.cleanUp();
      DisplayManager.closeDisplay();
   }


}