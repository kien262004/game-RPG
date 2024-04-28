package com.mygdx.game.entity;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.CoreGame;
import com.mygdx.game.character.enemy.Enemy;
import com.mygdx.game.character.player.PlayerType;
import com.mygdx.game.effect.Effect;
import com.mygdx.game.entity.component.*;
import com.mygdx.game.entity.system.*;
import com.mygdx.game.items.food.Food;
import com.mygdx.game.items.food.FoodType;
import com.mygdx.game.items.weapon.Weapon;
import com.mygdx.game.map.GameObject;
import com.mygdx.game.view.AnimationType;

import static com.mygdx.game.CoreGame.UNIT_SCALE;
import static com.mygdx.game.view.DirectionType.DOWN;

public class ECSEngine extends PooledEngine{

	public static final ComponentMapper<PlayerComponent> playerCmpMapper = ComponentMapper.getFor(PlayerComponent.class);
	public static final ComponentMapper<Box2DComponent> box2dCmpMapper = ComponentMapper.getFor(Box2DComponent.class);
	public static final ComponentMapper<AnimationComponent> aniCmpMapper = ComponentMapper.getFor(AnimationComponent.class);
	public static final ComponentMapper<GameObjectComponent> gameObjCmpMapper = ComponentMapper.getFor(GameObjectComponent.class);
	public static final ComponentMapper<WeaponComponent> weaponCmpMapper = ComponentMapper.getFor(WeaponComponent.class);
	public static final ComponentMapper<EnemyComponent> enemyCmpMapper = ComponentMapper.getFor(EnemyComponent.class);
	public static final ComponentMapper<EffectComponent> effectCmpMapper = ComponentMapper.getFor(EffectComponent.class);
	public static final ComponentMapper<FoodComponent> foodCmpMapper = ComponentMapper.getFor(FoodComponent.class);

	private final World world;

	private final Vector2 localPosition;
	private final Vector2 posBeforeRotation;
	private final Vector2 posAfterRotation;

	private final Array<Food> foodArray;

	public ECSEngine(CoreGame game) {
		super();

		world = game.getWorld();

		localPosition = new Vector2();
		posBeforeRotation = new Vector2();
		posAfterRotation = new Vector2();

		foodArray = new Array<Food>();

		// them system chon engine
		this.addSystem(new PlayerMovementSystem(game));
		this.addSystem(new PlayerCameraSystem(game));
		this.addSystem(new AnimationSystem(game));
		this.addSystem(new PlayerAnimationSystem(game));
		this.addSystem(new CollisionSystem(game));
		this.addSystem(new PlayerAttackSystem(game));
		this.addSystem(new EffectSystem(game));
		this.addSystem(new EnemyMovementSystem(game));
	}

	public Array<Food> getFoodArray() {
		return foodArray;
	}

	public void createPlayer(final Vector2 playerSpawnLocation, PlayerType type, final float width, final float height, Weapon weapon) {
		final Entity player = this.createEntity();

		// player component
		final PlayerComponent playerComponent = this.createComponent(PlayerComponent.class);
		playerComponent.maxLife = type.getHealth();
		playerComponent.life = playerComponent.maxLife;
		playerComponent.speed.set(type.getSpeed());
		playerComponent.aniType = PlayerType.BLACK_NINJA_MAGE;
		playerComponent.direction = DOWN;
		playerComponent.weapon = weapon;
		player.add(playerComponent);

		// box2d component
		CoreGame.resetBodiesAndFixtureDefinition();
		final Box2DComponent box2DComponent = this.createComponent(Box2DComponent.class);
		CoreGame.BODY_DEF.position.set(playerSpawnLocation.x, playerSpawnLocation.y);
		CoreGame.BODY_DEF.fixedRotation = true;
		CoreGame.BODY_DEF.type = BodyDef.BodyType.DynamicBody;
		box2DComponent.body = world.createBody(CoreGame.BODY_DEF);
		box2DComponent.body.setUserData(player);
		box2DComponent.width = width;
		box2DComponent.height = height;
		box2DComponent.renderPosition = box2DComponent.body.getPosition();
//		box2DComponent.renderPosition.set(box2DComponent.body.getPosition());

		PolygonShape pShape = new PolygonShape();
		pShape.setAsBox(width * 0.5f, height * 0.5f);
		CoreGame.FIXTURE_DEF.filter.categoryBits = CoreGame.BIT_PLAYER;
		CoreGame.FIXTURE_DEF.filter.maskBits = -1;
		CoreGame.FIXTURE_DEF.shape = pShape;
		box2DComponent.body.createFixture(CoreGame.FIXTURE_DEF);

		pShape.dispose();
		player.add(box2DComponent);

		//animation component
		final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
		animationComponent.width = 16 * UNIT_SCALE;
		animationComponent.height = 16 * UNIT_SCALE;
		animationComponent.aniType = AnimationType.DOWN;
		animationComponent.path = playerComponent.aniType.getAtlasPath();
		player.add(animationComponent);
		this.addEntity(player);

	}

