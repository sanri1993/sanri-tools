package com.sanri.tools.modules.versioncontrol.git.dtos;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class Branches {
    private List<Branch> branches = new ArrayList<>();
    private String currentBranchName;


    public Branches() {
    }

    public Branches(List<Branch> branches, String currentBranchName) {
        this.branches = branches;
        this.currentBranchName = currentBranchName;
    }

    @Data
    public static final class Branch {
        private boolean local;
        private String name;
        private String objectId;
        private String branchName;

        public Branch() {
        }

        public Branch(String name,String objectId) {
            this.name = name;
            this.objectId = objectId;

            if (name.contains("refs/heads")){
                local = true;
                this.branchName = name.substring("refs/heads/".length());
            }else{
                try {
                    this.branchName = name.substring("refs/remotes/".length());
                }catch (Exception e){
                    log.error("名称截取异常, 使用原来名称: {}",name);
                    this.branchName = name;
                }
            }
        }

    }
}
