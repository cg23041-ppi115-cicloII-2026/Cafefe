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

import sv.edu.ues.ppi115.cafefe.control.ProductoCaracteristicaService;
import sv.edu.ues.ppi115.cafefe.control.ProductoService;
import sv.edu.ues.ppi115.cafefe.control.CaracteristicaService;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;

@Path("producto/{id_producto}/caracteristica")
public class ProductoCaracteristicaResource implements Serializable {

    @Inject
    ProductoCaracteristicaService productoCaracteristicaService;

    @Inject
    ProductoService productoService;

    @Inject
    CaracteristicaService caracteristicaService;

    /**
     * POST /producto/{id_producto}/caracteristica
     * Crea una característica asociada a un producto en específico.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(
            @PathParam("id_producto") UUID idProducto,
            ProductoCaracteristica productoCaracteristica,
            @Context UriInfo uriInfo) {
        if (productoCaracteristica == null || idProducto == null
                || productoCaracteristica.getIdProductoCaracteristica() != null
                || productoCaracteristica.getIdCaracteristica() == null
                || productoCaracteristica.getIdCaracteristica().getIdCaracteristica() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "El recurso no puede ser nulo, idProducto e idCaracteristica no pueden ser nulos y el recurso no debe tener un ID asignado")
                    .build();
        }
        try {
            Producto producto = productoService.findById(idProducto);
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Producto no encontrado")
                        .build();
            }

            Caracteristica caracteristica = caracteristicaService.findById(
                    productoCaracteristica.getIdCaracteristica().getIdCaracteristica());
            if (caracteristica == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Característica no encontrada")
                        .build();
            }
            productoCaracteristica.setIdProductoCaracteristica(UUID.randomUUID());
            productoCaracteristica.setIdProducto(producto);
            productoCaracteristica.setIdCaracteristica(caracteristica);

            productoCaracteristicaService.create(productoCaracteristica);

            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(productoCaracteristica.getIdProductoCaracteristica().toString());
            return Response.created(uriBuilder.build()).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    /**
     * GET /producto/{id_producto}/caracteristica
     * Devuelve las características asociadas a un producto en un rango paginado.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorRango(
            @PathParam("id_producto") UUID idProducto,
            @QueryParam("first") @DefaultValue("0") int firstResult,
            @QueryParam("max") @DefaultValue("50") int maxResults) {
        if (idProducto == null || firstResult < 0 || maxResults <= 0 || maxResults > 50) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "Los parámetros 'first' debe ser >= 0 y 'max' debe ser > 0 y <= 50")
                    .build();
        }
        try {
            List<ProductoCaracteristica> registros = productoCaracteristicaService.findByProducto(idProducto, firstResult, maxResults);
            Long total = productoCaracteristicaService.contarPorProducto(idProducto);

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
     * GET /producto/{id_producto}/caracteristica/{id}
     * Devuelve una asignación de característica específica de un producto.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorId(
            @PathParam("id_producto") UUID idProducto,
            @PathParam("id") UUID id) {
        if (id == null || idProducto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idProducto e id no pueden ser nulos")
                    .build();
        }
        try {
            ProductoCaracteristica encontrado = productoCaracteristicaService.findByIdYProducto(id, idProducto);
            if (encontrado != null && encontrado.getIdProducto() != null && idProducto.equals(encontrado.getIdProducto().getIdProducto())) {
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
     * PUT /producto/{id_producto}/caracteristica/{id}
     * Actualiza una asignación de característica existente para el producto.
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(
            @PathParam("id_producto") UUID idProducto,
            @PathParam("id") UUID id,
            ProductoCaracteristica productoCaracteristica) {
        if (productoCaracteristica == null || idProducto == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El recurso o los identificadores no pueden ser nulos")
                    .build();
        }
        try {
            ProductoCaracteristica existente = productoCaracteristicaService.findByIdYProducto(id, idProducto);
            if (existente == null || existente.getIdProducto() == null || !idProducto.equals(existente.getIdProducto().getIdProducto())) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado")
                        .build();
            }

            if (productoCaracteristica.getIdCaracteristica() == null
                    || productoCaracteristica.getIdCaracteristica().getIdCaracteristica() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idCaracteristica no puede ser nulo")
                        .build();
            }
            Caracteristica caracteristica = caracteristicaService.findById(
                    productoCaracteristica.getIdCaracteristica().getIdCaracteristica());
            if (caracteristica == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Característica no encontrada")
                        .build();
            }
            productoCaracteristica.setIdProductoCaracteristica(id);
            productoCaracteristica.setIdProducto(existente.getIdProducto());
            productoCaracteristica.setIdCaracteristica(caracteristica);

            productoCaracteristicaService.update(productoCaracteristica);
            return Response.ok(productoCaracteristica).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /producto/{id_producto}/caracteristica/{id}
     * Elimina la asignación de característica de un producto.
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(
            @PathParam("id_producto") UUID idProducto,
            @PathParam("id") UUID id) {
        if (id == null || idProducto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idProducto e id no pueden ser nulos")
                    .build();
        }
        try {
            ProductoCaracteristica existente = productoCaracteristicaService.findByIdYProducto(id, idProducto);
            if (existente != null && existente.getIdProducto() != null && idProducto.equals(existente.getIdProducto().getIdProducto())) {
                productoCaracteristicaService.delete(existente);
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