package com.smartmovesystems.keycloak.firebasemigrator

import com.smartmovesystems.keycloak.firebasemigrator.model.ScryptHashParametersRepresentation
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/scrypt/parameters")
interface HashParametersResource {

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    fun createHashParameters(rep: ScryptHashParametersRepresentation?): Response?

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAllParameters(): List<ScryptHashParametersRepresentation?>?
}