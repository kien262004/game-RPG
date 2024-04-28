package com.mygdx.game.entity.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.mygdx.game.CoreGame;
import com.mygdx.game.WorldContactListener.CollisionListener;
import com.mygdx.game.entity.ECSEngine;
import com.mygdx.game.entity.component.*;
import com.mygdx.game.items.food.Food;
import com.mygdx.game.items.food.FoodType;
import com.mygdx.game.map.GameObjectType;

import static com.mygdx.game.character.enemy.RandomItem.randomFood;

public class CollisionSystem extends IteratingSystem implements CollisionListener {
    CoreGame game;
    public CollisionSystem(final CoreGame game) {
        super(Family.all(RemoveComponent.class).get());
        this.game = game;
        game.getWorldContactListener().addPlayerCollisionListener(this);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        getEngine().removeEntity(entity);
    }

    @Override
    public void playerCollision(Entity player, Entity gameObj) {
        final GameObjectComponent gameObjCmp = ECSEngine.gameObjCmpMapper.get(gameObj);
        final PlayerComponent playerCmp = ECSEngine.playerCmpMapper.get(player);
    }

    @Override
    public void weaponCollision(Entity weapon, Entity gameObj) {
//        final GameObjectComponent gameObjCmp = ECSEngine.gameObjCmpMapper.get(gameObj);
//        if (gameObjCmp.type == GameObjectType.TRAP) {
//            gameObj.add(((ECSEngine) getEngine()).createComponent(RemoveComponent.class));
//        }
    }

    @Override
    public void playerVSEnemy(Entity player, Entity enemy) {
        final PlayerComponent playerCmp = ECSEngine.playerCmpMapper.get(player);
        final EnemyComponent enemyCmp = ECSEngine.enemyCmpMapper.get(enemy);
        final Box2DComponent box2dEnCmp = ECSEngine.box2dCmpMapper.get(enemy);
        playerCmp.life =  Math.max(playerCmp.life - enemyCmp.attack, 0);
        game.getGameUI().updateHeart();
        if (playerCmp.life <= 0) {
            Gdx.app.debug("Player", "dead");
        }
    }

    @Override
    public void weaponVSEnemy(Entity weapon, Entity enemy) {
        final WeaponComponent weaponCmp = ECSEngine.weaponCmpMapper.get(weapon);
        final EnemyComponent enemyCmp = ECSEngine.enemyCmpMapper.get(enemy);
        final Box2DComponent box2dEnCmp = ECSEngine.box2dCmpMapper.get(weapon);
        enemyCmp.life -= weaponCmp.attack;

        if (enemyCmp.life <= 0) {
            // thêm tạo cửa sổ để them do
            FoodType foodType = randomFood();
            Food food = new Food(foodType, box2dEnCmp.body.getPosition());
            game.getEcsEngine().getFoodArray().add(food);
            enemy.add(((ECSEngine) getEngine()).createComponent(RemoveComponent.class));
        }
    }

    @Override
    public void playerVSItem(Entity player, Entity item) {
        final PlayerComponent playerCmp = ECSEngine.playerCmpMapper.get(player);
        final FoodComponent itemCmp = ECSEngine.foodCmpMapper.get(item);

        item.add(((ECSEngine) getEngine()).createComponent(RemoveComponent.class));
    }
}
