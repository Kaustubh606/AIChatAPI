package com.aichatapp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class TimeTools {
    private static final Logger logger = LoggerFactory.getLogger(TimeTools.class);


    @Tool(name="getCurrentLocalTime", description = "Get the current time in user's timezone")
    String getCurrentLocalTime(){
        logger.info("in method getCurrentLocalTime - Returning the current time in user's timezone");
        return LocalTime.now().toString();
    }

    @Tool(name="getCurrentTime", description ="Get current time in the specified time zone.")
    public String getCurrentTime(@ToolParam(description = "Value representing the timezone") String timeZone){
        logger.info("in method getCurrentTime - Returning the current time in provided timezone {}",timeZone);
        return LocalTime.now(ZoneId.of(timeZone)).toString();

    }
}
