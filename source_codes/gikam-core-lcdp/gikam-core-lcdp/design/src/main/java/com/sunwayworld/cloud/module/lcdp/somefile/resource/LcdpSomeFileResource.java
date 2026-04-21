package com.sunwayworld.cloud.module.lcdp.somefile.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/core/module/lcdp/some-files")
public interface LcdpSomeFileResource {
    @RequestMapping(method = RequestMethod.GET)
    String selectFiles();
}