	public void createGameObject(final GameObject gameObject) {
		final Entity gameObjEntity = this.createEntity();

		// gameobject component
		final GameObjectComponent gameObjectComponent = this.createComponent(GameObjectComponent.class);
		gameObjectComponent.animationIndex = gameObject.getAnimationIndex();
		gameObjectComponent.type = gameObject.getType();
		gameObjEntity.add(gameObjectComponent);

		// animation component
		final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
		animationComponent.width = gameObject.getWidth();
		animationComponent.height = gameObject.getHeight();

		gameObjEntity.add(animationComponent);

		// box 2D component
		CoreGame.resetBodiesAndFixtureDefinition();
		final float halfW = gameObject.getWidth() / 2;
		final float halfH = gameObject.getHeight() / 2;
		final float angleRad = -gameObject.getRotDegree() * MathUtils.degreesToRadians;
		final Box2DComponent box2DComponent = this.createComponent(Box2DComponent.class);
		CoreGame.BODY_DEF.type = BodyDef.BodyType.DynamicBody;
		CoreGame.BODY_DEF.position.set(gameObject.getPosition().x + halfW, gameObject.getPosition().y + halfH);
		box2DComponent.body = world.createBody(CoreGame.BODY_DEF);
		box2DComponent.body.setUserData(gameObjEntity);
		box2DComponent.width = gameObject.getWidth();
		box2DComponent.height = gameObject.getHeight();

		// save position before rotation - Tiled is rotated around the bottom left corner instead of the center of a Tile
		localPosition.set(-halfW, -halfH);
		posBeforeRotation.set(box2DComponent.body.getWorldPoint(localPosition));
		// rotate body
		box2DComponent.body.setTransform(box2DComponent.body.getPosition(), angleRad);
		// get position after rotation
		posAfterRotation.set(box2DComponent.body.getWorldPoint(localPosition));
		//adjust position to its original value before the rotation
		box2DComponent.body.setTransform(box2DComponent.body.getPosition().add(posBeforeRotation).sub(posAfterRotation), angleRad);
		box2DComponent.renderPosition.set(box2DComponent.body.getPosition().x, box2DComponent.body.getPosition().y);

		CoreGame.FIXTURE_DEF.filter.categoryBits = CoreGame.BIT_GAME_OBJECT;
		CoreGame.FIXTURE_DEF.filter.maskBits = CoreGame.BIT_PLAYER|CoreGame.BIT_WEAPON;
		CoreGame.FIXTURE_DEF.isSensor = true;
		PolygonShape pShape = new PolygonShape();
		pShape.setAsBox(halfW, halfH);
		CoreGame.FIXTURE_DEF.shape = pShape;
		box2DComponent.body.createFixture(CoreGame.FIXTURE_DEF);
		pShape.dispose();
		gameObjEntity.add(box2DComponent);

		this.addEntity(gameObjEntity);
	}

