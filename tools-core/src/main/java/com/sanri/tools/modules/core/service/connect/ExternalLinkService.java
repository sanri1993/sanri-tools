package com.sanri.tools.modules.core.service.connect;

import com.sanri.tools.modules.core.service.file.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalLinkService {

    @Autowired
    private FileManager fileManager;


}
