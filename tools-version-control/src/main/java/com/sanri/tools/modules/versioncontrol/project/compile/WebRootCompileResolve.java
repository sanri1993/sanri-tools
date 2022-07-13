package com.sanri.tools.modules.versioncontrol.project.compile;

import com.sanri.tools.modules.core.utils.OnlyPath;
import com.sanri.tools.modules.versioncontrol.dtos.CompileFiles;
import com.sanri.tools.modules.versioncontrol.git.dtos.DiffChanges;
import com.sanri.tools.modules.core.dtos.RelativeFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
@Order(1000)
public class WebRootCompileResolve implements CompileResolve {

    public static final OnlyPath WEB_ROOT = new OnlyPath("src/main/webapp");

    @Override
    public CompileFiles.DiffCompileFile resolve(DiffChanges.DiffFile diffFile, RelativeFile modulePath,OnlyPath projectPath) {
        final CompileFiles.DiffCompileFile diffCompileFile = new CompileFiles.DiffCompileFile(diffFile,modulePath,projectPath);
        diffCompileFile.setCompileFiles(Arrays.asList(diffFile.getRelativeFile()));
        return diffCompileFile;
    }

    @Override
    public boolean support(DiffChanges.DiffFile diffFile) {
        final String relativePath = diffFile.path();
        return new OnlyPath(relativePath).startsWith(WEB_ROOT);
    }
}
