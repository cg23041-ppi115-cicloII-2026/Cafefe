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

import sv.edu.ues.ppi115.cafefe.control.OrdenProductoService;
import sv.edu.ues.ppi115.cafefe.control.OrdenService;
import sv.edu.ues.ppi115.cafefe.control.ProductoService;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;
import sv.edu.ues.ppi115.cafefe.entity.Producto;

@Path("orden/{id_orden}/producto")
public class OrdenProductoResource implements Serializable {

    @Inject
    OrdenProductoService ordenProductoService;

    @Inject
    OrdenService ordenService;

    @Inject
    ProductoService productoService;

    /**
     * POST /orden/{id_orden}/producto
     * Crea un producto asociado a una orden en específica.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(
            @PathParam("id_orden") UUID idOrden,
            OrdenProducto ordenProducto,
            @Context UriInfo uriInfo) {
        if (ordenProducto == null || idOrden == null
                || ordenProducto.getIdOrdenProducto() != null
                || ordenProducto.getIdProducto() == null
                || ordenProducto.getIdProducto().getIdProducto() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El recurso no puede ser nulo, idOrden e idProducto no pueden ser nulos y el recurso no debe tener un ID asignado")
                    .build();
        }
        try {
            Orden orden = ordenService.findById(idOrden);
            if (orden == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Orden no encontrada")
                        .build();
            }

            Producto producto = productoService.findById(ordenProducto.getIdProducto().getIdProducto());
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Producto no encontrado")
                        .build();
            }
            ordenProducto.setIdOrdenProducto(UUID.randomUUID());
            ordenProducto.setIdOrden(orden);
            ordenProducto.setIdProducto(producto);

            ordenProductoService.create(ordenProducto);

            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(ordenProducto.getIdOrdenProducto().toString());
            return Response.created(uriBuilder.build()).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    /**
     * GET /orden/{id_orden}/producto
     * Devuelve los productos asociados a una orden en un rango paginado.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorRango(
            @PathParam("id_orden") UUID idOrden,
            @QueryParam("first") @DefaultValue("0") int firstResult,
            @QueryParam("max") @DefaultValue("50") int maxResults) {
        if (idOrden == null || firstResult < 0 || maxResults <= 0 || maxResults > 50) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "Los parámetros 'first' debe ser >= 0 y 'max' debe ser > 0 y <= 50")
                    .build();
        }
        try {
            List<OrdenProducto> registros = ordenProductoService.findByOrden(idOrden, firstResult, maxResults);
            Long total = ordenProductoService.contarPorOrden(idOrden);

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

    /**
     * GET /orden/{id_orden}/producto/{id}
     * Devuelve una asignación de producto específica de una orden.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorId(
            @PathParam("id_orden") UUID idOrden,
            @PathParam("id") UUID id) {
        if (id == null || idOrden == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idOrden e id no pueden ser nulos")
                    .build();
        }
        try {
            OrdenProducto encontrado = ordenProductoService.findByIdYOrden(id, idOrden);
            if (encontrado != null && encontrado.getIdOrden() != null && idOrden.equals(encontrado.getIdOrden().getIdOrden())) {
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

    /**
     * PUT /orden/{id_orden}/producto/{id}
     * Actualiza un producto existente para la orden.
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(
            @PathParam("id_orden") UUID idOrden,
            @PathParam("id") UUID id,
            OrdenProducto ordenProducto) {
        if (ordenProducto == null || idOrden == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El recurso o los identificadores no pueden ser nulos")
                    .build();
        }
        try {
            OrdenProducto existente = ordenProductoService.findByIdYOrden(id, idOrden);
            if (existente == null || existente.getIdOrden() == null || !idOrden.equals(existente.getIdOrden().getIdOrden())) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado")
                        .build();
            }

            if (ordenProducto.getIdProducto() == null
                    || ordenProducto.getIdProducto().getIdProducto() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idProducto no puede ser nulo")
                        .build();
            }
            Producto producto = productoService.findById(ordenProducto.getIdProducto().getIdProducto());
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Producto no encontrado")
                        .build();
            }
            ordenProducto.setIdOrdenProducto(id);
            ordenProducto.setIdOrden(existente.getIdOrden());
            ordenProducto.setIdProducto(producto);

            ordenProductoService.update(ordenProducto);
            return Response.ok(ordenProducto).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /orden/{id_orden}/producto/{id}
     * Elimina el producto de una orden.
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(
            @PathParam("id_orden") UUID idOrden,
            @PathParam("id") UUID id) {
        if (id == null || idOrden == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idOrden e id no pueden ser nulos")
                    .build();
        }
        try {
            OrdenProducto existente = ordenProductoService.findByIdYOrden(id, idOrden);
            if (existente != null && existente.getIdOrden() != null && idOrden.equals(existente.getIdOrden().getIdOrden())) {
                ordenProductoService.delete(existente);
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