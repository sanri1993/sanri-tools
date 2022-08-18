package com.sanri.tools.modules.proxy.controller.dtos;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SendRequestParam {
   private String connName;
   private String reqId;
   private Map<String,String> params = new HashMap<>();
}
