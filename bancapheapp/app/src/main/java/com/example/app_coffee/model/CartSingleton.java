package com.example.app_coffee.model;

import java.util.ArrayList;
import java.util.List;

public class CartSingleton {
    private static CartSingleton instance;
    private List<Coffee> cart;

    private CartSingleton() {
        cart = new ArrayList<>();
    }

    public static CartSingleton getInstance() {
        if (instance == null) {
            instance = new CartSingleton();
        }
        return instance;
    }

    public List<Coffee> getCart() {
        return cart;
    }

    public void setCart(List<Coffee> cart) {
        this.cart = cart;
    }
}
