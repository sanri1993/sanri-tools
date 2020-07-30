package com.sanri.tools.modules.name.service;

import com.sanri.tools.modules.core.dtos.PluginDto;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.protocol.exception.ToolException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class NameService {

    // 分词器
    @Autowired(required = false)
    private List<SplitWord> splitWords = new ArrayList<>();

    // 翻译工具, 包含业务翻译,英语翻译
    @Autowired(required = false)
    private List<Translate> translates = new ArrayList<>();

    @Autowired(required = false)
    private List<CharHandler> charHandlers = new ArrayList<>();

    @Autowired
    private PluginManager pluginManager;

    /**
     * 变量或方法取名
     * @param orginChars
     * @param splitToolName
     * @param tranlates
     * @return
     */
    public Set<String> translate(String orginChars, String splitToolName, List<String> tranlates){
        TranslateCharSequence translateCharSequence = new TranslateCharSequence(orginChars);

        // 先找到使用的分词器,进行分词
        firstSplitWord(splitToolName, translateCharSequence);

        // 然后使用翻译工具进行翻译; 找到需要使用的翻译工具; 这里包含业务词和通用词,最后才是英语翻译
        secondTranslate(tranlates, translateCharSequence);

        // 词拼接及后续处理
        thridCharHandlerAndMerge(translateCharSequence);

        // 得出结论
        Set<String> results = translateCharSequence.results();

        return results;
    }

    private void thridCharHandlerAndMerge(TranslateCharSequence translateCharSequence) {
        if(CollectionUtils.isNotEmpty(charHandlers)) {
            for (CharHandler charHandler : charHandlers) {
                charHandler.handler(translateCharSequence);
            }
        }
    }

    private void secondTranslate(List<String> tranlates, TranslateCharSequence translateCharSequence) {
        List<Translate> findtranslates = new ArrayList<>();
        for (Translate translate : translates) {
            if (tranlates.contains(translate.getName())){
                findtranslates.add(translate);
            }
        }

        // 用找到的翻译工具做翻译
        for (Translate translate : findtranslates) {
            translate.doTranslate(translateCharSequence);

            // 如果是英语翻译工具,再增加直译
            if (translate instanceof  EnglishTranslate){
                EnglishTranslate englishTranslate = (EnglishTranslate) translate;
                Set<String> results = englishTranslate.directTranslate(translateCharSequence.getOriginSequence().toString());
                for (String result : results) {
                    translateCharSequence.addDirectTranslate(result);
                }
            }
        }
    }

    private void firstSplitWord(String splitToolName, TranslateCharSequence translateCharSequence) {
        if (CollectionUtils.isEmpty(splitWords)){
            throw new ToolException("未找到可用的分词器");
        }
        SplitWord splitTool = null;
        for (SplitWord tool : splitWords) {
            String name = tool.getName();
            if (name.equals(splitToolName)){
                splitTool = tool;
                break;
            }
        }
        if (splitTool == null){
            throw new ToolException("找不到分词器:"+splitToolName);
        }
        splitTool.doSplit(translateCharSequence);
    }

    @PostConstruct
    public void register(){
        pluginManager.register(PluginDto.builder().module(BizTranslate.module).name("main").build());
    }
}
