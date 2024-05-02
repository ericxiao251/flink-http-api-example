package org.example;

// Use this tool to convert JSON to POJO: https://www.jsonschema2pojo.org/

public class Pokemon {

    String name;

    int order;

    int weight;

    public Pokemon(String name, int order, int weight) {
        this.name = name;
        this.order = order;
        this.weight = weight;
    }
}
