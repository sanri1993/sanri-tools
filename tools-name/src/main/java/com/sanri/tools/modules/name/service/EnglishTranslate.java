package com.sanri.tools.modules.name.service;

import java.util.Set;

public interface EnglishTranslate {
    /**
     * 直译,不用拆词
     * @param source
     * @return
     */
    public Set<String> directTranslate(String source);
}
