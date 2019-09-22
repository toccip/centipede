package org.newdawn.spaceinvaders;

import java.util.Random;

import org.newdawn.spaceinvaders.CentipedeEntity.vec2;

public class SpiderEntity extends Entity{

	private Game game;
	private boolean moveLeft = false;
	private boolean moveUp = true;
	private int x_move = 0;
	private int y_move = 0;
	private final int xdim = 43;
	private final int ydim = 29;
	private long prev_time = 0;
	private boolean spawning = true;
	
	private Random rand = new Random();
	public SpiderEntity(Game game) {
		super("sprites/alien.gif", game.width/2, -1 * 29);
		this.game = game;
		setMaxHealth(2);
		resetHealth();
	}
	
	
	public void moveLogic() {
		if(spawning) {
			x_move = 0;
			y_move = 2;
			if(y >= 10) {
				spawning = false;
			}
		}else {
			long cur_time = System.currentTimeMillis();
			if(cur_time - prev_time > 400) {
				prev_time = cur_time;
				x_move = (rand.nextInt(4)+1) * (rand.nextBoolean() ? 1:-1);
				y_move = (rand.nextInt(4)+1) * (rand.nextBoolean() ? 1:-1);
			}
			if((x + x_move >= game.width - xdim) || (x + x_move <= 0)) {
				x_move *= -1;
			}
			
			if((y + y_move >=  game.height - ydim) || (y + y_move <= 0)) {
				y_move *= -1;
			}
		}
	}
	
	public void move() {
		setHorizontalMovement(x_move);
		setVerticalMovement(y_move);
		super.move();
	}

	public void collidedWith(Entity other) {
		
	}
}
