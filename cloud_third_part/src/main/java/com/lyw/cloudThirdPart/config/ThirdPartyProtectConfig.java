package com.lyw.cloudThirdPart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class ThirdPartyProtectConfig {

    @Bean
    public DefaultRedisScript<Boolean> costAwareLimitScript() {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/cost_aware_limit.lua")));
        script.setResultType(Boolean.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Boolean> behaviorAnalysisScript() {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/behavior_analysis.lua")));
        script.setResultType(Boolean.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Boolean> budgetLimitScript() {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/budget_limit.lua")));
        script.setResultType(Boolean.class);
        return script;
    }
}
