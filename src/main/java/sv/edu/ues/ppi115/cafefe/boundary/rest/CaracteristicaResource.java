package sv.edu.ues.ppi115.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.ppi115.cafefe.control.CaracteristicaService;
import sv.edu.ues.ppi115.cafefe.control.TipoCaracteristicaService;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;
import sv.edu.ues.ppi115.cafefe.entity.TipoCaracteristica;

@Path("caracteristica")
public class CaracteristicaResource implements Serializable {

    @Inject
    CaracteristicaService caracteristicaService;

    @Inject
    TipoCaracteristicaService tipoCaracteristicaService;

    /**
     * Devuelve un rango de registros y el total en el header Total-registros.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        try {
            if (first >= 0 && max > 0 && max <= 50) {
                List<Caracteristica> caracteristicas = caracteristicaService.findRange(first, max);
                Long total = caracteristicaService.contar();
                Response.ResponseBuilder responseBuilder = Response.ok(caracteristicas)
                        .header(ResponseHeaders.TOTAL_REGISTROS.toString(), total)
                        .type(MediaType.APPLICATION_JSON);
                return responseBuilder.build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                                "Los parámetros 'first' debe ser >= 0 y 'max' debe ser > 0 y <= 50")
                        .build();
            }
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    /**
     * GET /caracteristica/{id}
     * Devuelve un registro especifico, o 404 si no existe.
     */
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                Caracteristica encontrado = caracteristicaService.findById(id);
                if (encontrado != null) {
                    return Response.ok(encontrado)
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "id: " + id)
                        .build();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                        .build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .header(ResponseHeaders.WRONG_PARAMETER.toString(), "id: " + id)
                .build();
    }

    /**
     * POST /caracteristica
     * Crea un registro. Responde 201 con el header Location del nuevo recurso.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Caracteristica nuevo, @Context UriInfo uriInfo) {
        if (nuevo != null && nuevo.getIdCaracteristica() == null) {
            try {
                // Verificar e incorporar la entidad TipoCaracteristica persistida
                if (nuevo.getIdTipoCaracteristica() != null && nuevo.getIdTipoCaracteristica().getIdTipoCaracteristica() != null) {
                    UUID idTipo = nuevo.getIdTipoCaracteristica().getIdTipoCaracteristica();
                    TipoCaracteristica tipoEncontrado = tipoCaracteristicaService.findById(idTipo);

                    if (tipoEncontrado == null) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El TipoCaracterística con ID " + idTipo + " no existe")
                                .build();
                    }
                    nuevo.setIdTipoCaracteristica(tipoEncontrado);
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header(ResponseHeaders.WRONG_PARAMETER.toString(), "Debe proporcionar el idTipoCaracteristica")
                            .build();
                }

                nuevo.setIdCaracteristica(UUID.randomUUID());
                caracteristicaService.create(nuevo);
                URI location = uriInfo.getAbsolutePathBuilder()
                        .path(String.valueOf(nuevo.getIdCaracteristica()))
                        .build();
                return Response.created(location).build(); // 201
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                        .build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El recurso no puede ser nulo y no debe tener un ID asignado")
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                Caracteristica encontrado = caracteristicaService.findById(id);
                if (encontrado != null) {
                    caracteristicaService.delete(encontrado);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                        .build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El ID no puede ser nulo")
                .build();
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") UUID id, Caracteristica nuevo) {
        if (nuevo != null && id != null) {
            try {
                Caracteristica existente = caracteristicaService.findById(id);
                if (existente == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado")
                            .build();
                }

                // Verificar e incorporar la entidad TipoCaracteristica persistida
                if (nuevo.getIdTipoCaracteristica() != null && nuevo.getIdTipoCaracteristica().getIdTipoCaracteristica() != null) {
                    UUID idTipo = nuevo.getIdTipoCaracteristica().getIdTipoCaracteristica();
                    TipoCaracteristica tipoEncontrado = tipoCaracteristicaService.findById(idTipo);

                    if (tipoEncontrado == null) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El TipoCaracterística con ID " + idTipo + " no existe")
                                .build();
                    }
                    nuevo.setIdTipoCaracteristica(tipoEncontrado);
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header(ResponseHeaders.WRONG_PARAMETER.toString(), "Debe proporcionar el idTipoCaracteristica")
                            .build();
                }

                nuevo.setIdCaracteristica(id);
                caracteristicaService.update(nuevo);
                return Response.ok(nuevo).build();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                        .build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El recurso no puede ser nulo y debe tener un ID asignado")
                .build();
    }
}