	public void createPlayerWeapon(final Weapon weapon) {
		final Entity weaponEntity = this.createEntity();
		// weapon component
		final WeaponComponent weaponComponent = this.createComponent(WeaponComponent.class);
		weaponComponent.type = weapon.type;
		weaponComponent.direction = weapon.direction;
		weaponComponent.attack = weapon.type.getAttack();
		weaponEntity.add(weaponComponent);

		// animation component
		final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
		animationComponent.width = weapon.getWidth();
		animationComponent.height = weapon.getHeight();
		weaponEntity.add(animationComponent);

		// box 2D component
		CoreGame.resetBodiesAndFixtureDefinition();
		final float halfW = weapon.getWidth() / 2;
		final float halfH = weapon.getHeight() / 2;
		final float angleRad = -weapon.getDirection().getCode() * MathUtils.degreesToRadians * 90;
		final Box2DComponent box2DComponent = this.createComponent(Box2DComponent.class);
		CoreGame.BODY_DEF.type = BodyDef.BodyType.KinematicBody;
		CoreGame.BODY_DEF.position.set(weapon.getPosition().x + weapon.posDirection[weapon.direction.getCode()].x , weapon.getPosition().y + weapon.posDirection[weapon.direction.getCode()].y);
		box2DComponent.body = world.createBody(CoreGame.BODY_DEF);
		box2DComponent.body.setUserData(weaponEntity);
		box2DComponent.width = weapon.getWidth();
		box2DComponent.height = weapon.getHeight();
		Vector2 position = new Vector2(weapon.getPosition().x + weapon.effDirection[weapon.direction.getCode()].x , weapon.getPosition().y + weapon.effDirection[weapon.direction.getCode()].y);
		weapon.effect.setPosition(position);

		// save position before rotation - Tiled is rotated around the bottom left corner instead of the center of a Tile
		localPosition.set(0, 0);
		posBeforeRotation.set(box2DComponent.body.getWorldPoint(localPosition));
		// rotate body
		box2DComponent.body.setTransform(box2DComponent.body.getPosition(), angleRad);
		// get position after rotation
		posAfterRotation.set(box2DComponent.body.getWorldPoint(localPosition));
//		adjust position to its original value before the rotation
		box2DComponent.body.setTransform(box2DComponent.body.getPosition().add(posBeforeRotation).sub(posAfterRotation), angleRad);
//		box2DComponent.renderPosition.set(box2DComponent.body.getPosition().x - animationComponent.width * 1f, box2DComponent.body.getPosition().y - animationComponent.height * 1f);
		box2DComponent.renderPosition.set(box2DComponent.body.getPosition().x, box2DComponent.body.getPosition().y);
		//animation component
		CoreGame.FIXTURE_DEF.filter.categoryBits = CoreGame.BIT_WEAPON;
		CoreGame.FIXTURE_DEF.filter.maskBits = -1;
		CoreGame.FIXTURE_DEF.isSensor = true;
		PolygonShape pShape = new PolygonShape();
		pShape.setAsBox(halfW, halfH);
		CoreGame.FIXTURE_DEF.shape = pShape;
		box2DComponent.body.createFixture(CoreGame.FIXTURE_DEF);
		pShape.dispose();
		weaponEntity.add(box2DComponent);

		this.addEntity(weaponEntity);
	}

