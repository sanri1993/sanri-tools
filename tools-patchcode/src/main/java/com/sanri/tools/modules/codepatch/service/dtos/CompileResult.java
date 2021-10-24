package com.sanri.tools.modules.codepatch.service.dtos;

import lombok.Data;

@Data
public class CompileResult {
    private int exitValue;
    private String result;

    public CompileResult() {
    }

    public CompileResult(int exitValue, String result) {
        this.exitValue = exitValue;
        this.result = result;
    }
}
