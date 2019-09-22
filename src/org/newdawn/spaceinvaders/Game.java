package org.newdawn.spaceinvaders;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class Game extends Canvas {
	/** The stragey that allows us to use accelerate page flipping */
	private BufferStrategy strategy;
	/** True if the game is currently "running", i.e. the game loop is looping */
	private boolean gameRunning = true;
	/** The list of all the entities that exist in our game */
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	/** The list of entities that need to be removed from the game this loop */
	private ArrayList<Entity> removeList = new ArrayList<Entity>();
	/** The entity representing the player */
	private Entity ship;

	/** The time at which last fired a shot */
	private long lastFire = 0;
	/** The interval between our players shot (ms) */
	private long firingInterval = 300;

	private int segmentCount = 0;
	
	private String message = "";
	/** True if we're holding up game play until a key has been pressed */
	private boolean waitingForKeyPress = true;

	/** True if we are firing */
	private boolean firePressed = false;
	
	private MyMouseListener mml;
	
	public final int width = 800;
	public final int height = 600;
	
	public boolean[][] mush_locs = new boolean[41][30];
	public static final double mush_prob = 0.05f;
	private boolean first_start = true;
	private boolean spawn_new_pede = false;
	private int lives = 3;
	private int score = 0;
	
	private String hud_msg = "";
	private boolean gameOver = false;

	private boolean shipDeathUsed = false;
	/**
	 * Construct our game and set it running.
	 */
	public Game() {
		// create a frame to contain our game
		JFrame container = new JFrame("Space Invaders 101");
		
		// get hold the content of the frame and set up the resolution of the game
		JPanel panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(width,height));
		panel.setLayout(null);
		
		// setup our canvas size and put it into the content of the frame
		setBounds(0,0,800,600);
		panel.add(this);
		
		// Tell AWT not to bother repainting our canvas since we're
		// going to do that our self in accelerated mode
		setIgnoreRepaint(true);
		
		// finally make the window visible 
		container.pack();
		container.setResizable(false);
		container.setVisible(true);
		
		// add a listener to respond to the user closing the window. If they
		// do we'd like to exit the game
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		// add a key input system (defined below) to our canvas
		// so we can respond to key pressed
		addKeyListener(new KeyInputHandler());
		mml = new MyMouseListener();
		
		addMouseListener(mml);
		addMouseMotionListener(mml);
		
		// request the focus so key events come to us
		requestFocus();

		// create the buffering strategy which will allow AWT
		// to manage our accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		
		
		container.setCursor( container.getToolkit().createCustomCursor(
                new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB ),
                new Point(),
                null ) );
		
		// initialise the entities in our game so there's something
		// to see at startup
		initEntities();
	}
	
	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
	private void startGame() {
		// clear out any existing entities and intialise a new set
		if(!first_start) {
			entities.clear();
			initEntities();
			first_start = false;
		}
		
		firePressed = false;
	}
	
	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entitiy will be added to the overall list of entities in the game.
	 */
	private void initEntities() {
		
		CentipedeEntity headcent = new CentipedeEntity(this, 800, 0);
		entities.add(headcent);
		segmentCount++;
		for(int i = 0; i < 19; i++) {
			CentipedeEntity newcent = new CentipedeEntity(this, (CentipedeEntity)entities.get(entities.size()-1));
			entities.add(newcent);
			segmentCount++;
		}
		
		for(int i = 0; i < 40; i++) {
			for(int j = 0; j < 30; j++) {
				mush_locs[i][j] = false;
			}
		}
		
		
		
		for(int j = 1; j < 23; j++){
			for(int i = 3; i < 39; i++) {
				double mush_rand = Math.random();
				if(mush_rand <= mush_prob) {
					if(!mush_locs[i-1][j-1] && !mush_locs[i+1][j-1]) {
						entities.add(new MushEntity(this, 20*i -20, j*20));
						mush_locs[i][j] = true;
					}
				}
			}
		}
		entities.add(new SpiderEntity(this));
		ship = new ShipEntity(this,"sprites/ship.gif",370,550);
		entities.add(ship);
		
	}
	
	
	/**
	 * Remove an entity from the game. The entity removed will
	 * no longer move or be drawn.
	 * 
	 * @param entity The entity that should be removed
	 */
	public void removeEntity(Entity entity) {
		removeList.add(entity);
		
		if(entity instanceof CentipedeEntity) {
			CentipedeEntity tempref = (CentipedeEntity) entity;
			if(tempref.getFront() != null) {
				tempref.getFront().setBack(null);
			}
			if(tempref.getBack() != null) {
				tempref.getBack().setFront(null);
				tempref.getBack().setHead(true);
			}
		}
	}
	
	/**
	 * Notification that the player has died. 
	 */
	public void notifyDeath() {
		if(shipDeathUsed) {
			return;
		}
		waitingForKeyPress = true;
		shipDeathUsed = true;
		if(lives == 1) {
			lives--;
			message = "Game Over!";
			gameOver = true;
		}else {
			for (int i=0;i<entities.size();i++) {
				if((entities.get(i) instanceof CentipedeEntity)
					|| (entities.get(i) instanceof SpiderEntity)) {
					removeEntity(entities.get(i));
				}else if(entities.get(i) instanceof MushEntity){
					if(entities.get(i).getHealth() != entities.get(i).getMaxHealth()) {
						score += 10;
					}
					entities.get(i).resetHealth();
					
				}
			}
			lives--;
			spawnPede();
			entities.add(new SpiderEntity(this));
		}
	}
	
	public void spawnPede() {
		spawn_new_pede = false;
		CentipedeEntity headcent = new CentipedeEntity(this, 800, 0);
		entities.add(headcent);
		segmentCount++;
		for(int i = 0; i < 20; i++) {
			CentipedeEntity newcent = new CentipedeEntity(this, (CentipedeEntity)entities.get(entities.size()-1));
			entities.add(newcent);
			segmentCount++;
		}
	}
	
	public void notifyCentipedeKilled() {
		score += 5;
		segmentCount--;
		if(segmentCount == 0) {
			spawn_new_pede = true;
		}
	}
	
	public void notifyMushKilled() {
		score += 5;
	}
	
	public void notifySpiderKilled() {
		score += 600;
	}
	
	public void addScore(int added) {
		score += added;
	}
	
	
	/**
	 * Attempt to fire a shot from the player. Its called "try"
	 * since we must first check that the player can fire at this 
	 * point, i.e. has he/she waited long enough between shots
	 */
	public void tryToFire() {
		// check that we have waiting long enough to fire
		if (System.currentTimeMillis() - lastFire < firingInterval) {
			return;
		}
		
		// if we waited long enough, create the shot entity, and record the time.
		lastFire = System.currentTimeMillis();
		ShotEntity shot = new ShotEntity(this,"sprites/shot.gif",ship.getX()+10,ship.getY()-30);
		SoundClip shot_effect = new SoundClip("src/sprites/laser_sound.wav");
		entities.add(shot);
	}
	
	/**
	 * The main game loop. This loop is running during all game
	 * play as is responsible for the following activities:
	 * <p>
	 * - Working out the speed of the game loop to update moves
	 * - Moving the game entities
	 * - Drawing the screen contents (entities, text)
	 * - Updating game events
	 * - Checking Input
	 * <p>
	 */
	public void gameLoop() {
		long lastLoopTime = System.currentTimeMillis();
		
		while (gameRunning) {
			
			if((System.currentTimeMillis() - lastLoopTime) >= 15) {
				lastLoopTime = System.currentTimeMillis();
				Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
				
				g.setColor(Color.black);
				g.fillRect(0,0,800,600);
				
				
				
				if(!waitingForKeyPress && !gameOver) {
					
					if(spawn_new_pede) {
						spawnPede();
					}
					
					//resolve move logic
					for (int i=0;i<entities.size();i++) {
						Entity entity = (Entity) entities.get(i);
						entity.moveLogic();
					}
					//move
					for (int i=0;i<entities.size();i++) {
						Entity entity = (Entity) entities.get(i);
						
						entity.move();
					}
					
					ship.setX(mml.getX());
					ship.setY(mml.getY());
					
					
					
					if (firePressed) {
						tryToFire();
					}
					
				}else {
					g.setColor(Color.white);
					g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,250);
					if(!gameOver) {
						g.drawString("Press any key to start",(800-g.getFontMetrics().stringWidth("Press any key to start"))/2,300);
					}
					
				}
				
				for (int i=0;i<entities.size();i++) {
					Entity entity = (Entity) entities.get(i);
					
					entity.draw(g);
				}
				
				if(!waitingForKeyPress) {
					for (int p=0;p<entities.size();p++) {
						for (int s=p+1;s<entities.size();s++) {
							Entity me = (Entity) entities.get(p);
							Entity him = (Entity) entities.get(s);
							
							if (me.collidesWith(him)) {
								me.collidedWith(him);
								him.collidedWith(me);
							}
						}
					}
					
				}
				
				g.setColor(Color.white);
				hud_msg = "Lives: " + lives + " -  Score: " + score;				
				g.drawString(hud_msg, 5, 590);
				
				entities.removeAll(removeList);
				removeList.clear();
				
				g.dispose();
				strategy.show();
			}
			
			
		}
	}
	
	/**
	 * A class to handle keyboard input from the user. The class
	 * handles both dynamic input during game play, i.e. left/right 
	 * and shoot, and more static type input (i.e. press any key to
	 * continue)
	 * 
	 * This has been implemented as an inner class more through 
	 * habbit then anything else. Its perfectly normal to implement
	 * this as seperate class if slight less convienient.
	 * 
	 * @author Kevin Glass
	 */
	private class KeyInputHandler extends KeyAdapter {
		/** The number of key presses we've had while waiting for an "any key" press */
		private int pressCount = 1;
		
		/**
		 * Notification from AWT that a key has been pressed. Note that
		 * a key being pressed is equal to being pushed down but *NOT*
		 * released. Thats where keyTyped() comes in.
		 *
		 * @param e The details of the key that was pressed 
		 */
		public void keyPressed(KeyEvent e) {
			// if we're waiting for an "any key" typed then we don't 
			// want to do anything with just a "press"
			if (waitingForKeyPress) {
				return;
			}
			
			
		} 
		
		/**
		 * Notification from AWT that a key has been released.
		 *
		 * @param e The details of the key that was released 
		 */
		public void keyReleased(KeyEvent e) {
			// if we're waiting for an "any key" typed then we don't 
			// want to do anything with just a "released"
			if (waitingForKeyPress) {
				return;
			}
			
		}

		/**
		 * Notification from AWT that a key has been typed. Note that
		 * typing a key means to both press and then release it.
		 *
		 * @param e The details of the key that was typed. 
		 */
		public void keyTyped(KeyEvent e) {
			// if we're waiting for a "any key" type then
			// check if we've recieved any recently. We may
			// have had a keyType() event from the user releasing
			// the shoot or move keys, hence the use of the "pressCount"
			// counter.
			if (waitingForKeyPress) {
				if (pressCount == 1) {
					// since we've now recieved our key typed
					// event we can mark it as such and start 
					// our new game
					waitingForKeyPress = false;
					shipDeathUsed = false;
					startGame();
					pressCount = 0;
				} else {
					pressCount++;
				}
			}
			
			// if we hit escape, then quit the game
			if (e.getKeyChar() == 27) {
				System.exit(0);
			}
		}
	}
	
	private class MyMouseListener extends MouseAdapter{
		public int x = 0 , y = 0;
		public void mouseMoved(MouseEvent e) {
			x = e.getX();
			y = e.getY();
		}
		
		public void mouseDragged(MouseEvent e) {
			x = e.getX();
			y = e.getY();
		}
		
		public void mousePressed(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1) {
				
				firePressed = true;
			}
		}
		public void mouseReleased(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1) {
				
				firePressed = false;
			}
		}
		public int getX() {
			return x;
		}
		public int getY() {
			return y;
		}
		
	}
	
	public class SoundClip implements Runnable{
		String src;
		Thread sound_thread;
		public SoundClip(String src) {
			this.src = src;
			start();
		}
		
		
		public void run() {
			try {
		         // Open an audio input stream.           
		          File soundFile = new File(src);
		          AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);              
		         // Get a sound clip resource.
		         Clip clip = AudioSystem.getClip();
		         // Open audio clip and load samples from the audio input stream.
		         clip.open(audioIn);
		         clip.start();
		      } catch (UnsupportedAudioFileException e) {
		         e.printStackTrace();
		      } catch (IOException e) {
		         e.printStackTrace();
		      } catch (LineUnavailableException e) {
		         e.printStackTrace();
		      }
		}
		
		public void start() {
			sound_thread = new Thread(this);
			sound_thread.start();
		}
		
	}
	/**
	 * The entry point into the game. We'll simply create an
	 * instance of class which will start the display and game
	 * loop.
	 * 
	 * @param argv The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		Game g =new Game();

		// Start the main game loop, note: this method will not
		// return until the game has finished running. Hence we are
		// using the actual main thread to run the game.
		g.gameLoop();
	}
}
