package com.sanri.tools.modules.database.service.meta.dtos;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Catalog {
    private String catalog;
    private List<String> schemas = new ArrayList<>();

    public Catalog() {
    }

    public Catalog(String catalog) {
        this.catalog = catalog;
    }

    public Catalog(String catalog, List<String> schemas) {
        this.catalog = catalog;
        this.schemas = schemas;
    }
}
