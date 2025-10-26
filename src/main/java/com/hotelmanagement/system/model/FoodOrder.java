package com.hotelmanagement.system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "food_orders")
public class FoodOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToMany
    @JoinTable(
            name = "food_order_items",
            joinColumns = @JoinColumn(name = "food_order_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    private List<MenuItem> items;

    // ✅ ADD THIS FIELD FOR TRACKING QUANTITIES
    @ElementCollection
    @CollectionTable(name = "food_order_quantities",
            joinColumns = @JoinColumn(name = "food_order_id"))
    @MapKeyColumn(name = "menu_item_id")
    @Column(name = "quantity")
    private Map<Long, Integer> itemQuantities = new HashMap<>();

    private double totalPrice;
    private String status;
    private LocalDateTime orderedAt;

    // Existing Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    // ✅ ADD THESE NEW GETTERS AND SETTERS FOR QUANTITIES
    public Map<Long, Integer> getItemQuantities() {
        return itemQuantities;
    }

    public void setItemQuantities(Map<Long, Integer> itemQuantities) {
        this.itemQuantities = itemQuantities;
    }
}