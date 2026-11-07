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
import sv.edu.ues.ppi115.cafefe.control.ProductoService;
import sv.edu.ues.ppi115.cafefe.control.ProductoTipoProductoService;
import sv.edu.ues.ppi115.cafefe.control.TipoProductoService;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.ProductoTipoProducto;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

@Path("producto/{id_producto}/tipoproducto")
public class ProductoTipoProductoResource implements Serializable {

    @Inject
    ProductoTipoProductoService productoTipoProductoService;

    @Inject
    ProductoService productoService;

    @Inject
    TipoProductoService tipoProductoService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(@PathParam("id_producto") UUID idProducto,
            ProductoTipoProducto productoTipoProducto, @Context UriInfo uriInfo) {
        if (productoTipoProducto == null || idProducto == null
                || productoTipoProducto.getIdProductoTipoProducto() != null
                || productoTipoProducto.getIdTipoProducto() == null
                || productoTipoProducto.getIdTipoProducto().getIdTipoProducto() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "El recurso no puede ser nulo, idProducto e idTipoProducto no pueden ser nulos y el recurso no debe tener un ID asignado").build();
        }
        try {
            Producto producto = productoService.findById(idProducto);
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Producto no encontrado").build();
            }
            TipoProducto tipoProducto = tipoProductoService.findById(
                    productoTipoProducto.getIdTipoProducto().getIdTipoProducto());
            if (tipoProducto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "TipoProducto no encontrado").build();
            }
            productoTipoProducto.setIdProductoTipoProducto(UUID.randomUUID());
            productoTipoProducto.setIdProducto(producto);
            productoTipoProducto.setIdTipoProducto(tipoProducto);
            productoTipoProductoService.create(productoTipoProducto);
            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder()
                    .path(productoTipoProducto.getIdProductoTipoProducto().toString());
            return Response.created(uriBuilder.build()).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorRango(@PathParam("id_producto") UUID idProducto,
            @QueryParam("first") @DefaultValue("0") int firstResult,
            @QueryParam("max") @DefaultValue("50") int maxResults) {
        if (idProducto == null || firstResult < 0 || maxResults <= 0 || maxResults > 50) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "Los parámetros 'first' debe ser >= 0 y 'max' debe ser > 0 y <= 50").build();
        }
        try {
            List<ProductoTipoProducto> registros = productoTipoProductoService
                    .findByProducto(idProducto, firstResult, maxResults);
            Long total = productoTipoProductoService.contarPorProducto(idProducto);
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
    public Response buscarPorId(@PathParam("id_producto") UUID idProducto, @PathParam("id") UUID id) {
        if (idProducto == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "idProducto e id no pueden ser nulos").build();
        }
        try {
            ProductoTipoProducto encontrado = productoTipoProductoService.findByIdYProducto(id, idProducto);
            if (encontrado != null && encontrado.getIdProducto() != null
                    && idProducto.equals(encontrado.getIdProducto().getIdProducto())) {
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
    public Response actualizar(@PathParam("id_producto") UUID idProducto, @PathParam("id") UUID id,
            ProductoTipoProducto productoTipoProducto) {
        if (productoTipoProducto == null || idProducto == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "El recurso o los identificadores no pueden ser nulos").build();
        }
        try {
            ProductoTipoProducto existente = productoTipoProductoService.findByIdYProducto(id, idProducto);
            if (existente == null || existente.getIdProducto() == null
                    || !idProducto.equals(existente.getIdProducto().getIdProducto())) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado").build();
            }
            if (productoTipoProducto.getIdTipoProducto() == null
                    || productoTipoProducto.getIdTipoProducto().getIdTipoProducto() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idTipoProducto no puede ser nulo").build();
            }
            TipoProducto tipoProducto = tipoProductoService.findById(
                    productoTipoProducto.getIdTipoProducto().getIdTipoProducto());
            if (tipoProducto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "TipoProducto no encontrado").build();
            }
            productoTipoProducto.setIdProductoTipoProducto(id);
            productoTipoProducto.setIdProducto(existente.getIdProducto());
            productoTipoProducto.setIdTipoProducto(tipoProducto);
            productoTipoProductoService.update(productoTipoProducto);
            return Response.ok(productoTipoProducto).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("id_producto") UUID idProducto, @PathParam("id") UUID id) {
        if (idProducto == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "idProducto e id no pueden ser nulos").build();
        }
        try {
            ProductoTipoProducto existente = productoTipoProductoService.findByIdYProducto(id, idProducto);
            if (existente != null && existente.getIdProducto() != null
                    && idProducto.equals(existente.getIdProducto().getIdProducto())) {
                productoTipoProductoService.delete(existente);
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
