package org.newdawn.spaceinvaders;


public class CentipedeEntity extends Entity{

	private Game game;
	private boolean isHead = false;
	private CentipedeEntity front;
	private CentipedeEntity back;
	private int speedmag = 5;
	private vec2 targetpos;
	private vec2 next_targetpos;
	private boolean moveLeft = true;

	//head constructor
	public CentipedeEntity(Game game, int x,int y) {
		super("sprites/dot.png",x,y);
		front = null;
		back = null;
		this.game = game;
		isHead = true;
		
		targetpos = new vec2(x - 20, y);
		next_targetpos = new vec2(x - 20, y);
		
		setMaxHealth(2);
		resetHealth();
	}
	public CentipedeEntity(Game game, CentipedeEntity front) {
		super("sprites/dot.png",front.getX() + 20,front.getY());
		this.front = front;
		front.setBack(this);
		this.game = game;

		targetpos = new vec2(x - 20, y);
		next_targetpos = new vec2(x - 20, y);
		
		setMaxHealth(2);
		resetHealth();
	}
	
	
	public void setMoveLeft(boolean dir) {
		moveLeft = dir;
	}
	
	public CentipedeEntity getBack() {
		return back;
	}
	
	public CentipedeEntity getFront() {
		return front;
	}
	
	public void setBack(CentipedeEntity a_cent) {
		back = a_cent;
	}
	
	public void setFront(CentipedeEntity a_cent) {
		front = a_cent;
	}
	
	public void setHead(boolean newh) {
		isHead = newh;
	}

	public vec2 getTargetPos() {
		return targetpos;
	}
	public void setNextTargetPos(vec2 a_pos) {
		next_targetpos.x = a_pos.x;
		next_targetpos.y = a_pos.y;
	}

	public void moveLogic() {
		if(x == targetpos.x && y == targetpos.y) {
			
			if(back != null) {
				back.setNextTargetPos(targetpos);
			}
			
			if(isHead) {
				if(checkCollision()) { //collision
					if(y == 500) {
						targetpos.y = y - 20;
					}else {
						targetpos.y = y + 20;
					}
					moveLeft = !moveLeft;
				}else {
					targetpos.x = x + (moveLeft ? -20:20);
				}
			}else {
				targetpos.x = next_targetpos.x;
				targetpos.y = next_targetpos.y;
				moveLeft = (x > targetpos.x);
			}
			
		}
	}
	
	
	public void move() {
		if(targetpos.x != x) {
			setHorizontalMovement(speedmag * (moveLeft ? -1:1));
			setVerticalMovement(0);
		}else {
			setHorizontalMovement(0);
			setVerticalMovement(speedmag * ((targetpos.y > y) ? 1:-1));
		}
		super.move();
	}
	public boolean checkCollision() {
		if((x == 780 && !moveLeft) || (x == 0 && moveLeft)) {
			return true;
		}else if( (((x+20)/20) + 1 < 40) && (((x+20)/20) + 1 >= 0) &&
				(((x+20)/20) - 1 < 40) && (((x+20)/20) - 1 >= 0)){
			
			if(game.mush_locs[((x+20)/20) - 1][y/20] && moveLeft) {
				return true;
			}else if(game.mush_locs[((x+20)/20) + 1][y/20] && !moveLeft) {
				return true;
			}
		}
		return false;
	}
	
	public void collidedWith(Entity other) {
		// collisions with aliens are handled elsewhere
	}
	
	public class vec2{
		public int x,y;
		public vec2(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public boolean equals(vec2 a_pos) {
			return x == a_pos.x && y == a_pos.y;
		}
	}
	
}