	public void createEnemy(final Enemy enemy) {
		final Entity enemyEnity = this.createEntity();

		// enemy component
		final EnemyComponent enemyComponent = this.createComponent(EnemyComponent.class);
		enemyComponent.maxLife = enemy.getType().getMaxLife();
		enemyComponent.life = enemyComponent.maxLife;
		enemyComponent.type = enemy.getType();
		enemyComponent.speed.set(enemy.getType().getSpeed(), enemy.getType().getSpeed());
		enemyComponent.attack = enemy.getType().getAttack();
		enemyEnity.add(enemyComponent);

		// animation component
		final AnimationComponent animationComponent = this.createComponent(AnimationComponent.class);
		animationComponent.width = enemy.getWidth() * UNIT_SCALE;
		animationComponent.height = enemy.getHeight()* UNIT_SCALE;
		animationComponent.aniType = AnimationType.DOWN;
		animationComponent.path = enemyComponent.type.getAtlasPath();
		enemyEnity.add(animationComponent);

		// box 2D component
		CoreGame.resetBodiesAndFixtureDefinition();
		final float halfW = enemy.getWidth() * UNIT_SCALE / 2;
		final float halfH = enemy.getHeight() * UNIT_SCALE / 2;
		final Box2DComponent box2DComponent = this.createComponent(Box2DComponent.class);
		CoreGame.BODY_DEF.type = BodyDef.BodyType.DynamicBody;
		CoreGame.BODY_DEF.position.set(enemy.getPosition().x + halfW, enemy.getPosition().y + halfH);
		box2DComponent.body = world.createBody(CoreGame.BODY_DEF);
		box2DComponent.body.setUserData(enemyEnity);
		box2DComponent.width = enemy.getWidth() * UNIT_SCALE;
		box2DComponent.height = enemy.getHeight() * UNIT_SCALE;
		box2DComponent.renderPosition.set(box2DComponent.body.getPosition().x, box2DComponent.body.getPosition().y);

		CoreGame.FIXTURE_DEF.filter.categoryBits = CoreGame.BIT_ENEMY;
		CoreGame.FIXTURE_DEF.filter.maskBits = -1;
		PolygonShape pShape = new PolygonShape();
		pShape.setAsBox(halfW, halfH);
		CoreGame.FIXTURE_DEF.shape = pShape;
		box2DComponent.body.createFixture(CoreGame.FIXTURE_DEF);
		pShape.dispose();
		enemyEnity.add(box2DComponent);
		this.addEntity(enemyEnity);
	}

	public void createEffect(final Effect effect) {
		final Entity effectEntity = this.createEntity();
		// effect component
		EffectComponent effectComponent = this.createComponent(EffectComponent.class);
		effectComponent.type = effect.getType();
		effectComponent.aniTime = 0;
		effectComponent.height = effect.getType().getHeight() * UNIT_SCALE;
		effectComponent.width = effect.getType().getWidth() * UNIT_SCALE;
		effectComponent.path = effect.getType().getAtlasPath();
		effectComponent.position = effect.getPosition();
		effectComponent.direction = effect.getDirection();
		effectEntity.add(effectComponent);
		this.addEntity(effectEntity);
	}

	public void createFood(final FoodType foodType, final Vector2 foodPosition) {
		final Entity foodEntity = this.createEntity();

		//food component
		FoodComponent foodComponent = this.createComponent(FoodComponent.class);
		foodComponent.foodType = foodType;
		foodComponent.timeRemain = foodType.getTime();
		foodComponent.height = foodType.getHeight() * UNIT_SCALE;
		foodComponent.width = foodType.getWidth() * UNIT_SCALE;
		foodEntity.add(foodComponent);

		//box2d component
		CoreGame.resetBodiesAndFixtureDefinition();
		final float halfW = foodType.getWidth() * UNIT_SCALE / 2;
		final float halfH = foodType.getHeight() * UNIT_SCALE / 2;
		final Box2DComponent box2DComponent = this.createComponent(Box2DComponent.class);
		CoreGame.BODY_DEF.type = BodyDef.BodyType.DynamicBody;
		CoreGame.BODY_DEF.position.set(foodPosition.x + halfW, foodPosition.y + halfH);
		box2DComponent.body = world.createBody(CoreGame.BODY_DEF);
		box2DComponent.body.setUserData(foodEntity);
		box2DComponent.width = foodType.getWidth() * UNIT_SCALE;
		box2DComponent.height = foodType.getHeight() * UNIT_SCALE;
		box2DComponent.renderPosition.set(box2DComponent.body.getPosition().x, box2DComponent.body.getPosition().y);

		CoreGame.FIXTURE_DEF.filter.categoryBits = CoreGame.BIT_GAME_OBJECT;
		CoreGame.FIXTURE_DEF.filter.maskBits = CoreGame.BIT_PLAYER;
		CoreGame.FIXTURE_DEF.isSensor = true;
		PolygonShape pShape = new PolygonShape();
		pShape.setAsBox(halfW, halfH);
		CoreGame.FIXTURE_DEF.shape = pShape;
		box2DComponent.body.createFixture(CoreGame.FIXTURE_DEF);
		pShape.dispose();
		foodEntity.add(box2DComponent);

		this.addEntity(foodEntity);
	}
}
