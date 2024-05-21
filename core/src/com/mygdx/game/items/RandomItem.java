package com.mygdx.game.items;


import com.mygdx.game.items.food.FoodType;
import com.mygdx.game.items.weapon.WeaponType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomItem {
    public static FoodType randomFood() {
        FoodType listFood[] = FoodType.values();
        Random random = new Random();

        // Tạo số nguyên ngẫu nhiên
        int randomNumber = random.nextInt(1000);
        return listFood[randomNumber % listFood.length];
    }
    public static WeaponType randomWeapon() {
        List<WeaponType> listWeapon = new ArrayList<>(Arrays.asList(WeaponType.values()));
//        listWeapon.remove(WeaponType.NOT_GIVEN)

        Random random = new Random();
        int randomNumber = random.nextInt(1000);
        return listWeapon.get(randomNumber % listWeapon.size());
    }

    public static WeaponType randomHotWeapon() {
        List<WeaponType> listWeapon = new ArrayList<>();
        listWeapon.add(WeaponType.BIGSWORD);
        listWeapon.add(WeaponType.CLUB);
//        listWeapon.add(WeaponType.HAMMER);
        Random random = new Random();
        int randomNumber = random.nextInt(1000);
        return listWeapon.get(randomNumber % listWeapon.size());
    }
}
