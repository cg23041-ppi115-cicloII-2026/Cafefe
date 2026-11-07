package sv.edu.ues.ppi115.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.ppi115.cafefe.control.PagoDetalleService;
import sv.edu.ues.ppi115.cafefe.control.PagoService;
import sv.edu.ues.ppi115.cafefe.entity.Pago;
import sv.edu.ues.ppi115.cafefe.entity.PagoDetalle;

@Path("pago/{id_pago}/detalle")
public class PagoDetalleResource implements Serializable {

    @Inject
    PagoDetalleService pagoDetalleService;

    @Inject
    PagoService pagoService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(@PathParam("id_pago") UUID idPago, PagoDetalle pagoDetalle,
            @Context UriInfo uriInfo) {
        if (pagoDetalle == null || idPago == null
                || pagoDetalle.getIdPagoDetalle() != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "El recurso no puede ser nulo, idPago no puede ser nulo y el recurso no debe tener un ID asignado").build();
        }
        try {
            Pago pago = pagoService.findById(idPago);
            if (pago == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Pago no encontrado").build();
            }
            pagoDetalle.setIdPagoDetalle(UUID.randomUUID());
            pagoDetalle.setIdPago(pago);
            pagoDetalleService.create(pagoDetalle);
            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder()
                    .path(pagoDetalle.getIdPagoDetalle().toString());
            return Response.created(uriBuilder.build()).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorRango(@PathParam("id_pago") UUID idPago,
            @QueryParam("first") @DefaultValue("0") int firstResult,
            @QueryParam("max") @DefaultValue("50") int maxResults) {
        if (idPago == null || firstResult < 0 || maxResults <= 0 || maxResults > 50) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "Los parámetros 'first' debe ser >= 0 y 'max' debe ser > 0 y <= 50").build();
        }
        try {
            List<PagoDetalle> registros = pagoDetalleService.findByPago(idPago, firstResult, maxResults);
            Long total = pagoDetalleService.contarPorPago(idPago);
            return Response.ok(registros).header(ResponseHeaders.TOTAL_REGISTROS.toString(), total).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorId(@PathParam("id_pago") UUID idPago, @PathParam("id") UUID id) {
        if (idPago == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "idPago e id no pueden ser nulos").build();
        }
        try {
            PagoDetalle encontrado = pagoDetalleService.findByIdYPago(id, idPago);
            if (encontrado != null && encontrado.getIdPago() != null
                    && idPago.equals(encontrado.getIdPago().getIdPago())) {
                return Response.ok(encontrado).build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado").build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
        }
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id_pago") UUID idPago, @PathParam("id") UUID id,
            PagoDetalle pagoDetalle) {
        if (pagoDetalle == null || idPago == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "El recurso o los identificadores no pueden ser nulos").build();
        }
        try {
            PagoDetalle existente = pagoDetalleService.findByIdYPago(id, idPago);
            if (existente == null || existente.getIdPago() == null
                    || !idPago.equals(existente.getIdPago().getIdPago())) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado").build();
            }
            pagoDetalle.setIdPagoDetalle(id);
            pagoDetalle.setIdPago(existente.getIdPago());
            pagoDetalleService.update(pagoDetalle);
            return Response.ok(pagoDetalle).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("id_pago") UUID idPago, @PathParam("id") UUID id) {
        if (idPago == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "idPago e id no pueden ser nulos").build();
        }
        try {
            PagoDetalle existente = pagoDetalleService.findByIdYPago(id, idPago);
            if (existente != null && existente.getIdPago() != null
                    && idPago.equals(existente.getIdPago().getIdPago())) {
                pagoDetalleService.delete(existente);
                return Response.noContent().build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado").build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
        }
    }

}
