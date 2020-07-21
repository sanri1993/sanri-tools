package com.sanri.tools.modules.redis;

import com.sanri.tools.modules.core.service.file.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    @Autowired
    FileManager fileManager;

}
