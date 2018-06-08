package za.cbsa.integration.connectors.hsm.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 *
 * @author deepak
 */

@Path("/healthservice")
public class HealthController {
    
    @Path("echo")
    @GET
    public Response sendEchoResponse(){
        System.out.println("***Received Echo Request");
        
        return Response.status(200).entity("Health Service UP").build();
        
    }
    
}
