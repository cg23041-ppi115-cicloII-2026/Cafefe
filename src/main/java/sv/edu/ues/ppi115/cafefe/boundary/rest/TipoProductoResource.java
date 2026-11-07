package sv.edu.ues.ppi115.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
import sv.edu.ues.ppi115.cafefe.control.TipoProductoService;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

@Path("tipoproducto")
public class TipoProductoResource implements Serializable {

    @Inject
    TipoProductoService tipoProductoService;

    /**
     * GET /tipoproducto?first=0&max=50
     * Devuelve un rango de registros y el total en el header Total-registros.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") @Max(50) int max) {
        try {
            if (first >= 0 && max > 0 && max <= 50) {
                List<TipoProducto> tipoProductos = tipoProductoService.findRange(first, max);
                Long total = tipoProductoService.contar();
                Response.ResponseBuilder responseBuilder = Response.ok(tipoProductos)
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
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    /**
     * GET /tipoproducto/{id}
     * Devuelve un registro especifico, o 404 si no existe.
     */
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                TipoProducto encontrado = tipoProductoService.findById(id);
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
     * POST /tipoproducto
     * Crea un registro. Responde 201 con el header Location del nuevo recurso.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(TipoProducto nuevo, @Context UriInfo uriInfo) {
        if (nuevo != null && nuevo.getIdTipoProducto() == null) {
            try {
                nuevo.setIdTipoProducto(UUID.randomUUID());
                tipoProductoService.create(nuevo);
                URI location = uriInfo.getAbsolutePathBuilder()
                        .path(String.valueOf(nuevo.getIdTipoProducto()))
                        .build();
                return Response.created(location).build();   // 201
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
                TipoProducto encontrado = tipoProductoService.findById(id);
                if (encontrado != null) {
                    tipoProductoService.delete(encontrado);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND).header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado").build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).header(ResponseHeaders.WRONG_PARAMETER.toString(), "El ID no puede ser nulo").build();
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") UUID id, TipoProducto nuevo) {
        if (nuevo != null && id != null) {
            try {
                TipoProducto existente = tipoProductoService.findById(id);
                if (existente != null) {
                    nuevo.setIdTipoProducto(id);
                    tipoProductoService.update(nuevo);
                    return Response.ok(nuevo).build();
                }
                return Response.status(Response.Status.NOT_FOUND).header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado").build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).header(ResponseHeaders.WRONG_PARAMETER.toString(), "El recurso no puede ser nulo y debe tener un ID asignado").build();
    }
}
