package org.ppj.pp.event.core.controller;

import org.ppj.pp.event.core.xxljob.biz.impl.ExecutorBizImpl;
import org.ppj.pp.event.core.xxljob.biz.model.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class XxlJobClientController {

    @PostMapping("/beat")
    public ReturnT<String> beat() {
        return new ExecutorBizImpl().beat();
    }

    @PostMapping("/idleBeat")
    public ReturnT<String> idleBeat(@RequestBody IdleBeatParam param) {
        return new ExecutorBizImpl().idleBeat(param);
    }

    @PostMapping("/run")
    public ReturnT<String> run(@RequestBody TriggerParam param) {
        return new ExecutorBizImpl().run(param);
    }

    @PostMapping("/kill")
    public ReturnT<String> kill(@RequestBody KillParam param) {
        return new ExecutorBizImpl().kill(param);
    }

    @PostMapping("/log")
    public ReturnT<LogResult> log(@RequestBody LogParam param) {
        return new ExecutorBizImpl().log(param);
    }
}
