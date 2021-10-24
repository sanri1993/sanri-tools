package com.sanri.tools.modules.codepatch.controller.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupRepository {
    private String group;
    private List<String> repositorys = new ArrayList<>();

    public GroupRepository() {
    }

    public GroupRepository(String group, List<String> repositorys) {
        this.group = group;
        this.repositorys = repositorys;
    }
}
