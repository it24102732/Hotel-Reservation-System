package com.hotelmanagement.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/menu")
    public String menuPage() {
        return "menu";
    }

    @GetMapping("/index.html")
    public String indexPage() {
        return "index";
    }


    @GetMapping("/food-ordering")
    public String foodOrdering() {
        return "food-ordering";
    }

    @GetMapping("/food-ordering.html")
    public String foodOrderingHtml() {
        return "food-ordering";
    }

    @GetMapping("/order-online")
    public String orderOnline() {
        return "food-ordering";
    }

    @GetMapping("/search-reservation")
    public String searchReservation() {
        return "search-reservation";
    }

    @GetMapping("/search-reservation.html")
    public String searchReservationHtml() {
        return "search-reservation";
    }

    @GetMapping("/error")
    public String handleError() {
        return "error";
    }

    @GetMapping("/home")
    public String home() {
        return "redirect:/";
    }
}