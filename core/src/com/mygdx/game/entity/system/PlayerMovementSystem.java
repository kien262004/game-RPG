package com.mygdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.CoreGame;
import com.mygdx.game.entity.ECSEngine;
import com.mygdx.game.entity.component.Box2DComponent;
import com.mygdx.game.entity.component.PlayerComponent;
import com.mygdx.game.input.GameKey;
import com.mygdx.game.input.InputManager;
import com.mygdx.game.input.KeyInputListener;

public class PlayerMovementSystem extends IteratingSystem implements KeyInputListener {

	private boolean directionChange;
	private boolean isAttack;
	private int xFactor;
	private int yFactor;

	public PlayerMovementSystem(final CoreGame game) {
		super(Family.all(PlayerComponent.class, Box2DComponent.class).get());
		game.getInputManager().addInputListener(this);
	}

	@Override
	protected void processEntity(final Entity entity, float deltaTime) {
		final PlayerComponent playerComponent = ECSEngine.playerCmpMapper.get(entity);
		final Box2DComponent b2dComponent = ECSEngine.box2dCmpMapper.get(entity);
		
		float speed = (float) Math.sqrt(xFactor * xFactor + yFactor * yFactor);
		float speedx = speed > 0 ? playerComponent.speed.x * xFactor / speed : 0;
		float speedy = speed > 0 ? playerComponent.speed.y * yFactor / speed : 0;
		
		if (isAttack) {
			playerComponent.isAttack = true;
			speedx = 0;
			speedy = 0;
		}
		else {
			playerComponent.isAttack = false;
		}

		if (directionChange) {
			b2dComponent.body.applyLinearImpulse(speedx - b2dComponent.body.getLinearVelocity().x * b2dComponent.body.getMass(),
												 speedy - b2dComponent.body.getLinearVelocity().y * b2dComponent.body.getMass(), 
												 b2dComponent.body.getWorldCenter().x, 
												 b2dComponent.body.getWorldCenter().y, true);
		}
	}

	@Override
	public void keyPressed(InputManager manager, GameKey gameKey) {
		switch(gameKey) {
		case UP:
			directionChange = true;
			yFactor = 1;
			break;
		case DOWN:
			directionChange = true;
			yFactor = -1;
			break;
		case LEFT:
			directionChange = true;
			xFactor = -1;
			break;
		case RIGHT:
			directionChange = true;
			xFactor = 1;
			break;
		case ATTACK:
			isAttack = true;
			break;
		default:
			break;
		}
	}

	@Override
	public void keyUp(InputManager manager, GameKey gameKey) {
		switch(gameKey) {
		case UP:
			directionChange = true;
			yFactor = manager.isKeyPressed(GameKey.DOWN) ? -1: 0;
			break;
		case DOWN:
			directionChange = true;
			yFactor = manager.isKeyPressed(GameKey.UP) ? 1: 0;
			break;
		case LEFT:
			directionChange = true;
			xFactor = manager.isKeyPressed(GameKey.RIGHT) ? 1: 0;
			break;
		case RIGHT:
			directionChange = true;
			xFactor = manager.isKeyPressed(GameKey.LEFT) ? -1: 0;
			break;
		case ATTACK:
			isAttack = false;
			break;
		default:
			break;
		}
		
	}
	
}
