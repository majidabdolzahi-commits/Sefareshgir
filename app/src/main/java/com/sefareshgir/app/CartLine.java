package com.sefareshgir.app;

public class CartLine {

    public long productId;
    public String name;
    public int qty;
    public long price;
    public long buy;

    public CartLine(
            long id,
            String name,
            int qty,
            long price,
            long buy) {

        this.productId = id;
        this.name = name;
        this.qty = qty;
        this.price = price;
        this.buy = buy;
    }
}
