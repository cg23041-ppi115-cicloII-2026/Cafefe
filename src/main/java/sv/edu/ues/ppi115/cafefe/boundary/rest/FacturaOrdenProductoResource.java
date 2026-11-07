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
import sv.edu.ues.ppi115.cafefe.control.FacturaOrdenProductoService;
import sv.edu.ues.ppi115.cafefe.control.FacturaService;
import sv.edu.ues.ppi115.cafefe.control.OrdenProductoService;
import sv.edu.ues.ppi115.cafefe.entity.Factura;
import sv.edu.ues.ppi115.cafefe.entity.FacturaOrdenProducto;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;

@Path("factura/{id_factura}/ordenproducto")
public class FacturaOrdenProductoResource implements Serializable {

    @Inject
    FacturaOrdenProductoService facturaOrdenProductoService;

    @Inject
    FacturaService facturaService;

    @Inject
    OrdenProductoService ordenProductoService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(@PathParam("id_factura") UUID idFactura,
            FacturaOrdenProducto facturaOrdenProducto, @Context UriInfo uriInfo) {
        if (facturaOrdenProducto == null || idFactura == null
                || facturaOrdenProducto.getIdFacturaOrdenProducto() != null
                || facturaOrdenProducto.getIdOrdenProducto() == null
                || facturaOrdenProducto.getIdOrdenProducto().getIdOrdenProducto() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "El recurso no puede ser nulo, idFactura e idOrdenProducto no pueden ser nulos y el recurso no debe tener un ID asignado").build();
        }
        try {
            Factura factura = facturaService.findById(idFactura);
            if (factura == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Factura no encontrada").build();
            }
            OrdenProducto ordenProducto = ordenProductoService.findById(
                    facturaOrdenProducto.getIdOrdenProducto().getIdOrdenProducto());
            if (ordenProducto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "OrdenProducto no encontrado").build();
            }
            facturaOrdenProducto.setIdFacturaOrdenProducto(UUID.randomUUID());
            facturaOrdenProducto.setIdFactura(factura);
            facturaOrdenProducto.setIdOrdenProducto(ordenProducto);
            facturaOrdenProductoService.create(facturaOrdenProducto);
            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder()
                    .path(facturaOrdenProducto.getIdFacturaOrdenProducto().toString());
            return Response.created(uriBuilder.build()).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorRango(@PathParam("id_factura") UUID idFactura,
            @QueryParam("first") @DefaultValue("0") int firstResult,
            @QueryParam("max") @DefaultValue("50") int maxResults) {
        if (idFactura == null || firstResult < 0 || maxResults <= 0 || maxResults > 50) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "Los parámetros 'first' debe ser >= 0 y 'max' debe ser > 0 y <= 50").build();
        }
        try {
            List<FacturaOrdenProducto> registros = facturaOrdenProductoService
                    .findByFactura(idFactura, firstResult, maxResults);
            Long total = facturaOrdenProductoService.contarPorFactura(idFactura);
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
    public Response buscarPorId(@PathParam("id_factura") UUID idFactura, @PathParam("id") UUID id) {
        if (idFactura == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "idFactura e id no pueden ser nulos").build();
        }
        try {
            FacturaOrdenProducto encontrado = facturaOrdenProductoService.findByIdYFactura(id, idFactura);
            if (encontrado != null && encontrado.getIdFactura() != null
                    && idFactura.equals(encontrado.getIdFactura().getIdFactura())) {
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
    public Response actualizar(@PathParam("id_factura") UUID idFactura, @PathParam("id") UUID id,
            FacturaOrdenProducto facturaOrdenProducto) {
        if (facturaOrdenProducto == null || idFactura == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "El recurso o los identificadores no pueden ser nulos").build();
        }
        try {
            FacturaOrdenProducto existente = facturaOrdenProductoService.findByIdYFactura(id, idFactura);
            if (existente == null || existente.getIdFactura() == null
                    || !idFactura.equals(existente.getIdFactura().getIdFactura())) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado").build();
            }
            if (facturaOrdenProducto.getIdOrdenProducto() == null
                    || facturaOrdenProducto.getIdOrdenProducto().getIdOrdenProducto() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idOrdenProducto no puede ser nulo").build();
            }
            OrdenProducto ordenProducto = ordenProductoService.findById(
                    facturaOrdenProducto.getIdOrdenProducto().getIdOrdenProducto());
            if (ordenProducto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "OrdenProducto no encontrado").build();
            }
            facturaOrdenProducto.setIdFacturaOrdenProducto(id);
            facturaOrdenProducto.setIdFactura(existente.getIdFactura());
            facturaOrdenProducto.setIdOrdenProducto(ordenProducto);
            facturaOrdenProductoService.update(facturaOrdenProducto);
            return Response.ok(facturaOrdenProducto).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("id_factura") UUID idFactura, @PathParam("id") UUID id) {
        if (idFactura == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "idFactura e id no pueden ser nulos").build();
        }
        try {
            FacturaOrdenProducto existente = facturaOrdenProductoService.findByIdYFactura(id, idFactura);
            if (existente != null && existente.getIdFactura() != null
                    && idFactura.equals(existente.getIdFactura().getIdFactura())) {
                facturaOrdenProductoService.delete(existente);
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
