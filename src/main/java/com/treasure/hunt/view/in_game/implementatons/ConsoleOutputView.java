package com.treasure.hunt.view.in_game.implementatons;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.view.in_game.View;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ConsoleOutputView implements View<Product> {

    private GameHistory gameHistory;
    private int pos = 0;

    public void draw(Product product) {
        //product.getGeometryItems().forEach(geometryItem -> log.info(geometryItem.getObject().toString()));
        log.info(product.getGeometryItems().toString());
    }

    @Override
    public void run() {
        while (true) {
            List<Product> products = null;
            try {
                products = gameHistory.giveNewProducts(pos);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            products.forEach(product -> draw(product));
            pos += products.size();

        }
    }

    @Override
    public void init(GameHistory gameHistory) {
        this.gameHistory = gameHistory;
    }
}
