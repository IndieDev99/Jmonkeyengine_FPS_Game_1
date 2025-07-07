package  mygame;


import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Quaternion;
import com.jme3.input.MouseInput;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.math.FastMath;
import com.jme3.water.WaterFilter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;


public class Main extends SimpleApplication 
        implements ActionListener {
    

  final private Vector3f walkDirection = new Vector3f();
  
  private  Spatial ak47;
  private  Spatial Tank;
  private  Spatial MP5;
 CameraNode cameraNode;
 
 
 // Player Node
 Node playerNode;


 
  public static void main(String[] args) {
    Main app = new Main();
    app.start();
    
  }

     
   public Main() {
        // these are the default AppsStates minus the FlyCam
        super(new StatsAppState(), new AudioListenerState(), new DebugKeysAppState());
    }
     
  
  
  

  @Override
  public void simpleInitApp() {
   
      cameraNode =new CameraNode("CameraNode",cam);
      cameraNode.setLocalTranslation(0, 2, 0);
     
      // Enable physics...
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false); // enable to visualize physics meshes
        stateManager.attach(bulletAppState);
   
       
     
      Box b=new Box(1,1,1);
      Geometry geom=new Geometry("Box",b);
      Material mat=new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
      mat.setColor("Color", ColorRGBA.Blue);
      geom.setMaterial(mat);
      
      
      
     //Weapons 
     
     ak47=assetManager.loadModel("Models/Weapon/ak47.j3o");
     ak47.setLocalTranslation(-2, -1.2f, 3.5f);
     Quaternion akRotation=new Quaternion();
     akRotation.fromAngles(0, FastMath.PI, 0);  
     ak47.setLocalRotation(akRotation);
     
     
     //MP 5 in Scene
     
     MP5=assetManager.loadModel("Models/Weapon/MP5.j3o");
     MP5.setLocalTranslation(0, 7, -26);
      Quaternion mp5Rotation=new Quaternion();
     mp5Rotation.fromAngles(0, 0, FastMath.PI/2);  
    MP5.setLocalRotation(mp5Rotation);
     rootNode.attachChild(MP5);
   
     
     
     
     
     
     
     // Tanks
     
     
     Tank=assetManager.loadModel("Models/Weapon/Tank.j3o");
     Tank.setLocalTranslation(0, 0, -28);
     Tank.setLocalScale(4, 4, 4);
     rootNode.attachChild(Tank);
     
     
   

    // We re-use the flyby camera for rotation, while positioning is handled by physics
 
    viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
    
   
    setUpLight();


   
   
    //Terrain and Water
    Spatial sceneModel = assetManager.loadModel("Scenes/map.j3o");
      setupWaterFilter();
   
   
    sceneModel.setLocalScale(2f);

    // We set up collision detection for the scene 
    
    
    CollisionShape sceneShape =
            CollisionShapeFactory.createMeshShape(sceneModel);
    RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
    sceneModel.addControl(landscape);

 

    rootNode.attachChild(sceneModel);
    bulletAppState.getPhysicsSpace().add(landscape);
 
    PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
    playerNode = createCharacter(physicsSpace);
      
       
   playerNode.setLocalTranslation(0, 10, 0);
   
   rootNode.attachChild(cameraNode);
   cameraNode.attachChild(ak47);
   rootNode.attachChild(playerNode);
    setupInput(playerNode.getControl(BetterCharacterControl.class));
    
  }
  
  
  
  
      private void setupInput(BetterCharacterControl characterControl) {

        inputManager.setCursorVisible(false);

        // set up the basic movement functions for our character.
      playerMovement characterMovementState = new playerMovement(characterControl,cameraNode);
        stateManager.attach(characterMovementState);

        // add a mapping for our shoot function.
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Shoot");
    }

    public void onAction(String binding, boolean isPressed, float tpf) {

        
        
         if (binding.equals("Shoot") && !isPressed) {
        
        
        
        
        Geometry bullet = new Geometry("Bullet", new Sphere(32, 32, 0.1f));
        
            Material bulletMaterial=new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
           bulletMaterial.setColor("Color", ColorRGBA.Red);
           bullet.setMaterial(bulletMaterial);
           
            rootNode.attachChild(bullet);

            sceneBullets.add(new TimedBullet(bullet, 10));

            RigidBodyControl rigidBodyControl = new RigidBodyControl(CollisionShapeFactory.createDynamicMeshShape(bullet), 0.5f);
            rigidBodyControl.setCcdMotionThreshold(.2f);
            rigidBodyControl.setCcdSweptSphereRadius(.2f);
            bullet.addControl(rigidBodyControl);

            stateManager.getState(BulletAppState.class).getPhysicsSpace().add(rigidBodyControl);
            
            //Bullet Position from where it will be shot let now make it AK47 or other same weapon
            Vector3f bulletLocation = ak47.localToWorld(new Vector3f(-0.2f, 0.7f, -7.0f), null);

            rigidBodyControl.setPhysicsLocation(bulletLocation);
            rigidBodyControl.setPhysicsRotation(cam.getRotation());
            rigidBodyControl.applyImpulse(cam.getDirection().mult(20), new Vector3f());
        
  
         
         }
        
        
    }
  
 
  
  

  private Node createCharacter(PhysicsSpace physicsSpace) {

        BetterCharacterControl characterControl = new BetterCharacterControl(0.5f, 2, 2);
        characterControl.setJumpForce(new Vector3f(0, 10, 0));

        Node playerNode = new Node("Player");
        playerNode.addControl(characterControl);

        physicsSpace.add(characterControl);

        characterControl.warp(new Vector3f(0, 15, 0));
        
        return playerNode;
    }
  
  private void setUpLight() {
    // We add light so we see the scene
    AmbientLight al = new AmbientLight();
    al.setColor(ColorRGBA.White.mult(1.3f));
    rootNode.addLight(al);

    DirectionalLight dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White);
    dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
    rootNode.addLight(dl);
  }

   

 
 
     


  @Override
    public void simpleUpdate(float tpf) {
        
    cameraNode.setLocalTranslation(playerNode.getLocalTranslation().add(0, 2,0));

        playerNode.getControl(BetterCharacterControl.class).setViewDirection(cameraNode.getLocalTranslation());
        
        
        sceneBullets.removeIf(bullet -> {

            if (bullet.updateTime(tpf) > bullet.getMaxTime()) {
                bullet.getSpatial().removeFromParent();
                RigidBodyControl rigidBodyControl = bullet.getSpatial().getControl(RigidBodyControl.class);
                getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(rigidBodyControl);
                return true;
            }

            return false;
        });
        
        
        

    }
   
    

    
   private void setupWaterFilter() {
            // 1. First verify the file exists
        String waterFilterPath = "Scenes/Water.j3f";
         

        // 2. Try loading with proper initialization
        try {
            // Register assets folder if not already done
            assetManager.registerLocator("assets", FileLocator.class);
           
            // Create processor
            FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
           
            // Load water filter - try both methods
            WaterFilter waterFilter;
            try {
                // Method 1: Direct load
                waterFilter = (WaterFilter) assetManager.loadAsset(waterFilterPath);
            } catch (Exception e) {
                // Method 2: Create new and configure
                System.out.println("Using manual water filter creation");
                waterFilter = new WaterFilter(rootNode, walkDirection);
                configureWaterFilter(waterFilter);
            }
           
            fpp.addFilter(waterFilter);
            viewPort.addProcessor(fpp);
           
        } catch (Exception e) {
            System.err.println("Failed to initialize water: " + e.getMessage());
            e.printStackTrace();
            createFallbackWater();
        }
    }

     

    private void configureWaterFilter(WaterFilter filter) {
        // Set default water parameters
        filter.setWaterHeight(0);
        filter.setWaterTransparency(0.2f);
        filter.setMaxAmplitude(0.5f);
        filter.setWaveScale(0.005f);
        filter.setSpeed(0.7f);
        filter.setShoreHardness(0.8f);
        filter.setUseFoam(true);
        filter.setUseRipples(true);
        filter.setRefractionConstant(0.2f);
    }

    private void createFallbackWater() {
        System.out.println("Creating fallback water effect");
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        WaterFilter waterFilter = new WaterFilter(rootNode, walkDirection);
        configureWaterFilter(waterFilter);
        fpp.addFilter(waterFilter);
        viewPort.addProcessor(fpp);
   
    }
    
    
    
    // Bullets List 

    private final List<TimedBullet> sceneBullets = new ArrayList<>();

    private static class TimedBullet {

        private final Spatial bullet;
        private final float maxTime;
        private float time;

        public TimedBullet(Spatial bullet, float maxTime) {
            this.bullet = bullet;
            this.maxTime = maxTime;
        }

        public Spatial getSpatial() {
            return bullet;
        }

        public float getMaxTime() {
            return maxTime;
        }

        public float getTime() {
            return time;
        }

        public float updateTime(float tpf) {
            time += tpf;
         
            return time;
        }

    }
    
    
}