package org.newdawn.spaceinvaders;

public class MushEntity extends Entity{
	public MushEntity(Game game, int x,int y) {
		super("sprites/mush.png", x, y);
		setMaxHealth(3);
		resetHealth();
	}
	
	
	public void collidedWith(Entity other) {
		
	}
}
