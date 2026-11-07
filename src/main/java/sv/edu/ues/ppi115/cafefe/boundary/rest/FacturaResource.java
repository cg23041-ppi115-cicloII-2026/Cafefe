package sv.edu.ues.ppi115.cafefe.boundary.rest;
 
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
import sv.edu.ues.ppi115.cafefe.control.FacturaService;
import sv.edu.ues.ppi115.cafefe.control.CajaService;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolService;
import sv.edu.ues.ppi115.cafefe.entity.Caja;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Factura;
 
@Path("factura")
public class FacturaResource implements Serializable {
 
    @Inject
    FacturaService facturaService;

    @Inject
    CajaService cajaService;

    @Inject
    EmpleadoRolService empleadoRolService;
 
    /**
     * GET /factura?first=0&max=50  
     * Devuelve un rango de registros y el total en el header Total-registros.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        try {
            if (first >= 0 && max > 0 && max <= 50) {
                List<Factura> facturas = facturaService.findRange(first, max);
                Long total = facturaService.contar();
                Response.ResponseBuilder responseBuilder = Response.ok(facturas)
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
     * GET /factura/{id}
     * Devuelve un registro especifico, o 404 si no existe.
     */
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                Factura encontrado = facturaService.findById(id);
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
     * POST /factura
     * Crea un registro. Responde 201 con el header Location del nuevo recurso.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Factura nuevo, @Context UriInfo uriInfo) {
        if (nuevo != null && nuevo.getIdFactura() == null) {
            try {
                if (nuevo.getIdCaja() != null && nuevo.getIdCaja().getIdCaja() != null) {
                    UUID idCaja = nuevo.getIdCaja().getIdCaja();
                    Caja cajaEncontrada = cajaService.findById(idCaja);
                    if (cajaEncontrada == null) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .header(ResponseHeaders.WRONG_PARAMETER.toString(), "La Caja con ID " + idCaja + " no existe")
                                .build();
                    }
                    nuevo.setIdCaja(cajaEncontrada);
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header(ResponseHeaders.WRONG_PARAMETER.toString(), "Debe proporcionar el idCaja")
                            .build();
                }
                if (nuevo.getIdEmpleadoRol() != null && nuevo.getIdEmpleadoRol().getIdEmpleadoRol() != null) {
                    UUID idEmpleadoRol = nuevo.getIdEmpleadoRol().getIdEmpleadoRol();
                    EmpleadoRol empleadoRolEncontrado = empleadoRolService.findById(idEmpleadoRol);
                    if (empleadoRolEncontrado == null) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El EmpleadoRol con ID " + idEmpleadoRol + " no existe")
                                .build();
                    }
                    nuevo.setIdEmpleadoRol(empleadoRolEncontrado);
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header(ResponseHeaders.WRONG_PARAMETER.toString(), "Debe proporcionar el idEmpleadoRol")
                            .build();
                }
                nuevo.setIdFactura(UUID.randomUUID());
                facturaService.create(nuevo);
                URI location = uriInfo.getAbsolutePathBuilder()
                        .path(String.valueOf(nuevo.getIdFactura()))
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
                Factura encontrado = facturaService.findById(id);
                if (encontrado != null) {
                    facturaService.delete(encontrado);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND).header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado").build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST)        .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El ID no puede ser nulo").build();
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") UUID id, Factura nuevo) {
        if (nuevo != null && id != null) {
            try {
                Factura existente = facturaService.findById(id);
                if (existente != null) {
                    if (nuevo.getIdCaja() != null && nuevo.getIdCaja().getIdCaja() != null) {
                        UUID idCaja = nuevo.getIdCaja().getIdCaja();
                        Caja cajaEncontrada = cajaService.findById(idCaja);
                        if (cajaEncontrada == null) {
                            return Response.status(Response.Status.BAD_REQUEST)
                                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "La Caja con ID " + idCaja + " no existe")
                                    .build();
                        }
                        nuevo.setIdCaja(cajaEncontrada);
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .header(ResponseHeaders.WRONG_PARAMETER.toString(), "Debe proporcionar el idCaja")
                                .build();
                    }
                    if (nuevo.getIdEmpleadoRol() != null && nuevo.getIdEmpleadoRol().getIdEmpleadoRol() != null) {
                        UUID idEmpleadoRol = nuevo.getIdEmpleadoRol().getIdEmpleadoRol();
                        EmpleadoRol empleadoRolEncontrado = empleadoRolService.findById(idEmpleadoRol);
                        if (empleadoRolEncontrado == null) {
                            return Response.status(Response.Status.BAD_REQUEST)
                                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El EmpleadoRol con ID " + idEmpleadoRol + " no existe")
                                    .build();
                        }
                        nuevo.setIdEmpleadoRol(empleadoRolEncontrado);
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .header(ResponseHeaders.WRONG_PARAMETER.toString(), "Debe proporcionar el idEmpleadoRol")
                                .build();
                    }
                    nuevo.setIdFactura(id);
                    facturaService.update(nuevo);
                    return Response.ok(nuevo).build();
                }
                return Response.status(Response.Status.NOT_FOUND).header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado").build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)                .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST)        .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El recurso no puede ser nulo y debe tener un ID asignado").build();
    }
}
 