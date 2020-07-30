package com.sanri.tools.modules.name.controller;

import com.sanri.tools.modules.name.service.BizTranslate;
import com.sanri.tools.modules.name.service.NameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/name")
public class NameController {
    @Autowired
    private BizTranslate bizTranslate;

    @Autowired
    private NameService nameService;

    @GetMapping("/translate")
    public Set<String> translate(String orginChars, String splitToolName, String[] tranlates){
        return nameService.translate(orginChars,splitToolName, Arrays.asList(tranlates));
    }

    @GetMapping("/bizs")
    public List<String> bizs(){
        return bizTranslate.bizs();
    }

    @GetMapping("/detail/{biz}")
    public List<String> bizMirrors(@PathVariable("biz") String biz) throws IOException {
        return bizTranslate.mirrors(biz);
    }

    @PostMapping("/mirror/write")
    public void writeMirror(String biz,String content) throws IOException {
        bizTranslate.writeMirror(biz,content);
    }
}
