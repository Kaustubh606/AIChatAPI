package com.aichatapp.tools;

import com.aichatapp.entity.HelpDeskTicket;
import com.aichatapp.model.TicketRequest;
import com.aichatapp.service.HelpDeskTicketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HelpDeskTools {
    private static final Logger logger = LoggerFactory.getLogger(HelpDeskTools.class);
    private final HelpDeskTicketService service;

    @Tool(name = "createTicket", description = "Create the support ticket", returnDirect = true)
    String createTicket(@ToolParam(description = "Details to create a support ticket")
                        TicketRequest req, ToolContext toolContext){
        String username = (String) toolContext.getContext().get("username");
        logger.info("Creating support ticket for user: {} with details: {}",username,req);
        HelpDeskTicket savedTicket = service.createTicket(req,username);
        logger.info("Ticket created successfully. Ticket ID: {}, Username: {}",savedTicket.getId(),savedTicket.getUsername());
        return "Ticket #"+savedTicket.getId()+" created successfully for the user "+savedTicket.getUsername();
    }

    @Tool(name = "fetchTickets", description = "Fetch the status of the tickets based on a given username")
    List<HelpDeskTicket> getTicketStatus(ToolContext toolContext){
        String username = (String) toolContext.getContext().get("username");
        logger.info("Fetching support tickets for user: {}",username);
        List<HelpDeskTicket> tickets = service.getTicketByUsername(username);
        logger.info("Found {} ticket(s) for user: {}",tickets.size(),username);
        //Exception Demo - it will be sent to LLM by default
        //throw new RuntimeException("Unable to fetch ticket status.");
        return tickets;
    }

}
