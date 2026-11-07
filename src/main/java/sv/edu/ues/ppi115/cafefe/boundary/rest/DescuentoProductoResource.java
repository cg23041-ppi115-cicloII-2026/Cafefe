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
import sv.edu.ues.ppi115.cafefe.control.DescuentoProductoService;
import sv.edu.ues.ppi115.cafefe.control.DescuentoService;
import sv.edu.ues.ppi115.cafefe.control.ProductoService;
import sv.edu.ues.ppi115.cafefe.entity.Descuento;
import sv.edu.ues.ppi115.cafefe.entity.DescuentoProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

@Path("descuento/{id_descuento}/producto")
public class DescuentoProductoResource implements Serializable {

    @Inject
    DescuentoProductoService descuentoProductoService;

    @Inject
    DescuentoService descuentoService;

    @Inject
    ProductoService productoService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(
            @PathParam("id_descuento") UUID idDescuento,
            DescuentoProducto descuentoProducto,
            @Context UriInfo uriInfo) {
        if (descuentoProducto == null || idDescuento == null
                || descuentoProducto.getIdDescuentoProducto() != null
                || descuentoProducto.getIdProducto() == null
                || descuentoProducto.getIdProducto().getIdProducto() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "El recurso no puede ser nulo, idDescuento e idProducto no pueden ser nulos y el recurso no debe tener un ID asignado")
                    .build();
        }
        try {
            Descuento descuento = descuentoService.findById(idDescuento);
            if (descuento == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Descuento no encontrado")
                        .build();
            }
            Producto producto = productoService.findById(descuentoProducto.getIdProducto().getIdProducto());
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Producto no encontrado").build();
            }
            descuentoProducto.setIdDescuentoProducto(UUID.randomUUID());
            descuentoProducto.setIdDescuento(descuento);
            descuentoProducto.setIdProducto(producto);
            descuentoProductoService.create(descuentoProducto);
            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder()
                    .path(descuentoProducto.getIdDescuentoProducto().toString());
            return Response.created(uriBuilder.build()).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorRango(
            @PathParam("id_descuento") UUID idDescuento,
            @QueryParam("first") @DefaultValue("0") int firstResult,
            @QueryParam("max") @DefaultValue("50") int maxResults) {
        if (idDescuento == null || firstResult < 0 || maxResults <= 0 || maxResults > 50) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "Los parámetros 'first' debe ser >= 0 y 'max' debe ser > 0 y <= 50")
                    .build();
        }
        try {
            List<DescuentoProducto> registros = descuentoProductoService
                    .findByDescuento(idDescuento, firstResult, maxResults);
            Long total = descuentoProductoService.contarPorDescuento(idDescuento);
            return Response.ok(registros)
                    .header(ResponseHeaders.TOTAL_REGISTROS.toString(), total)
                    .build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorId(
            @PathParam("id_descuento") UUID idDescuento,
            @PathParam("id") UUID id) {
        if (id == null || idDescuento == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "idDescuento e id no pueden ser nulos")
                    .build();
        }
        try {
            DescuentoProducto encontrado = descuentoProductoService.findByIdYDescuento(id, idDescuento);
            if (encontrado != null && encontrado.getIdDescuento() != null
                    && idDescuento.equals(encontrado.getIdDescuento().getIdDescuento())) {
                return Response.ok(encontrado).build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado")
                    .build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(
            @PathParam("id_descuento") UUID idDescuento,
            @PathParam("id") UUID id,
            DescuentoProducto descuentoProducto) {
        if (descuentoProducto == null || idDescuento == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "El recurso o los identificadores no pueden ser nulos")
                    .build();
        }
        try {
            DescuentoProducto existente = descuentoProductoService.findByIdYDescuento(id, idDescuento);
            if (existente == null || existente.getIdDescuento() == null
                    || !idDescuento.equals(existente.getIdDescuento().getIdDescuento())) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado")
                        .build();
            }
            if (descuentoProducto.getIdProducto() == null
                    || descuentoProducto.getIdProducto().getIdProducto() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idProducto no puede ser nulo").build();
            }
            Producto producto = productoService.findById(descuentoProducto.getIdProducto().getIdProducto());
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Producto no encontrado").build();
            }
            descuentoProducto.setIdDescuentoProducto(id);
            descuentoProducto.setIdDescuento(existente.getIdDescuento());
            descuentoProducto.setIdProducto(producto);
            descuentoProductoService.update(descuentoProducto);
            return Response.ok(descuentoProducto).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(
            @PathParam("id_descuento") UUID idDescuento,
            @PathParam("id") UUID id) {
        if (id == null || idDescuento == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "idDescuento e id no pueden ser nulos")
                    .build();
        }
        try {
            DescuentoProducto existente = descuentoProductoService.findByIdYDescuento(id, idDescuento);
            if (existente != null && existente.getIdDescuento() != null
                    && idDescuento.equals(existente.getIdDescuento().getIdDescuento())) {
                descuentoProductoService.delete(existente);
                return Response.noContent().build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado")
                    .build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }
}
